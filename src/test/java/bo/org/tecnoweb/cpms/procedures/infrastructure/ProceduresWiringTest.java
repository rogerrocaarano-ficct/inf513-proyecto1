package bo.org.tecnoweb.cpms.procedures.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.application.AssignTutorCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.OpenDegreeProcedureCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.RegisterInitialProfileCommandHandler;
import bo.org.tecnoweb.cpms.procedures.application.ReplaceInitialDocumentCommandHandler;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.users.application.UserManagementNotifier;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** Checks UC-002 and UC-003 bean discovery without a database or mail server. */
class ProceduresWiringTest {
    /** All procedure commands are registered as distinct beans for the mail channel. */
    @Test
    void exposesProcedureEmailHandlers() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(UserRepository.class, () -> mock(UserRepository.class));
            context.registerBean(UserManagementNotifier.class, () -> mock(UserManagementNotifier.class));
            context.registerBean(DegreeProcedureRepository.class, () -> mock(DegreeProcedureRepository.class));
            context.registerBean(DegreeProjectRepository.class, () -> mock(DegreeProjectRepository.class));
            context.registerBean(EmailSender.class, () -> mock(EmailSender.class));
            context.registerBean(StoredFileService.class, () -> mock(StoredFileService.class));
            context.register(ProceduresConfiguration.class, MailDegreeProcedureNotifier.class,
                    MailTutorAssignmentNotifier.class);
            context.refresh();

            assertThat(context.getBeansOfType(EmailCommandHandler.class).values())
                    .extracting(EmailCommandHandler::actionKey)
                    .containsExactlyInAnyOrder(OpenDegreeProcedureCommandHandler.ACTION,
                            ReplaceInitialDocumentCommandHandler.ACTION, RegisterInitialProfileCommandHandler.ACTION,
                            AssignTutorCommandHandler.ACTION);
        }
    }
}
