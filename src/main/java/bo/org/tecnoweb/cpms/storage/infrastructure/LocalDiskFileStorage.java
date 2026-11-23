package bo.org.tecnoweb.cpms.storage.infrastructure;

import bo.org.tecnoweb.cpms.storage.application.FileStorage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * {@link FileStorage} on a folder of the local disk, set by {@code cpms.storage.root} (issue #9).
 *
 * <p>A file is written to a temporary file in its folder and then moved to its key without
 * replacing, so an existing file is never overwritten and no half-written file is ever visible
 * under a key. If the transaction of the write rolls back, the file is deleted.
 */
@Component
class LocalDiskFileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalDiskFileStorage.class);

    private final Path root;

    /**
     * Creates the adapter and the root folder if it does not exist.
     *
     * @param properties storage settings
     * @throws IllegalStateException if {@code cpms.storage.root} is not set
     * @throws UncheckedIOException  if the root folder cannot be created
     */
    LocalDiskFileStorage(StorageProperties properties) {
        if (properties.root() == null || properties.root().isBlank()) {
            throw new IllegalStateException(
                    "cpms.storage.root is not set: set CPMS_STORAGE_ROOT to the folder where files are kept");
        }
        this.root = Path.of(properties.root()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create the storage root " + root, e);
        }
        log.info("Files are stored in {}", root);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String path, byte[] content) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("A file can only be written inside a transaction");
        }
        Path target = resolve(path);
        try {
            Files.createDirectories(target.getParent());
            Path temporary = Files.createTempFile(target.getParent(), ".", ".tmp");
            try {
                Files.write(temporary, content);
                // No ATOMIC_MOVE: on some platforms it replaces an existing target.
                Files.move(temporary, target);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException("A file already exists under " + path, e);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write the file " + path, e);
        }
        TransactionSynchronizationManager.registerSynchronization(new DeleteOnRollback(target));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] read(String path) {
        try {
            return Files.readAllBytes(resolve(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read the file " + path, e);
        }
    }

    /**
     * Turns a key into a path below the root.
     *
     * @param path the key
     * @return the absolute path of the file
     * @throws IllegalArgumentException if the key points outside the root
     */
    private Path resolve(String path) {
        Path target = root.resolve(path).normalize();
        if (!target.startsWith(root) || target.equals(root)) {
            throw new IllegalArgumentException("The key " + path + " is outside the storage root");
        }
        return target;
    }

    /**
     * Deletes a written file when its transaction rolls back, so no orphan file is left.
     *
     * @param file the written file
     */
    private record DeleteOnRollback(Path file) implements TransactionSynchronization {

        /**
         * Deletes the file if the transaction rolled back. A failure is logged, not rethrown: the
         * transaction is already over.
         *
         * @param status the completion status of the transaction
         */
        @Override
        public void afterCompletion(int status) {
            if (status != STATUS_ROLLED_BACK) {
                return;
            }
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                log.warn("Cannot delete the file {} after a rollback", file, e);
            }
        }
    }
}
