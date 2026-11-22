package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureUseCase;
import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureUseCase.DocumentReplaced;
import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureUseCase.Opened;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.event.TransactionalEventListener;

@Configuration(proxyBeanMethods = false)
class ProceduresConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ProceduresConfiguration.class);
    private final EmailSender outbox;

    ProceduresConfiguration(EmailSender outbox) {
        this.outbox = outbox;
    }

    @Bean
    DegreeProcedureUseCase degreeProcedureUseCase(UserRepository users, DegreeProcedureRepository procedures,
            ApplicationEventPublisher events) {
        return new DegreeProcedureUseCase(users, procedures, events::publishEvent, events::publishEvent);
    }

    @Bean
    EmailCommandHandler openDegreeProcedure(DegreeProcedureUseCase useCase) {
        return new ProcedureCommand("tramites.abrirTramite", 7, 4, useCase::open);
    }

    @Bean
    EmailCommandHandler replaceInitialDocument(DegreeProcedureUseCase useCase) {
        return new ProcedureCommand("tramites.reemplazarDocumento", 1, 1, useCase::replace);
    }

    @TransactionalEventListener
    void onOpened(Opened event) {
        send(event.requester(), "Trámite abierto: " + event.reference(),
                "Se abrió el trámite de titulación de " + event.studentName() + ".\n"
                        + "Referencia: " + event.reference() + "\nDocumentos iniciales registrados: 4.");
        if (event.created()) {
            send(event.student(), "Su cuenta en el sistema de trámites de titulación",
                    "Hola, " + event.studentName() + ":\n\n"
                            + "Se creó su cuenta con rol estudiante. Puede enviar comandos desde esta dirección.");
        }
        send(event.student(), "Apertura de trámite: " + event.reference(),
                "Su trámite de titulación se abrió con la referencia " + event.reference() + ".");
    }

    @TransactionalEventListener
    void onReplaced(DocumentReplaced event) {
        send(event.requester(), "Documento inicial reemplazado: " + event.reference(),
                "Se reemplazó el documento " + event.documentLabel() + " en el expediente "
                        + event.reference() + ".");
    }

    private void send(String to, String subject, String body) {
        try {
            outbox.send(to, subject, body, List.of());
        } catch (RuntimeException exception) {
            log.warn("Could not send procedure notification: {}", subject, exception);
        }
    }

    private record ProcedureCommand(String actionKey, int requiredArgs, int requiredFiles,
            Consumer<ReceivedEmail> action) implements EmailCommandHandler {
        @Override
        public void handle(ReceivedEmail email) {
            action.accept(email);
        }
    }
}
