package bo.org.tecnoweb.cpms.storage.infrastructure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Empties the folder of the stored files at every startup of the disposable environments, together with the
 * database (capabilities {@code local-environment} and {@code test-environment}, requirements "Entorno local
 * limpio en cada arranque" and "Entorno limpio en cada arranque").
 *
 * <p>Active only with the {@code local} and {@code staging} profiles, whose database is cleaned at startup
 * by {@code DisposableDatabaseReset}: a file kept without its metadata would be an orphan. It runs while the
 * application context starts, before the mail channel reads the mailbox, so nothing is written meanwhile.
 * Only the content of {@code cpms.storage.root} is deleted, never the folder itself.
 */
@Component
@Profile({"local", "staging"})
class DisposableStorageReset {

    private static final Logger log = LoggerFactory.getLogger(DisposableStorageReset.class);

    /**
     * Empties the storage folder.
     *
     * @param properties storage settings; nothing is done when the folder is not set or does not exist
     * @throws UncheckedIOException if a file cannot be deleted
     */
    DisposableStorageReset(StorageProperties properties) {
        if (properties.root() == null || properties.root().isBlank()) {
            return;
        }
        Path root = Path.of(properties.root()).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            return;
        }
        int deleted = empty(root);
        log.info("Emptied the storage folder {} ({} entries deleted)", root, deleted);
    }

    /**
     * Deletes every file and folder below {@code root}, deepest first.
     *
     * @param root folder to empty
     * @return number of files and folders deleted
     */
    static int empty(Path root) {
        List<Path> entries;
        try (Stream<Path> walk = Files.walk(root)) {
            entries = walk.filter(path -> !path.equals(root))
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot list the storage folder " + root, e);
        }
        for (Path entry : entries) {
            try {
                Files.delete(entry);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot delete " + entry, e);
            }
        }
        return entries.size();
    }
}
