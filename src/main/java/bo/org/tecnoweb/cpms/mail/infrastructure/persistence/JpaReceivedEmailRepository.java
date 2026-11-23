package bo.org.tecnoweb.cpms.mail.infrastructure.persistence;

import bo.org.tecnoweb.cpms.mail.application.ReceivedEmailRepository;
import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter of {@link ReceivedEmailRepository}.
 *
 * <p>Maps the domain classes to their own JPA entities, so the domain has no persistence
 * annotations. The unique {@code mailbox_id} column backs the rule that a message is stored once.
 */
@Repository
class JpaReceivedEmailRepository implements ReceivedEmailRepository {

    private static final EnumSet<ReceivedEmailState> NOT_PROCESSED =
            EnumSet.of(ReceivedEmailState.RECEIVED, ReceivedEmailState.VALIDATED);

    private final SpringDataReceivedEmailRepository emails;

    /**
     * Creates the adapter.
     *
     * @param emails Spring Data repository of the entities
     */
    JpaReceivedEmailRepository(SpringDataReceivedEmailRepository emails) {
        this.emails = emails;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Joins the caller's transaction if there is one. Attachments are written only when the email
     * is inserted, since they never change, and only as references to their stored files: their
     * content is never written to the database (issue #9).
     *
     * @throws IllegalStateException if a new email has an attachment that was not stored first
     */
    @Override
    @Transactional
    public void store(ReceivedEmail email) {
        Optional<ReceivedEmailEntity> existing = emails.findById(email.getId());
        if (existing.isPresent()) {
            ReceivedEmailEntity entity = existing.get();
            copyFields(email, entity);
            return;
        }
        if (emails.existsByMailboxId(email.getMailboxId())) {
            return;
        }
        ReceivedEmailEntity entity = new ReceivedEmailEntity(email.getId());
        copyFields(email, entity);
        for (File file : email.getAttachments()) {
            if (file.getStoredFileId() == null) {
                throw new IllegalStateException("Attachment " + file.getId() + " of email " + email.getId()
                        + " must be stored before the email");
            }
            entity.getAttachments().add(new ReceivedEmailAttachmentEntity(file.getId(), file.getStoredFileId()));
        }
        emails.save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReceivedEmail> getNotProcessed() {
        return emails.findByStateInOrderByReceivedAtAscIdAsc(NOT_PROCESSED).stream()
                .map(JpaReceivedEmailRepository::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ReceivedEmail> findByMailboxId(String mailboxId) {
        return emails.findByMailboxId(mailboxId).map(JpaReceivedEmailRepository::toDomain);
    }

    /**
     * Copies the email fields to the entity. Attachments are not copied: they never change once
     * stored.
     *
     * @param email  source
     * @param entity target
     */
    private static void copyFields(ReceivedEmail email, ReceivedEmailEntity entity) {
        entity.setMailboxId(email.getMailboxId());
        entity.setFromAddress(email.getFrom());
        entity.setSubject(email.getSubject());
        entity.setBody(email.getBody());
        entity.setState(email.getState());
    }

    /**
     * Rebuilds the domain email from its entity.
     *
     * @param entity the stored email
     * @return the email with its state and attachments
     */
    private static ReceivedEmail toDomain(ReceivedEmailEntity entity) {
        List<File> attachments = entity.getAttachments().stream()
                .map(a -> File.rehydrate().withId(a.getId()).withStoredFileId(a.getStoredFileId()))
                .toList();
        return ReceivedEmail.rehydrate()
                .withId(entity.getId())
                .withMailboxId(entity.getMailboxId())
                .fromSender(entity.getFromAddress())
                .withSubject(entity.getSubject())
                .withBody(entity.getBody())
                .withState(entity.getState())
                .withAttachments(attachments);
    }
}
