package bo.org.tecnoweb.cpms.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileAlreadyVersionedException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileNotCurrentException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * File storage end to end, with the real database and disk: one test per scenario of the
 * {@code file-storage} spec (issue #9).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StoredFileServiceIntegrationTest {

    @Autowired
    private StoredFileService service;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private JdbcTemplate jdbc;

    @Value("${cpms.storage.root}")
    private Path root;

    @BeforeEach
    void clean() {
        // Other tests sharing this context may leave owners of stored files behind (issue #9).
        jdbc.update("DELETE FROM received_email_attachment");
        jdbc.update("DELETE FROM degree_project");
        jdbc.update("DELETE FROM degree_procedure_requirement");
        jdbc.update("DELETE FROM stored_file");
    }

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private StoredFile store(String content) {
        return transaction.execute(status -> service.store("perfil.pdf", "application/pdf", bytes(content)));
    }

    private StoredFile newVersion(UUID previousId, String content) {
        return transaction.execute(status -> service.storeNewVersion(previousId, "perfil.pdf", "application/pdf",
                bytes(content)));
    }

    private long filesOnDisk() throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile).count();
        }
    }

    private int rows() {
        return jdbc.queryForObject("SELECT count(*) FROM stored_file", Integer.class);
    }

    @Test
    void storesTheBytesOnDiskAndOnlyTheMetadataInTheDatabase() {
        StoredFile file = store("contenido");

        assertThat(root.resolve(file.getPath())).hasBinaryContent(bytes("contenido"));
        assertThat(file.getPath()).matches("\\d{4}/\\d{2}/" + file.getId());
        assertThat(jdbc.queryForMap("SELECT original_name, content_type, size FROM stored_file WHERE id = ?",
                file.getId())).containsEntry("original_name", "perfil.pdf")
                .containsEntry("content_type", "application/pdf").containsEntry("size", 9L);
    }

    @Test
    void invalidFileWritesNothing() throws IOException {
        long before = filesOnDisk();

        assertThatThrownBy(() -> transaction.execute(status -> service.store("", "application/pdf", new byte[0])))
                .isInstanceOf(DomainValidationException.class);

        assertThat(filesOnDisk()).isEqualTo(before);
        assertThat(rows()).isZero();
    }

    @Test
    void readsTheSameBytes() {
        StoredFile file = store("contenido");

        assertThat(service.readContent(file.getId())).isEqualTo(bytes("contenido"));
    }

    @Test
    void missingIdIsRejected() {
        assertThatThrownBy(() -> service.readContent(UUID.randomUUID()))
                .isInstanceOf(StoredFileNotFoundException.class).hasMessage("El archivo solicitado no existe.");
        assertThatThrownBy(() -> newVersion(UUID.randomUUID(), "x"))
                .isInstanceOf(StoredFileNotFoundException.class);
    }

    @Test
    void newVersionKeepsThePreviousOne() {
        StoredFile first = store("v1");

        StoredFile second = newVersion(first.getId(), "v2");

        assertThat(service.readContent(first.getId())).isEqualTo(bytes("v1"));
        assertThat(service.readContent(second.getId())).isEqualTo(bytes("v2"));
        assertThat(service.getHistory(first.getId())).extracting(StoredFile::getId, StoredFile::isCurrent)
                .containsExactly(tuple(first.getId(), false),
                        tuple(second.getId(), true));
    }

    @Test
    void anOldVersionCannotBeVersioned() throws IOException {
        StoredFile first = store("v1");
        newVersion(first.getId(), "v2");
        long before = filesOnDisk();

        assertThatThrownBy(() -> newVersion(first.getId(), "v3")).isInstanceOf(StoredFileNotCurrentException.class)
                .hasMessage("Solo se puede crear una versión nueva a partir de la versión vigente del archivo.");

        assertThat(filesOnDisk()).isEqualTo(before);
        assertThat(rows()).isEqualTo(2);
    }

    @Test
    void historyFromAnIntermediateVersion() {
        StoredFile first = store("v1");
        StoredFile second = newVersion(first.getId(), "v2");
        StoredFile third = newVersion(second.getId(), "v3");

        List<StoredFile> history = service.getHistory(second.getId());

        assertThat(history).extracting(StoredFile::getId).containsExactly(first.getId(), second.getId(),
                third.getId());
        assertThat(history).filteredOn(StoredFile::isCurrent).extracting(StoredFile::getId)
                .containsExactly(third.getId());
    }

    @Test
    void historyOfAFileNeverVersioned() {
        StoredFile file = store("v1");

        assertThat(service.getHistory(file.getId())).extracting(StoredFile::getId, StoredFile::isCurrent)
                .containsExactly(tuple(file.getId(), true));
    }

    @Test
    void twoSimultaneousVersionsDoNotForkTheChain() throws Exception {
        StoredFile first = store("v1");
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        CompletableFuture<StoredFile> winner = CompletableFuture.supplyAsync(() -> transaction.execute(status -> {
            StoredFile next = service.storeNewVersion(first.getId(), "a.pdf", "application/pdf", bytes("a"));
            locked.countDown();
            await(release);
            return next;
        }));
        assertThat(locked.await(30, TimeUnit.SECONDS)).isTrue();
        CompletableFuture<StoredFile> loser = CompletableFuture.supplyAsync(() -> newVersion(first.getId(), "b"));
        Thread.sleep(500); // let the second request reach the lock
        release.countDown();

        StoredFile next = winner.get(30, TimeUnit.SECONDS);
        assertThatThrownBy(() -> loser.get(30, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(StoredFileNotCurrentException.class);
        assertThat(service.getHistory(first.getId())).extracting(StoredFile::getId)
                .containsExactly(first.getId(), next.getId());
    }

    @Test
    void rollbackAfterStoringLeavesNothing() throws IOException {
        long before = filesOnDisk();

        transaction.executeWithoutResult(status -> {
            service.store("perfil.pdf", "application/pdf", bytes("x"));
            status.setRollbackOnly();
        });

        assertThat(filesOnDisk()).isEqualTo(before);
        assertThat(rows()).isZero();
    }

    @Test
    void rollbackWhileVersioningKeepsThePreviousVersionCurrent() {
        StoredFile first = store("v1");
        Path[] written = new Path[1];

        transaction.executeWithoutResult(status -> {
            written[0] = root.resolve(service.storeNewVersion(first.getId(), "perfil.pdf", "application/pdf",
                    bytes("v2")).getPath());
            status.setRollbackOnly();
        });

        assertThat(written[0]).doesNotExist();
        assertThat(service.getHistory(first.getId())).extracting(StoredFile::getId, StoredFile::isCurrent)
                .containsExactly(tuple(first.getId(), true));
        assertThat(service.readContent(first.getId())).isEqualTo(bytes("v1"));
    }

    private StoredFile append(UUID previousId, UUID nextId) {
        return transaction.execute(status -> service.appendVersion(previousId, nextId));
    }

    @Test
    void appendsAStoredFileAsTheNextVersionWithoutWriting() throws IOException {
        StoredFile first = store("v1");
        StoredFile received = store("v2");
        long before = filesOnDisk();

        append(first.getId(), received.getId());

        assertThat(filesOnDisk()).isEqualTo(before);
        assertThat(service.getHistory(received.getId())).extracting(StoredFile::getId, StoredFile::isCurrent)
                .containsExactly(tuple(first.getId(), false), tuple(received.getId(), true));
        assertThat(service.getHistory(first.getId())).extracting(StoredFile::getId)
                .containsExactly(first.getId(), received.getId());
        assertThat(service.readContent(first.getId())).isEqualTo(bytes("v1"));
    }

    @Test
    void appendToAnOldVersionIsRejected() {
        StoredFile first = store("v1");
        newVersion(first.getId(), "v2");
        StoredFile received = store("x");

        assertThatThrownBy(() -> append(first.getId(), received.getId()))
                .isInstanceOf(StoredFileNotCurrentException.class)
                .hasMessage("Solo se puede crear una versión nueva a partir de la versión vigente del archivo.");
        assertThat(service.get(received.getId()).getFirstVersionId()).isEqualTo(received.getId());
    }

    @Test
    void appendOfAFileThatAlreadyBelongsToAHistoryIsRejected() {
        StoredFile first = store("v1");
        StoredFile second = newVersion(first.getId(), "v2");
        StoredFile current = store("c");

        assertThatThrownBy(() -> append(current.getId(), second.getId()))
                .isInstanceOf(StoredFileAlreadyVersionedException.class)
                .hasMessage("El archivo ya forma parte del historial de otro archivo.");
        assertThatThrownBy(() -> append(second.getId(), first.getId()))
                .isInstanceOf(StoredFileAlreadyVersionedException.class);
        assertThatThrownBy(() -> append(current.getId(), current.getId()))
                .isInstanceOf(StoredFileAlreadyVersionedException.class);
        assertThatThrownBy(() -> append(current.getId(), UUID.randomUUID()))
                .isInstanceOf(StoredFileNotFoundException.class);
        assertThat(service.getHistory(current.getId())).extracting(StoredFile::getId)
                .containsExactly(current.getId());
    }

    @Test
    void twoSimultaneousAppendsDoNotForkTheChain() throws Exception {
        StoredFile first = store("v1");
        StoredFile a = store("a");
        StoredFile b = store("b");
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        CompletableFuture<StoredFile> winner = CompletableFuture.supplyAsync(() -> transaction.execute(status -> {
            StoredFile next = service.appendVersion(first.getId(), a.getId());
            locked.countDown();
            await(release);
            return next;
        }));
        assertThat(locked.await(30, TimeUnit.SECONDS)).isTrue();
        CompletableFuture<StoredFile> loser = CompletableFuture.supplyAsync(() -> append(first.getId(), b.getId()));
        Thread.sleep(500); // let the second request reach the lock
        release.countDown();

        winner.get(30, TimeUnit.SECONDS);
        assertThatThrownBy(() -> loser.get(30, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(StoredFileNotCurrentException.class);
        assertThat(service.getHistory(first.getId())).extracting(StoredFile::getId)
                .containsExactly(first.getId(), a.getId());
        assertThat(service.get(b.getId()).isCurrent()).isTrue();
    }

    @Test
    void getReturnsTheData() {
        StoredFile file = store("contenido");

        StoredFile found = service.get(file.getId());

        assertThat(found.getOriginalName()).isEqualTo("perfil.pdf");
        assertThat(found.getContentType()).isEqualTo("application/pdf");
        assertThat(found.getSize()).isEqualTo(9);
        assertThat(found.getStoredAt()).isEqualTo(file.getStoredAt());
        assertThat(found.isCurrent()).isTrue();
        assertThatThrownBy(() -> service.get(UUID.randomUUID())).isInstanceOf(StoredFileNotFoundException.class)
                .hasMessage("El archivo solicitado no existe.");
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
