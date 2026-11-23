package bo.org.tecnoweb.cpms.procedures.application;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.OpeningRequirement;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Exercises UC-002 decisions using doubles of the outgoing ports. */
class DegreeProcedureUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final DegreeProcedureRepository procedures = mock(DegreeProcedureRepository.class);
    private final DegreeProcedureNotifier notifier = mock(DegreeProcedureNotifier.class);
    private final AcademicCalendar calendar = () -> SEMESTER;
    private final Storage storage = new Storage();
    private final DegreeProcedureUseCase useCase = new DegreeProcedureUseCase(users, procedures, notifier,
            storage.service, calendar);
    private final String requester = "secretaria@cpms.test";
    private final User student = student();
    private final OpenDegreeProcedureCommand command = new OpenDegreeProcedureCommand("123456", storage.documents());

    /** Basic flow: opens on the account of the registration number and confirms its reference (FR-056). */
    @Test
    void opensOnTheStudentAccountOfTheCode() {
        allowOpening();

        useCase.open(requester, command);

        verify(procedures).insert(argThat(procedure -> procedure.getStudentId().equals(student.getId())
                && procedure.getReference().equals(REFERENCE)));
        verify(users, never()).insert(any());
        verify(notifier).notifyOpened(argThat(event -> event.reference().equals(REFERENCE)
                && event.student().equals(student.getEmailAddress())));
    }

    /** The registration number is read without surrounding spaces. */
    @Test
    void findsTheAccountWithoutSurroundingSpaces() {
        allowOpening();

        useCase.open(requester, new OpenDegreeProcedureCommand(" 123456 ", command.attachmentIds()));

        verify(users).getByUagrmCodeForUpdate("123456");
        verify(procedures).insert(any());
    }

    /** Without an account for the registration number the opening is rejected: UC-001 creates it first. */
    @Test
    void rejectsACodeWithoutAccount() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary()));
        when(users.getByUagrmCodeForUpdate("123456")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.open(requester, command))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsExactly(Map.entry(DegreeProcedure.CODE_FIELD,
                                "No existe una cuenta con el registro 123456. Créela primero con usuarios.crearCuenta.")));
        verify(procedures, never()).insert(any());
        verifyNoInteractions(notifier);
    }

    /** An empty registration number is rejected before looking for an account. */
    @Test
    void rejectsAnEmptyCode() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary()));

        assertThatThrownBy(() -> useCase.open(requester, new OpenDegreeProcedureCommand(" ", command.attachmentIds())))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrors()).containsOnlyKeys(DegreeProcedure.CODE_FIELD));
        verify(users, never()).getByUagrmCodeForUpdate(any());
    }

    /** The registration number of a professor does not open a procedure (FR-056). */
    @Test
    void rejectsAnAccountOfAnotherRole() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary()));
        var professor = User.builder().withRole(Role.PROFESSOR).withEmailAddress("luis@cpms.test")
                .withFirstName("Luis").withLastName("Vaca").withUagrmCode("123456");
        when(users.getByUagrmCodeForUpdate("123456")).thenReturn(Optional.of(professor));

        assertThatThrownBy(() -> useCase.open(requester, command))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsExactly(Map.entry(DegreeProcedure.CODE_FIELD, "La cuenta del registro 123456 no es de estudiante.")));
        verify(procedures, never()).insert(any());
    }

    /** A disabled student account does not open a procedure (FR-056). */
    @Test
    void rejectsADisabledStudent() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary()));
        var disabled = User.rehydrate().withId(UUID.randomUUID()).withRole(Role.STUDENT).withEmailAddress("ana@cpms.test")
                .withFirstName("Ana").withLastName("Pérez").withUagrmCode("123456").withActive(false);
        when(users.getByUagrmCodeForUpdate("123456")).thenReturn(Optional.of(disabled));

        assertThatThrownBy(() -> useCase.open(requester, command))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrors()).containsOnlyKeys("cuenta"));
        verify(procedures, never()).insert(any());
    }

    /** A student with an ongoing procedure cannot open another (FR-056). */
    @Test
    void rejectsAStudentWithAnOngoingProcedure() {
        allowOpening();
        when(procedures.existsOpenForStudent(student.getId())).thenReturn(true);

        assertThatThrownBy(() -> useCase.open(requester, command))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsExactly(Map.entry(DegreeProcedure.CODE_FIELD,
                                "El estudiante ya tiene un trámite de titulación en curso.")));
        verify(procedures, never()).insert(any());
    }

    /** A student whose procedure of the semester is closed cannot open another in the same semester. */
    @Test
    void rejectsASecondProcedureInTheSameSemester() {
        allowOpening();
        when(procedures.existsByReference(REFERENCE)).thenReturn(true);

        assertThatThrownBy(() -> useCase.open(requester, command))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsExactly(Map.entry(DegreeProcedure.CODE_FIELD,
                                "El estudiante ya tiene un trámite de titulación en el semestre 2026-2.")));
        verify(procedures, never()).insert(any());
        verifyNoInteractions(notifier);
    }

    /** A closed procedure of another semester does not prevent opening in the current one. */
    @Test
    void opensWhenThePreviousProcedureIsOfAnotherSemester() {
        allowOpening();
        when(procedures.existsByReference("2026-1-123456")).thenReturn(true);

        useCase.open(requester, command);

        verify(procedures).insert(argThat(procedure -> procedure.getReference().equals(REFERENCE)));
    }

    /** Permissions prevent other roles from opening and replacing. */
    @Test
    void deniesADirector() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary().withRole(Role.DIRECTOR)));
        assertThatThrownBy(() -> useCase.open(requester, command)).isInstanceOf(UserNotAuthorizedException.class);
        assertThatThrownBy(() -> useCase.replaceRequirement(requester, new ReplaceOpeningRequirementCommand(REFERENCE, storage.pdf("ci.pdf"))))
                .isInstanceOf(UserNotAuthorizedException.class);
        verify(users, never()).getByUagrmCodeForUpdate(any());
        verifyNoInteractions(procedures, notifier);
    }

    /** The procedure adopts the four stored files without writing new ones (issue #9). */
    @Test
    void openingAdoptsTheStoredFiles() {
        allowOpening();
        useCase.open(requester, command);

        verify(procedures).insert(argThat(procedure -> procedure.getRequirements().values()
                .containsAll(command.attachmentIds())));
        assertThat(storage.writes).isEmpty();
    }

    /** An attachment that is not a PDF rejects the opening before creating anything (NFR-005). */
    @Test
    void rejectsAnInvalidPdfWithoutOpening() {
        allowOpening();
        var ids = List.of(storage.pdf("carta.pdf"), storage.pdf("cv.pdf"), storage.pdf("ci.pdf"),
                storage.store("boleta.pdf", "texto".getBytes(StandardCharsets.US_ASCII)));

        assertThatThrownBy(() -> useCase.open(requester, new OpenDegreeProcedureCommand("123456", ids)))
                .isInstanceOfSatisfying(DomainValidationException.class, error -> assertThat(error.getErrors())
                        .containsEntry("adjuntos", "El archivo boleta.pdf debe ser un PDF."));
        verify(procedures, never()).insert(any());
    }

    /** A2 links the received PDF as the next version of the replaced document (FR-055, issue #9). */
    @Test
    void replacementLinksTheReceivedFileAsTheNextVersion() {
        var opened = openedProcedure();
        var previous = opened.requirementOf(OpeningRequirement.IDENTITY);
        var received = storage.pdf("ci_actualizada.pdf");

        useCase.replaceRequirement(requester, new ReplaceOpeningRequirementCommand(REFERENCE, received));

        verify(procedures).update(argThat(procedure ->
                procedure.requirementOf(OpeningRequirement.IDENTITY).equals(received)));
        assertThat(storage.service.getHistory(received)).extracting(StoredFile::getId)
                .containsExactly(previous, received);
        assertThat(storage.writes).isEmpty();
        verify(notifier).notifyRequirementReplaced(argThat(event -> event.reference().equals(REFERENCE)));
    }

    /** An invalid PDF, a malformed reference or an unavailable procedure neither replaces nor links versions (FR-055). */
    @Test
    void rejectedReplacementLinksNothing() {
        var opened = openedProcedure();
        var previous = opened.requirementOf(OpeningRequirement.IDENTITY);
        var notPdf = storage.store("ci.pdf", "texto".getBytes(StandardCharsets.US_ASCII));

        assertThatThrownBy(() -> useCase.replaceRequirement(requester, new ReplaceOpeningRequirementCommand(REFERENCE, notPdf)))
                .isInstanceOf(DomainValidationException.class);
        var valid = storage.pdf("ci.pdf");
        assertThatThrownBy(() -> useCase.replaceRequirement(requester, new ReplaceOpeningRequirementCommand("TR-001", valid)))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrors()).containsOnlyKeys(DegreeProcedure.REFERENCE_FIELD));
        when(procedures.getOpenByReferenceForUpdate("2026-2-999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.replaceRequirement(requester, new ReplaceOpeningRequirementCommand("2026-2-999", valid)))
                .isInstanceOf(DomainValidationException.class);

        verify(procedures, never()).update(any());
        assertThat(storage.service.get(previous).isCurrent()).isTrue();
        assertThat(storage.service.get(valid).getFirstVersionId()).isEqualTo(valid);
    }

    /** An open procedure of the student whose documents are stored files. */
    private DegreeProcedure openedProcedure() {
        var secretary = secretary();
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary));
        var files = storage.documents().stream().map(storage.service::get).toList();
        var opened = DegreeProcedure.builder().open(student, secretary, SEMESTER, files);
        var persisted = DegreeProcedure.rehydrate(opened.getId(), REFERENCE, student.getId(), secretary.getId(),
                opened.getOpenedAt(), null, opened.getRequirements());
        when(procedures.getOpenByReferenceForUpdate(REFERENCE)).thenReturn(Optional.of(persisted));
        return persisted;
    }

    private void allowOpening() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary()));
        when(users.getByUagrmCodeForUpdate("123456")).thenReturn(Optional.of(student));
    }
}
