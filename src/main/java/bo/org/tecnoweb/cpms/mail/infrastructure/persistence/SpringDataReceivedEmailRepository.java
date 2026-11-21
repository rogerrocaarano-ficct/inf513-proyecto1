package bo.org.tecnoweb.cpms.mail.infrastructure.persistence;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository of {@link ReceivedEmailEntity}, used only by {@link JpaReceivedEmailRepository}.
 */
interface SpringDataReceivedEmailRepository extends JpaRepository<ReceivedEmailEntity, UUID> {

    /**
     * Finds the email of a message.
     *
     * @param mailboxId the UIDL of the message
     * @return the entity, or empty if the message was never stored
     */
    Optional<ReceivedEmailEntity> findByMailboxId(String mailboxId);

    /**
     * Tells whether a message is already stored.
     *
     * @param mailboxId the UIDL of the message
     * @return {@code true} if an email with that UIDL exists
     */
    boolean existsByMailboxId(String mailboxId);

    /**
     * Finds the emails in some states, oldest first.
     *
     * @param states the states to include
     * @return the entities ordered by arrival time, then by identifier
     */
    List<ReceivedEmailEntity> findByStateInOrderByReceivedAtAscIdAsc(Collection<ReceivedEmailState> states);
}
