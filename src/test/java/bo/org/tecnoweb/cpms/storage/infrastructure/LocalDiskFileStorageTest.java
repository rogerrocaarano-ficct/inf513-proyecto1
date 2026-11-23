package bo.org.tecnoweb.cpms.storage.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

class LocalDiskFileStorageTest {

    @TempDir
    private Path root;

    private LocalDiskFileStorage storage;

    private final TransactionTemplate transaction = new TransactionTemplate(new NoResourceTransactionManager());

    @BeforeEach
    void setUp() {
        storage = new LocalDiskFileStorage(new StorageProperties(root.toString()));
    }

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void writesAndReadsUnderTheKey() {
        transaction.executeWithoutResult(status -> storage.write("2026/11/abc", bytes("contenido")));

        assertThat(root.resolve("2026/11/abc")).hasBinaryContent(bytes("contenido"));
        assertThat(storage.read("2026/11/abc")).isEqualTo(bytes("contenido"));
        assertThat(root.resolve("2026/11")).isDirectoryContaining(p -> p.getFileName().toString().equals("abc"))
                .isDirectoryNotContaining("glob:**.tmp");
    }

    @Test
    void neverOverwritesAnExistingFile() {
        transaction.executeWithoutResult(status -> storage.write("2026/11/abc", bytes("original")));

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> storage.write("2026/11/abc",
                bytes("otro")))).isInstanceOf(IllegalStateException.class);
        assertThat(root.resolve("2026/11/abc")).hasBinaryContent(bytes("original"));
    }

    @Test
    void rejectsKeysOutsideTheRoot() {
        assertThatThrownBy(() -> storage.read("../fuera")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> storage.write("../../fuera",
                bytes("x")))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesToWriteWithoutATransaction() {
        assertThatThrownBy(() -> storage.write("2026/11/abc", bytes("x"))).isInstanceOf(IllegalStateException.class);
        assertThat(root.resolve("2026/11/abc")).doesNotExist();
    }

    @Test
    void deletesTheFileWhenTheTransactionRollsBack() {
        transaction.executeWithoutResult(status -> {
            storage.write("2026/11/abc", bytes("x"));
            status.setRollbackOnly();
        });

        assertThat(root.resolve("2026/11/abc")).doesNotExist();
    }

    @Test
    void keepsTheFileWhenTheTransactionCommits() {
        transaction.executeWithoutResult(status -> storage.write("2026/11/abc", bytes("x")));

        assertThat(Files.exists(root.resolve("2026/11/abc"))).isTrue();
    }

    @Test
    void doesNotStartWithoutARoot() {
        assertThatThrownBy(() -> new LocalDiskFileStorage(new StorageProperties(" ")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("CPMS_STORAGE_ROOT");
    }

    /** Transaction manager without a resource: it only drives the transaction synchronizations. */
    private static final class NoResourceTransactionManager extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
        }
    }
}
