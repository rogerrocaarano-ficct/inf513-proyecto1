package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.procedures.application.AcademicCalendar;
import bo.org.tecnoweb.cpms.procedures.application.AssignTutorCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureNotifier;
import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureUseCase;
import bo.org.tecnoweb.cpms.procedures.application.OpenDegreeProcedureCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.RegisterInitialProfileCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.ReplaceInitialDocumentCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.TutorAssignmentNotifier;
import bo.org.tecnoweb.cpms.procedures.application.TutorAssignmentUseCase;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring wiring for UC-002, UC-003 and their email command handlers. */
@Configuration(proxyBeanMethods = false)
class ProceduresConfiguration {
    /** Creates the UC-003 application service. */
    @Bean
    TutorAssignmentUseCase tutorAssignmentUseCase(UserRepository users, DegreeProcedureRepository procedures,
            DegreeProjectRepository projects, TutorAssignmentNotifier notifier, StoredFileService storedFiles) {
        return new TutorAssignmentUseCase(users, procedures, projects, notifier, storedFiles);
    }

    /** Registers the UC-003 basic flow (FR-003). */
    @Bean
    AssignTutorCommandHandler assignTutorCommandHandler(TutorAssignmentUseCase useCase) {
        return new AssignTutorCommandHandler(useCase);
    }

    /** Registers the UC-003 alternative flow A1 (FR-004). */
    @Bean
    RegisterInitialProfileCommandHandler registerInitialProfileCommandHandler(TutorAssignmentUseCase useCase) {
        return new RegisterInitialProfileCommandHandler(useCase);
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
    ReplaceInitialDocumentCommandHandler replaceInitialDocumentCommandHandler(DegreeProcedureUseCase useCase) {
        return new ReplaceInitialDocumentCommandHandler(useCase);
    }
}
