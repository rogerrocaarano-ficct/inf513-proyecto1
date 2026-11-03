package grupo03sc.mailapp.storage.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.storage.application.FileStorageProperties;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class LocalFileStorageTests {

    @TempDir
    Path root;

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private LocalFileStorage storage() {
        return new LocalFileStorage(new FileStorageProperties(root));
    }

    @Test
    void storesTheFileUnderItsCategoryWithTheExtensionOfItsName() throws Exception {
        String key = storage().store("initial-documents", "Cédula.PDF", bytes("contenido"));

        assertThat(key).matches("initial-documents/[0-9a-f-]{36}\\.pdf");
        assertThat(Files.readString(root.resolve(key))).isEqualTo("contenido");
    }

    @Test
    void deletesAFileStoredInATransactionThatRollsBack() {
        TransactionSynchronizationManager.initSynchronization();
        String key = storage().store("initial-documents", "carta.pdf", bytes("x"));

        complete(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertThat(root.resolve(key)).doesNotExist();
    }

    @Test
    void keepsAFileStoredInATransactionThatCommits() {
        TransactionSynchronizationManager.initSynchronization();
        String key = storage().store("initial-documents", "carta.pdf", bytes("x"));

        complete(TransactionSynchronization.STATUS_COMMITTED);

        assertThat(root.resolve(key)).exists();
    }

    @Test
    void deletesOnlyAfterCommit() {
        LocalFileStorage storage = storage();
        String key = storage.store("initial-documents", "carta.pdf", bytes("x"));
        TransactionSynchronizationManager.initSynchronization();

        storage.delete(key);
        assertThat(root.resolve(key)).exists();
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        assertThat(root.resolve(key)).doesNotExist();
    }

    @Test
    void keepsAFileDeletedInATransactionThatRollsBack() {
        LocalFileStorage storage = storage();
        String key = storage.store("initial-documents", "carta.pdf", bytes("x"));
        TransactionSynchronizationManager.initSynchronization();

        storage.delete(key);
        complete(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertThat(root.resolve(key)).exists();
    }

    @Test
    void rejectsKeysOutsideTheRoot() {
        assertThatThrownBy(() -> storage().delete("../x")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage().delete(root.resolve("x").toString()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage().store("../otra", "a.pdf", bytes("x")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static void complete(int status) {
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(status));
    }

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
