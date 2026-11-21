package bo.org.tecnoweb.cpms.mail.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.mail.application.ReceivedEmailRepository;
import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class JpaReceivedEmailRepositoryTest {

    @Autowired
    private ReceivedEmailRepository repository;

    @Autowired
    private SpringDataReceivedEmailRepository springData;

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

    @Test
    void storesAndReadsAnEmailWithItsAttachments() {
        File file = File.builder().withName("perfil.pdf").withData("pdf".getBytes(StandardCharsets.UTF_8));
        ReceivedEmail email = email("uid-1").withAttachments(List.of(file));

        repository.store(email);

        ReceivedEmail read = repository.findByMailboxId("uid-1").orElseThrow();
        assertThat(read.getId()).isEqualTo(email.getId());
        assertThat(read.getFrom()).isEqualTo("ana@uagrm.bo");
        assertThat(read.getSubject()).isEqualTo("cmd[a; b]");
        assertThat(read.getBody()).isEqualTo("cuerpo");
        assertThat(read.getState()).isEqualTo(ReceivedEmailState.RECEIVED);
        assertThat(read.getAttachments()).singleElement().satisfies(a -> {
            assertThat(a.getId()).isEqualTo(file.getId());
            assertThat(a.getName()).isEqualTo("perfil.pdf");
            assertThat(a.getData()).isEqualTo("pdf".getBytes(StandardCharsets.UTF_8));
        });
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
