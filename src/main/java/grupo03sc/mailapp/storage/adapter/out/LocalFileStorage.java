package grupo03sc.mailapp.storage.adapter.out;

import grupo03sc.mailapp.storage.application.FileStorage;
import grupo03sc.mailapp.storage.application.FileStorageProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Stores files in the local file system (AD-012). A file written in a transaction that rolls back
 * is deleted, and a file deleted in a transaction is only removed after commit, so a rejected
 * command leaves no stray files and a replacement that is undone keeps the previous file.
 */
@Component
class LocalFileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorage.class);
    private static final Pattern CATEGORY = Pattern.compile("[a-z0-9-]+");
    private static final Pattern EXTENSION = Pattern.compile("[a-z0-9]{1,10}");

    private final Path root;

    LocalFileStorage(FileStorageProperties properties) {
        this.root = properties.root().toAbsolutePath().normalize();
    }

    @Override
    public String store(String category, String fileName, byte[] content) {
        if (!CATEGORY.matcher(category).matches()) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
        String key = category + "/" + UUID.randomUUID() + extension(fileName);
        Path file = resolve(key);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, content, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store " + key, e);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        deleteQuietly(file);
                    }
                }
            });
        }
        return key;
    }

    @Override
    public void delete(String storageKey) {
        Path file = resolve(storageKey);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteQuietly(file);
                }
            });
        } else {
            deleteQuietly(file);
        }
    }

    /** The file of a key, which must be relative and stay inside the root. */
    Path resolve(String storageKey) {
        Path relative = Path.of(storageKey);
        Path file = root.resolve(relative).normalize();
        if (relative.isAbsolute() || !file.startsWith(root) || file.equals(root)) {
            throw new IllegalArgumentException("Storage key outside the root: " + storageKey);
        }
        return file;
    }

    private static String extension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return EXTENSION.matcher(extension).matches() ? "." + extension : "";
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException | RuntimeException e) {
            log.error("Could not delete the stored file {}", file, e);
        }
    }
}
