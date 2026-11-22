package bo.org.tecnoweb.cpms.procedures.application;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.InitialDocument;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.users.application.UserManagementNotifier;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Exercises UC-002 decisions using doubles of the outgoing ports. */
class DegreeProcedureUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final DegreeProcedureRepository procedures = mock(DegreeProcedureRepository.class);
    private final UserManagementNotifier userNotifier = mock(UserManagementNotifier.class);
    private final DegreeProcedureNotifier notifier = mock(DegreeProcedureNotifier.class);
    private final Storage storage = new Storage();
    private final DegreeProcedureUseCase useCase = new DegreeProcedureUseCase(users, procedures, userNotifier, notifier,
            storage.service);
    private final String requester = "secretaria@cpms.test";
    private final OpenDegreeProcedureCommand command = new OpenDegreeProcedureCommand(
            "Ana", "Pérez", "", "1234", "123456", "ana@cpms.test", "7123456", storage.documents());

    /** Reuses an existing account without another sign-up notice (FR-056). */
    @Test
    void linksExistingStudent() {
        allowOpening();
        var student = student();
        when(users.getByEmailForUpdate(command.email())).thenReturn(Optional.of(student));
        useCase.open(requester, command);

        verify(procedures).insert(argThat(procedure -> procedure.getStudentId().equals(student.getId())));
        verify(users, never()).insert(any());
        verifyNoInteractions(userNotifier);
        verify(notifier).notifyOpened(any(), eq("TR-001"));
    }

    /** A1 creates the account and uses the existing sign-up notice (FR-035, FR-051). */
    @Test
    void createsAnAccountAndNotifiesStudent() {
        allowOpening();
        useCase.open(requester, command);

        var order = inOrder(users, procedures);
        order.verify(users).insert(argThat(student -> student.getEmailAddress().equals(command.email())));
        order.verify(procedures).insert(any());
        verify(userNotifier).notifyNewAccount(argThat(event -> event.signedUpUser().equals(command.email())));
    }

    /** FR-033 duplicate codes become a generic account validation error. */
    @Test
    void rejectsDuplicateCodeWithoutOpening() {
        allowOpening();
        doThrow(new DomainValidationException(Map.of("código", "Duplicado."))).when(users).insert(any());

        assertThatThrownBy(() -> useCase.open(requester, command))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        error -> assertThat(error.getErrors().get("cuenta")).contains("Verifique el correo y el registro"));
        verify(procedures, never()).insert(any());
        verifyNoInteractions(userNotifier, notifier);
    }

    /** Permissions prevent other roles from opening and replacing. */
    @Test
    void deniesADirector() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary().withRole(Role.DIRECTOR)));
        assertThatThrownBy(() -> useCase.open(requester, command)).isInstanceOf(UserNotAuthorizedException.class);
        assertThatThrownBy(() -> useCase.replace(requester, new ReplaceInitialDocumentCommand("TR-001", storage.pdf("ci.pdf"))))
                .isInstanceOf(UserNotAuthorizedException.class);
        verifyNoInteractions(procedures, notifier);
    }

    /** The procedure adopts the four stored files without writing new ones (issue #9). */
    @Test
    void openingAdoptsTheStoredFiles() {
        allowOpening();
        useCase.open(requester, command);

        verify(procedures).insert(argThat(procedure -> procedure.getDocuments().values().stream()
                .map(DegreeProcedure.Document::storedFileId).toList().containsAll(command.attachmentIds())));
        assertThat(storage.writes).isEmpty();
    }

    /** An attachment that is not a PDF rejects the opening before creating anything (NFR-005). */
    @Test
    void rejectsAnInvalidPdfWithoutOpening() {
        allowOpening();
        var ids = List.of(storage.pdf("carta.pdf"), storage.pdf("cv.pdf"), storage.pdf("ci.pdf"),
                storage.store("boleta.pdf", "texto".getBytes(StandardCharsets.US_ASCII)));
        var invalid = new OpenDegreeProcedureCommand("Ana", "Pérez", "", "1234", "123456", "ana@cpms.test",
                "7123456", ids);

        assertThatThrownBy(() -> useCase.open(requester, invalid))
                .isInstanceOfSatisfying(DomainValidationException.class, error -> assertThat(error.getErrors())
                        .containsEntry("adjuntos", "El archivo boleta.pdf debe ser un PDF."));
        verify(procedures, never()).insert(any());
        verify(users, never()).insert(any());
    }

    /** A2 links the received PDF as the next version of the replaced document (FR-055, issue #9). */
    @Test
    void replacementLinksTheReceivedFileAsTheNextVersion() {
        var opened = openedProcedure();
        var previous = opened.documentOf(InitialDocument.IDENTITY).storedFileId();
        var received = storage.pdf("ci_actualizada.pdf");

        useCase.replace(requester, new ReplaceInitialDocumentCommand("TR-001", received));

        verify(procedures).update(argThat(procedure ->
                procedure.documentOf(InitialDocument.IDENTITY).storedFileId().equals(received)));
        assertThat(storage.service.getHistory(received)).extracting(StoredFile::getId)
                .containsExactly(previous, received);
        assertThat(storage.writes).isEmpty();
        verify(notifier).notifyDocumentReplaced(any());
    }

    /** An invalid PDF or an unavailable procedure neither replaces nor links versions (FR-055). */
    @Test
    void rejectedReplacementLinksNothing() {
        var opened = openedProcedure();
        var previous = opened.documentOf(InitialDocument.IDENTITY).storedFileId();
        var notPdf = storage.store("ci.pdf", "texto".getBytes(StandardCharsets.US_ASCII));

        assertThatThrownBy(() -> useCase.replace(requester, new ReplaceInitialDocumentCommand("TR-001", notPdf)))
                .isInstanceOf(DomainValidationException.class);
        when(procedures.getOpenByReferenceForUpdate(2L)).thenReturn(Optional.empty());
        var valid = storage.pdf("ci.pdf");
        assertThatThrownBy(() -> useCase.replace(requester, new ReplaceInitialDocumentCommand("TR-002", valid)))
                .isInstanceOf(DomainValidationException.class);

        verify(procedures, never()).update(any());
        assertThat(storage.service.get(previous).isCurrent()).isTrue();
        assertThat(storage.service.get(valid).getFirstVersionId()).isEqualTo(valid);
    }

    /** An open procedure TR-001 whose documents are stored files. */
    private DegreeProcedure openedProcedure() {
        var secretary = secretary();
        var student = student();
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary));
        var files = storage.documents().stream().map(storage.service::get).toList();
        var opened = DegreeProcedure.builder().withStudent(student).open(student, secretary, files);
        var persisted = DegreeProcedure.rehydrate(opened.getId(), 1, student.getId(), secretary.getId(),
                opened.getOpenedAt(), null, opened.getDocuments());
        when(procedures.getOpenByReferenceForUpdate(1L)).thenReturn(Optional.of(persisted));
        return persisted;
    }

    private void allowOpening() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary()));
        when(procedures.insert(any())).thenAnswer(call -> {
            DegreeProcedure procedure = call.getArgument(0);
            return DegreeProcedure.rehydrate(procedure.getId(), 1, procedure.getStudentId(), procedure.getOpenedBy(),
                    procedure.getOpenedAt(), null, procedure.getDocuments());
        });
    }
}
