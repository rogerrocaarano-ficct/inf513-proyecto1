package bo.org.tecnoweb.cpms.mail.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.mail.application.ReceivedEmailRepository;
import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
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

    private static ReceivedEmail email(String mailboxId) {
        return ReceivedEmail.builder()
                .withMailboxId(mailboxId)
                .fromSender("ana@uagrm.bo")
                .withSubject("cmd[a; b]")
                .withBody("cuerpo");
    }

    private File storedAttachment(String name, String content) {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        File file = File.builder().withName(name).withContentType("application/pdf").withData(data);
        return file.withStoredFileId(transaction.execute(status -> storedFiles.store(name, "application/pdf", data))
                .getId());
    }

    @Test
    void storesAndReadsAnEmailWithTheReferencesOfItsAttachmentsInOrder() {
        File first = storedAttachment("carta.pdf", "pdf1");
        File second = storedAttachment("cv.pdf", "pdf2");
        ReceivedEmail email = email("uid-1").withAttachments(List.of(first, second));

        repository.store(email);

        ReceivedEmail read = repository.findByMailboxId("uid-1").orElseThrow();
        assertThat(read.getId()).isEqualTo(email.getId());
        assertThat(read.getFrom()).isEqualTo("ana@uagrm.bo");
        assertThat(read.getSubject()).isEqualTo("cmd[a; b]");
        assertThat(read.getBody()).isEqualTo("cuerpo");
        assertThat(read.getState()).isEqualTo(ReceivedEmailState.RECEIVED);
        assertThat(read.getAttachments()).extracting(File::getId).containsExactly(first.getId(), second.getId());
        assertThat(read.getAttachmentIds()).containsExactly(first.getStoredFileId(), second.getStoredFileId());
        assertThat(read.getAttachments()).allSatisfy(a -> assertThat(a.getData()).isNull());
    }

    @Test
    void attachmentTableKeepsNoContent() {
        assertThat(jdbc.queryForList("SELECT column_name FROM information_schema.columns "
                + "WHERE table_name = 'received_email_attachment'", String.class))
                .containsExactlyInAnyOrder("id", "received_email_id", "position", "stored_file_id");
        assertThat(Arrays.stream(ReceivedEmailAttachmentEntity.class.getDeclaredFields()).map(Field::getType))
                .doesNotContain(byte[].class);
    }

    @Test
    void anAttachmentMustBeStoredBeforeTheEmail() {
        File notStored = File.builder().withName("a.pdf").withData(new byte[] {1});

        // Spring's repository translation wraps the IllegalStateException.
        assertThatThrownBy(() -> repository.store(email("uid-1").withAttachments(List.of(notStored))))
                .hasMessageContaining("must be stored before the email");
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
