package bo.org.tecnoweb.cpms.procedures.application.tutorassignment;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfileStatus;
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
    private final User director = secretary().withRole(Role.DIRECTOR);
    private final User tutor = secretary().withRole(Role.PROFESSOR).withEmailAddress("doc@test.test");
    private final DegreeProcedure procedure = DegreeProcedure.rehydrate(UUID.randomUUID(), REFERENCE,
            student.getId(), UUID.randomUUID(), Instant.now(), null, Map.of());

    private DegreeProject project(ProjectProfileStatus status) {
        var evaluatedOn = status == ProjectProfileStatus.PENDING ? null : Instant.now();
        var observations = status.allowsNewVersion() ? "Forma" : null;
        return DegreeProject.rehydrate(UUID.randomUUID(), procedure.getId(), "Redes",
                List.of(new ProjectProfile(1, UUID.randomUUID(), "Redes", status, observations, evaluatedOn)), null);
    }

    private void stubAssignment(DegreeProject project) {
        when(users.getByEmail(director.getEmailAddress())).thenReturn(Optional.of(director));
        when(users.getByEmail(tutor.getEmailAddress())).thenReturn(Optional.of(tutor));
        when(users.getById(student.getId())).thenReturn(Optional.of(student));
        when(procedures.getOpenByReferenceForUpdate(REFERENCE)).thenReturn(Optional.of(procedure));
        when(projects.getByProcedureId(procedure.getId())).thenReturn(Optional.of(project));
    }

    /** Student accounts cannot use the assignment operation (FR-003). */
    @Test
    void rejectsStudentAssigningTutor() {
        when(users.getByEmail(student.getEmailAddress())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> useCase.assignTutor(student.getEmailAddress(), new AssignTutorCommand(REFERENCE, "doc@test")))
                .isInstanceOf(UserNotAuthorizedException.class);
        verifyNoInteractions(procedures, projects, notifier);
    }

    /** Dirección assigns the tutor on a project whose current profile is approved (FR-003, FR-020). */
    @Test
    void assignsTutorAndNotifiesStudent() {
        var project = project(ProjectProfileStatus.APPROVED);
        stubAssignment(project);

        useCase.assignTutor(director.getEmailAddress(), new AssignTutorCommand(REFERENCE, tutor.getEmailAddress()));

        assertThat(project.getTutorId()).isEqualTo(tutor.getId());
        var sequence = inOrder(projects, notifier);
        sequence.verify(projects).update(project);
        sequence.verify(notifier).notifyTutorAssigned(argThat(event -> event.student().equals(student.getEmailAddress())));
    }

    /** A profile that is not approved blocks the assignment (FR-003, FR-005). */
    @Test
    void rejectsAssignmentWithoutApproval() {
        var project = project(ProjectProfileStatus.OBSERVED);
        stubAssignment(project);

        assertThatThrownBy(() -> useCase.assignTutor(director.getEmailAddress(),
                new AssignTutorCommand(REFERENCE, tutor.getEmailAddress())))
                .isInstanceOfSatisfying(DomainValidationException.class, exception ->
                        assertThat(exception.getErrors().get("perfil")).contains("aprobación"));
        verify(projects, never()).update(any());
        verifyNoInteractions(notifier);
    }
}
