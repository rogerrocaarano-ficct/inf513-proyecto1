package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureNotifier;
import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureUseCase;
import bo.org.tecnoweb.cpms.procedures.application.OpenDegreeProcedureCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.ReplaceInitialDocumentCommandHandler;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.users.application.UserManagementNotifier;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring wiring for UC-002 and its two email command handlers. */
@Configuration(proxyBeanMethods = false)
class ProceduresConfiguration {
    /** Creates the UC-002 application service. */
    @Bean
    DegreeProcedureUseCase degreeProcedureUseCase(UserRepository users, DegreeProcedureRepository procedures,
            UserManagementNotifier userNotifier, DegreeProcedureNotifier notifier) {
        return new DegreeProcedureUseCase(users, procedures, userNotifier, notifier);
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
