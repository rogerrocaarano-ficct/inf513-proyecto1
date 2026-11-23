package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.procedures.application.degreeprocedure.AcademicCalendar;
import bo.org.tecnoweb.cpms.procedures.application.degreeprocedure.DegreeProcedureNotifier;
import bo.org.tecnoweb.cpms.procedures.application.degreeprocedure.DegreeProcedureUseCase;
import bo.org.tecnoweb.cpms.procedures.application.degreeprocedure.OpenDegreeProcedureCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.degreeprocedure.ReplaceOpeningRequirementCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation.ApproveProfileCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation.ObserveProfileCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation.ProjectProfileEvaluationNotifier;
import bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation.ProjectProfileEvaluationUseCase;
import bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation.RegisterProfileCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation.RejectProfileCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.tutorassignment.AssignTutorCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.tutorassignment.TutorAssignmentNotifier;
import bo.org.tecnoweb.cpms.procedures.application.tutorassignment.TutorAssignmentUseCase;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring wiring for UC-002, UC-003, UC-004 and their email command handlers. */
@Configuration(proxyBeanMethods = false)
class ProceduresConfiguration {
    /** Creates the UC-003 application service. */
    @Bean
    TutorAssignmentUseCase tutorAssignmentUseCase(UserRepository users, DegreeProcedureRepository procedures,
            DegreeProjectRepository projects, TutorAssignmentNotifier notifier) {
        return new TutorAssignmentUseCase(users, procedures, projects, notifier);
    }

    /** Registers the UC-003 basic flow (FR-003). */
    @Bean
    AssignTutorCommandHandler assignTutorCommandHandler(TutorAssignmentUseCase useCase) {
        return new AssignTutorCommandHandler(useCase);
    }

    /** Creates the UC-004 application service. */
    @Bean
    ProjectProfileEvaluationUseCase projectProfileEvaluationUseCase(UserRepository users,
            DegreeProcedureRepository procedures, DegreeProjectRepository projects,
            ProjectProfileEvaluationNotifier notifier, StoredFileService storedFiles) {
        return new ProjectProfileEvaluationUseCase(users, procedures, projects, notifier, storedFiles);
    }

    /** Registers the UC-004 basic flow (FR-004, FR-023). */
    @Bean
    RegisterProfileCommandHandler registerProfileCommandHandler(ProjectProfileEvaluationUseCase useCase) {
        return new RegisterProfileCommandHandler(useCase);
    }

    /** Registers the UC-004 alternative flow A1 (FR-005). */
    @Bean
    ApproveProfileCommandHandler approveProfileCommandHandler(ProjectProfileEvaluationUseCase useCase) {
        return new ApproveProfileCommandHandler(useCase);
    }

    /** Registers the UC-004 alternative flow A2 (FR-022). */
    @Bean
    ObserveProfileCommandHandler observeProfileCommandHandler(ProjectProfileEvaluationUseCase useCase) {
        return new ObserveProfileCommandHandler(useCase);
    }

    /** Registers the UC-004 alternative flow A3 (FR-022). */
    @Bean
    RejectProfileCommandHandler rejectProfileCommandHandler(ProjectProfileEvaluationUseCase useCase) {
        return new RejectProfileCommandHandler(useCase);
    }

    /** Creates the UC-002 application service. */
    @Bean
    DegreeProcedureUseCase degreeProcedureUseCase(UserRepository users, DegreeProcedureRepository procedures,
            DegreeProcedureNotifier notifier, StoredFileService storedFiles, AcademicCalendar calendar) {
        return new DegreeProcedureUseCase(users, procedures, notifier, storedFiles, calendar);
    }

    /** Registers the FR-001 opening command with the mail channel. */
    @Bean
    OpenDegreeProcedureCommandHandler openDegreeProcedureCommandHandler(DegreeProcedureUseCase useCase) {
        return new OpenDegreeProcedureCommandHandler(useCase);
    }

    /** Registers the FR-055 replacement command with the mail channel. */
    @Bean
    ReplaceOpeningRequirementCommandHandler replaceOpeningRequirementCommandHandler(DegreeProcedureUseCase useCase) {
        return new ReplaceOpeningRequirementCommandHandler(useCase);
    }
}
