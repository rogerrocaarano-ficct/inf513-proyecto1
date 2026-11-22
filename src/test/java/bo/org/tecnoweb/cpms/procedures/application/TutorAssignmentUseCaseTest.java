package bo.org.tecnoweb.cpms.procedures.application;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Checks UC-003 with doubles of the repositories and notifier. */
class TutorAssignmentUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final DegreeProcedureRepository procedures = mock(DegreeProcedureRepository.class);
    private final DegreeProjectRepository projects = mock(DegreeProjectRepository.class);
    private final TutorAssignmentNotifier notifier = mock(TutorAssignmentNotifier.class);
    private final TutorAssignmentUseCase useCase = new TutorAssignmentUseCase(users, procedures, projects, notifier);
    private final User student = student();
    private final DegreeProcedure procedure = DegreeProcedure.rehydrate(UUID.randomUUID(), 1,
            student.getId(), UUID.randomUUID(), Instant.now(), null, Map.of());
    private final RegisterInitialProfileCommand submission = new RegisterInitialProfileCommand("Redes", pdf("perfil.pdf"));

    /** A valid student delivery writes a pending profile and notifies Taller (FR-057). */
    @Test
    void registersAndNotifiesWorkshop() {
        var workshop = secretary().withRole(Role.CAPSTONE_PROJECT_PROFESSOR);
        stubStudentProcedure();
        when(users.getByRole(Role.CAPSTONE_PROJECT_PROFESSOR)).thenReturn(List.of(workshop));

        useCase.registerInitialProfile(student.getEmailAddress(), submission);

        var sequence = inOrder(projects, notifier);
        sequence.verify(projects).insert(argThat(project -> project.getProcedureId().equals(procedure.getId())
                && !project.getProfile().approved()));
        sequence.verify(notifier).notifyProfileRegistered(argThat(event -> event.workshop().equals(workshop.getEmailAddress())));
    }

    /** Missing Taller leaves the request retryable without inserting a project (FR-057). */
    @Test
    void rejectsSubmissionWithoutWorkshop() {
        stubStudentProcedure();
        assertThatThrownBy(() -> useCase.registerInitialProfile(student.getEmailAddress(), submission))
                .isInstanceOfSatisfying(DomainValidationException.class, exception ->
                        assertThat(exception.getErrors().get("perfil")).contains("Taller de Grado"));

        verify(projects, never()).insert(any());
        verifyNoInteractions(notifier);
    }

    /** Student accounts cannot use the assignment operation (FR-003). */
    @Test
    void rejectsStudentAssigningTutor() {
        when(users.getByEmail(student.getEmailAddress())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> useCase.assignTutor(student.getEmailAddress(), new AssignTutorCommand("TR-001", "doc@test")))
                .isInstanceOf(UserNotAuthorizedException.class);
        verifyNoInteractions(procedures, projects, notifier);
    }

    /** Dirección assigns the tutor on a previously approved project (FR-003, FR-020). */
    @Test
    void assignsTutorAndNotifiesStudent() {
        var director = secretary().withRole(Role.DIRECTOR);
        var tutor = secretary().withRole(Role.PROFESSOR).withEmailAddress("doc@test.test");
        var project = DegreeProject.rehydrate(UUID.randomUUID(), procedure.getId(), "Redes",
                new ProjectProfile("perfil.pdf", pdf("perfil.pdf").getData(), true), null);
        when(users.getByEmail(director.getEmailAddress())).thenReturn(Optional.of(director));
        when(users.getByEmail(tutor.getEmailAddress())).thenReturn(Optional.of(tutor));
        when(users.getById(student.getId())).thenReturn(Optional.of(student));
        when(procedures.getOpenByReferenceForUpdate(1)).thenReturn(Optional.of(procedure));
        when(projects.getByProcedureId(procedure.getId())).thenReturn(Optional.of(project));

        useCase.assignTutor(director.getEmailAddress(), new AssignTutorCommand("TR-001", tutor.getEmailAddress()));

        assertThat(project.getTutorId()).isEqualTo(tutor.getId());
        var sequence = inOrder(projects, notifier);
        sequence.verify(projects).update(project);
        sequence.verify(notifier).notifyTutorAssigned(argThat(event -> event.student().equals(student.getEmailAddress())));
    }

    private void stubStudentProcedure() {
        when(users.getByEmail(student.getEmailAddress())).thenReturn(Optional.of(student));
        when(procedures.getOpenByStudentForUpdate(student.getId())).thenReturn(Optional.of(procedure));
    }
}
