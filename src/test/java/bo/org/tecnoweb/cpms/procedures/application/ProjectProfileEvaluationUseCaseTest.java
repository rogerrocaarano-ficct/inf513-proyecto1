package bo.org.tecnoweb.cpms.procedures.application;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfileStatus;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Checks UC-004 with doubles of the repositories and notifier and a real file storage over in-memory
 * ports. The exceptions E1 to E7 are those of {@code use-cases/UC-004.md}.
 */
class ProjectProfileEvaluationUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final DegreeProcedureRepository procedures = mock(DegreeProcedureRepository.class);
    private final DegreeProjectRepository projects = mock(DegreeProjectRepository.class);
    private final ProjectProfileEvaluationNotifier notifier = mock(ProjectProfileEvaluationNotifier.class);
    private final Storage storage = new Storage();
    private final ProjectProfileEvaluationUseCase useCase = new ProjectProfileEvaluationUseCase(users, procedures,
            projects, notifier, storage.service);
    private final User student = student();
    private final User workshop = secretary().withRole(Role.CAPSTONE_PROJECT_PROFESSOR)
            .withEmailAddress("taller@cpms.test");
    private final User director = secretary().withRole(Role.DIRECTOR).withEmailAddress("director@cpms.test");
    private final DegreeProcedure procedure = DegreeProcedure.rehydrate(UUID.randomUUID(), REFERENCE,
            student.getId(), UUID.randomUUID(), Instant.now(), null, Map.of());
    private final RegisterProfileCommand submission = new RegisterProfileCommand("Redes", storage.pdf("perfil.pdf"));

    private void stubSubmission() {
        when(users.getByEmail(student.getEmailAddress())).thenReturn(Optional.of(student));
        when(procedures.getOpenByStudentForUpdate(student.getId())).thenReturn(Optional.of(procedure));
        when(users.getByRole(Role.CAPSTONE_PROJECT_PROFESSOR)).thenReturn(List.of(workshop));
    }

    private DegreeProject stubEvaluation() {
        var project = DegreeProject.builder().withProcedureId(procedure.getId()).registerProfile(
                storage.service.get(submission.attachmentId()), "Redes", student.getEmailAddress(),
                workshop.getEmailAddress(), REFERENCE);
        project.pullEvents();
        when(users.getByEmail(workshop.getEmailAddress())).thenReturn(Optional.of(workshop));
        when(users.getById(student.getId())).thenReturn(Optional.of(student));
        when(procedures.getOpenByReferenceForUpdate(REFERENCE)).thenReturn(Optional.of(procedure));
        when(projects.getByProcedureId(procedure.getId())).thenReturn(Optional.of(project));
        return project;
    }

    /** Basic flow: the first profile creates a pending project and is sent to Taller (FR-004, FR-057). */
    @Test
    void registersTheFirstProfileAndNotifiesWorkshop() {
        stubSubmission();

        useCase.registerProfile(student.getEmailAddress(), submission);

        var sequence = inOrder(projects, notifier);
        sequence.verify(projects).insert(argThat(project -> project.getProcedureId().equals(procedure.getId())
                && project.getProfile().storedFileId().equals(submission.attachmentId())
                && project.getProfile().status() == ProjectProfileStatus.PENDING));
        sequence.verify(notifier).notifyProfileRegistered(argThat(event ->
                event.workshop().equals(workshop.getEmailAddress()) && event.profile().version() == 1));
    }

    /**
     * Basic flow after an observation: the project numbers the new version, and the file storage does
     * not chain the PDF to the previous one (FR-023).
     */
    @Test
    void registersANumberedVersionWithoutChainingFiles() {
        var project = stubEvaluation();
        project.observeProfile("Forma", student.getEmailAddress(), REFERENCE);
        project.pullEvents();
        stubSubmission();
        var second = storage.pdf("perfil-v2.pdf");

        useCase.registerProfile(student.getEmailAddress(), new RegisterProfileCommand("Redes rurales", second));

        verify(projects).update(project);
        verify(projects, never()).insert(any());
        assertThat(project.getProfiles()).extracting(profile -> profile.version()).containsExactly(1, 2);
        assertThat(project.getTitle()).isEqualTo("Redes rurales");
        assertThat(storage.service.get(submission.attachmentId()).getNextVersionId()).isNull();
        assertThat(storage.service.get(second).getFirstVersionId()).isEqualTo(second);
        verify(notifier).notifyProfileRegistered(argThat(event -> event.profile().version() == 2));
    }

    /** E1: a disabled or unauthorized account cannot submit or evaluate. */
    @Test
    void rejectsUnauthorizedRequesters() {
        when(users.getByEmail(workshop.getEmailAddress())).thenReturn(Optional.of(workshop));
        when(users.getByEmail(director.getEmailAddress())).thenReturn(Optional.of(director));

        assertThatThrownBy(() -> useCase.registerProfile(workshop.getEmailAddress(), submission))
                .isInstanceOf(UserNotAuthorizedException.class);
        assertThatThrownBy(() -> useCase.approveProfile(director.getEmailAddress(), REFERENCE))
                .isInstanceOf(UserNotAuthorizedException.class);
        verifyNoInteractions(procedures, projects, notifier);
    }

    /** E2: without an ongoing procedure nothing is registered. */
    @Test
    void rejectsSubmissionWithoutProcedure() {
        when(users.getByEmail(student.getEmailAddress())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> useCase.registerProfile(student.getEmailAddress(), submission))
                .isInstanceOfSatisfying(DomainValidationException.class, exception ->
                        assertThat(exception.getErrors()).containsKey("trámite"));
        verifyNoInteractions(projects, notifier);
    }

    /** E3: a pending profile admits no new version (FR-058). */
    @Test
    void rejectsSubmissionWhilePending() {
        stubEvaluation();
        stubSubmission();

        assertThatThrownBy(() -> useCase.registerProfile(student.getEmailAddress(),
                new RegisterProfileCommand("Otro", storage.pdf("otro.pdf"))))
                .isInstanceOf(DomainValidationException.class);
        verify(projects, never()).update(any());
        verifyNoInteractions(notifier);
    }

    /** E4: a file that is not a PDF is rejected without registering the profile (NFR-005). */
    @Test
    void rejectsAnInvalidPdf() {
        stubSubmission();
        var text = storage.store("perfil.txt", "texto".getBytes(StandardCharsets.US_ASCII));

        assertThatThrownBy(() -> useCase.registerProfile(student.getEmailAddress(), new RegisterProfileCommand("Redes", text)))
                .isInstanceOf(DomainValidationException.class);
        verify(projects, never()).insert(any());
        verifyNoInteractions(notifier);
    }

    /** E5: missing Taller leaves the request retryable without inserting a project (FR-057). */
    @Test
    void rejectsSubmissionWithoutWorkshop() {
        stubSubmission();
        when(users.getByRole(Role.CAPSTONE_PROJECT_PROFESSOR)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.registerProfile(student.getEmailAddress(), submission))
                .isInstanceOfSatisfying(DomainValidationException.class, exception ->
                        assertThat(exception.getErrors().get("perfil")).contains("Taller de Grado"));
        verify(projects, never()).insert(any());
        verifyNoInteractions(notifier);
    }

    /** A1: approval notifies the student and the active director (FR-005, FR-060, FR-061). */
    @Test
    void approvesAndNotifiesStudentAndDirector() {
        var project = stubEvaluation();
        when(users.getByRole(Role.DIRECTOR)).thenReturn(List.of(director));

        useCase.approveProfile(workshop.getEmailAddress(), REFERENCE);

        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.APPROVED);
        var sequence = inOrder(projects, notifier);
        sequence.verify(projects).update(project);
        sequence.verify(notifier).notifyProfileApproved(argThat(event ->
                event.student().equals(student.getEmailAddress())
                        && event.director().equals(director.getEmailAddress())));
    }

    /** A1 without an active director: the approval is recorded all the same (FR-061). */
    @Test
    void approvesWithoutActiveDirector() {
        var project = stubEvaluation();
        when(users.getByRole(Role.DIRECTOR)).thenReturn(List.of(director.withActive(false)));

        useCase.approveProfile(workshop.getEmailAddress(), REFERENCE);

        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.APPROVED);
        verify(notifier).notifyProfileApproved(argThat(event -> event.director() == null));
    }

    /** A2: observation keeps the observations and sends them to the student (FR-022, FR-060). */
    @Test
    void observesAndNotifiesStudent() {
        var project = stubEvaluation();

        useCase.observeProfile(workshop.getEmailAddress(), new ObserveProfileCommand(REFERENCE, "Forma"));

        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.OBSERVED);
        verify(projects).update(project);
        verify(notifier).notifyProfileObserved(argThat(event -> event.observations().equals("Forma")));
    }

    /** A3: rejection keeps the reason and sends it to the student (FR-022, FR-060). */
    @Test
    void rejectsAndNotifiesStudent() {
        var project = stubEvaluation();

        useCase.rejectProfile(workshop.getEmailAddress(), new RejectProfileCommand(REFERENCE, "Tema"));

        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.REJECTED);
        verify(notifier).notifyProfileRejected(argThat(event -> event.observations().equals("Tema")));
    }

    /** E6: a procedure without a pending profile is not evaluated. */
    @Test
    void rejectsEvaluationWithoutPendingProfile() {
        var project = stubEvaluation();
        project.observeProfile("Forma", student.getEmailAddress(), REFERENCE);
        project.pullEvents();

        assertThatThrownBy(() -> useCase.approveProfile(workshop.getEmailAddress(), REFERENCE))
                .isInstanceOf(DomainValidationException.class);
        when(projects.getByProcedureId(procedure.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.rejectProfile(workshop.getEmailAddress(), new RejectProfileCommand(REFERENCE, "Tema")))
                .isInstanceOf(DomainValidationException.class);
        verify(projects, never()).update(any());
        verifyNoInteractions(notifier);
    }

    /** E7: observing or rejecting with an empty body changes nothing (FR-022). */
    @Test
    void rejectsEvaluationWithoutObservations() {
        var project = stubEvaluation();

        assertThatThrownBy(() -> useCase.observeProfile(workshop.getEmailAddress(), new ObserveProfileCommand(REFERENCE, "")))
                .isInstanceOfSatisfying(DomainValidationException.class, exception ->
                        assertThat(exception.getErrors()).containsKey("observaciones"));
        assertThatThrownBy(() -> useCase.rejectProfile(workshop.getEmailAddress(), new RejectProfileCommand(REFERENCE, null)))
                .isInstanceOf(DomainValidationException.class);
        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.PENDING);
        verify(projects, never()).update(any());
        verifyNoInteractions(notifier);
    }
}
