package bo.org.tecnoweb.cpms.storage.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port of {@link StoredFile}: it keeps metadata only, never the bytes (issue #9).
 */
public interface StoredFileRepository {

    /**
     * Saves a new file.
     *
     * @param file the stored file
     */
    void insert(StoredFile file);

    /**
     * Saves the changes of an existing file: its {@code nextVersionId} when it is versioned.
     *
     * @param file the file
     */
    void update(StoredFile file);

    /**
     * Finds a file by its identifier.
     *
     * @param id the identifier
     * @return the file, or empty
     */
    Optional<StoredFile> getById(UUID id);

    /**
     * Finds a file and locks it until the end of the current transaction, so two new versions of it
     * cannot be created at the same time.
     *
     * @param id the identifier
     * @return the file, or empty
     */
    Optional<StoredFile> getByIdForUpdate(UUID id);

    /**
     * Finds every version of a chain: its history.
     *
     * @param firstVersionId the identifier of the first version
     * @return the versions, from the oldest to the newest; empty if there is no such chain
     */
    List<StoredFile> getByFirstVersionId(UUID firstVersionId);
}
