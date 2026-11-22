package bo.org.tecnoweb.cpms.mail.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.application.EmailInbox;
import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.mail.application.InboxPollerService;
import bo.org.tecnoweb.cpms.mail.application.ReceivedEmailRepository;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Spring wiring of the mail channel.
 *
 * <p>Every {@link EmailCommandHandler} bean of any subsystem is registered in the
 * {@link InboxPollerService}, so a use case joins the channel just by declaring its handler.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MailProperties.class)
class MailConfiguration {

    /**
     * Creates the polling service.
     *
     * @param inbox              POP3 adapter
     * @param handlers           every command handler bean
     * @param receivedEmails     JPA adapter of the received emails
     * @param outbox             SMTP adapter
     * @param properties         mail settings, for the batch size
     * @param transactionManager transaction manager of the database
     * @param storedFiles        file storage, where the attachments are kept (issue #9)
     * @return the polling service
     */
    @Bean
    InboxPollerService inboxPollerService(EmailInbox inbox, ObjectProvider<EmailCommandHandler> handlers,
            ReceivedEmailRepository receivedEmails, EmailSender outbox, MailProperties properties,
            PlatformTransactionManager transactionManager, StoredFileService storedFiles) {
        return new InboxPollerService(inbox, handlers.orderedStream().toList(), properties.batchSize(),
                receivedEmails, outbox, new TransactionTemplate(transactionManager), storedFiles);
    }

    /**
     * Schedules the polling cycles.
     *
     * <p>A cycle starts {@code cpms.mail.poll-interval} after the previous one ends, so two cycles
     * never overlap. Disabled with {@code cpms.mail.polling-enabled=false}.
     */
    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnProperty(prefix = "cpms.mail", name = "polling-enabled", havingValue = "true", matchIfMissing = true)
    static class Polling implements SchedulingConfigurer {

        private final InboxPollerService service;
        private final MailProperties properties;

        /**
         * Creates the scheduler.
         *
         * @param service    the polling service to run
         * @param properties mail settings, for the interval
         */
        Polling(InboxPollerService service, MailProperties properties) {
            this.service = service;
            this.properties = properties;
        }

        /**
         * Registers {@link InboxPollerService#pollAndProcess()} as a fixed-delay task.
         *
         * @param registrar Spring's task registrar
         */
        @Override
        public void configureTasks(ScheduledTaskRegistrar registrar) {
            registrar.addFixedDelayTask(service::pollAndProcess, properties.pollInterval());
        }
    }
}
