package bo.org.tecnoweb.cpms.mail.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.mail.application.ReceivedEmailRepository;
import bo.org.tecnoweb.cpms.mail.domain.Attachment;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class JpaReceivedEmailRepositoryTest {

    @Autowired
    private ReceivedEmailRepository repository;

    @Autowired
    private SpringDataReceivedEmailRepository springData;

    @Autowired
    private StoredFileService storedFiles;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        springData.deleteAll();
    }

    /** An email whose attachments the channel already stored. */
    private static ReceivedEmail email(String mailboxId) {
        return ReceivedEmail.builder()
                .withMailboxId(mailboxId)
                .fromSender("ana@uagrm.bo")
                .withSubject("cmd[a; b]")
                .withBody("cuerpo")
                .withState(ReceivedEmailState.RECEIVED);
    }

    private UUID storedFile(String name, String content) {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        return transaction.execute(status -> storedFiles.store(name, "application/pdf", data)).getId();
    }

    @Test
    void storesAndReadsAnEmailWithTheReferencesOfItsAttachmentsInOrder() {
        UUID first = storedFile("carta.pdf", "pdf1");
        UUID second = storedFile("cv.pdf", "pdf2");
        ReceivedEmail email = email("uid-1").withAttachments(List.of(new Attachment.Stored(first),
                new Attachment.Stored(second)));

        repository.store(email);

        ReceivedEmail read = repository.findByMailboxId("uid-1").orElseThrow();
        assertThat(read.getId()).isEqualTo(email.getId());
        assertThat(read.getFrom()).isEqualTo("ana@uagrm.bo");
        assertThat(read.getSubject()).isEqualTo("cmd[a; b]");
        assertThat(read.getBody()).isEqualTo("cuerpo");
        assertThat(read.getState()).isEqualTo(ReceivedEmailState.RECEIVED);
        assertThat(read.getAttachments()).containsExactly(new Attachment.Stored(first), new Attachment.Stored(second));
        assertThat(read.getAttachmentIds()).containsExactly(first, second);
    }

    @Test
    void attachmentTableKeepsOnlyTheReferenceInItsPosition() {
        assertThat(jdbc.queryForList("SELECT column_name FROM information_schema.columns "
                + "WHERE table_name = 'received_email_attachment'", String.class))
                .containsExactlyInAnyOrder("received_email_id", "position", "stored_file_id");
        assertThat(jdbc.queryForList("""
                SELECT k.column_name FROM information_schema.table_constraints c
                JOIN information_schema.key_column_usage k ON k.constraint_name = c.constraint_name
                WHERE c.table_name = 'received_email_attachment' AND c.constraint_type = 'PRIMARY KEY'
                ORDER BY k.ordinal_position""", String.class))
                .containsExactly("received_email_id", "position");
    }

    @Test
    void deletingAnEmailDeletesTheReferencesOfItsAttachments() {
        repository.store(email("uid-1").withAttachments(List.of(new Attachment.Stored(storedFile("a.pdf", "x")))));

        jdbc.update("DELETE FROM received_email");

        assertThat(jdbc.queryForObject("SELECT count(*) FROM received_email_attachment", Integer.class)).isZero();
    }

    @Test
    void aFetchedEmailCannotBeStored() {
        ReceivedEmail fetched = ReceivedEmail.builder().withMailboxId("uid-1")
                .withAttachments(List.of(new Attachment.Content("a.pdf", "application/pdf", new byte[] {1})));

        // Spring's repository translation wraps the IllegalStateException.
        assertThatThrownBy(() -> repository.store(fetched)).hasMessageContaining("must store its attachments");
        assertThat(springData.count()).isZero();
    }

    @Test
    void doesNotStoreTheSameMailboxIdTwice() {
        ReceivedEmail first = email("uid-1");
        ReceivedEmail sameMessageReadAgain = email("uid-1");

        repository.store(first);
        repository.store(sameMessageReadAgain);

        assertThat(springData.count()).isEqualTo(1);
        assertThat(repository.findByMailboxId("uid-1").orElseThrow().getId()).isEqualTo(first.getId());
    }

    @Test
    void storingAgainSavesTheNewState() {
        ReceivedEmail email = email("uid-1");
        repository.store(email);

        email.markAsDiscarded();
        repository.store(email);

        assertThat(repository.findByMailboxId("uid-1").orElseThrow().getState())
                .isEqualTo(ReceivedEmailState.DISCARDED);
    }

    @Test
    void findByMailboxIdReturnsNothingForAnUnknownMessage() {
        assertThat(repository.findByMailboxId("missing")).isEmpty();
    }

    @Test
    void notProcessedReturnsOnlyReceivedAndValidatedInArrivalOrder() {
        ReceivedEmail received = email("uid-1");
        ReceivedEmail validated = email("uid-2").withState(ReceivedEmailState.VALIDATED);
        ReceivedEmail processed = email("uid-3").withState(ReceivedEmailState.PROCESSED);
        ReceivedEmail discarded = email("uid-4").withState(ReceivedEmailState.DISCARDED);
        repository.store(received);
        repository.store(validated);
        repository.store(processed);
        repository.store(discarded);

        assertThat(repository.getNotProcessed())
                .extracting(ReceivedEmail::getMailboxId)
                .containsExactly("uid-1", "uid-2");
    }
}
