package bo.org.tecnoweb.cpms.storage.application;

import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileNotFoundException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Single entry point to file storage for every subsystem (issue #9). No other subsystem uses
 * {@link StoredFileRepository} or {@link FileStorage} directly.
 *
 * <p>It opens no transaction of its own: it runs inside the caller's (for example, the one
 * {@code InboxPollerService} opens for each command), so the metadata is saved or rolled back
 * together with the business operation, and the written file is deleted on rollback.
 *
 * <p>The owner of a file keeps the identifier of its current version and updates it when it
 * creates a new version.
 */
public class StoredFileService {

    private final StoredFileRepository files;
    private final FileStorage storage;

    /**
     * Creates the service.
     *
     * @param files   persistence port of the metadata
     * @param storage port that keeps the bytes
     */
    public StoredFileService(StoredFileRepository files, FileStorage storage) {
        this.files = files;
        this.storage = storage;
    }

    /**
     * Stores a new file, the first version of its own chain.
     *
     * @param originalName the name the file arrived with
     * @param contentType  the media type of the content
     * @param content      the bytes
     * @return the stored file
     * @throws bo.org.tecnoweb.cpms.common.DomainValidationException if the name or the type is blank,
     *                                                               or the content is empty
     */
    public StoredFile store(String originalName, String contentType, byte[] content) {
        StoredFile file = newFile(originalName, contentType, content);
        storage.write(file.getPath(), content);
        files.insert(file);
        return file;
    }

    /**
     * Stores a new version of a file, which becomes the current one. The previous version and its
     * bytes are kept unchanged.
     *
     * <p>The previous version is locked first, so a concurrent request waits and is then rejected
     * because the version is no longer the current one.
     *
     * @param previousId   the identifier of the current version
     * @param originalName the name the new version arrived with
     * @param contentType  the media type of the new content
     * @param content      the new bytes
     * @return the new version
     * @throws StoredFileNotFoundException                           if {@code previousId} does not
     *                                                               exist
     * @throws bo.org.tecnoweb.cpms.storage.domain.StoredFileNotCurrentException if
     *                                                               {@code previousId} already has a
     *                                                               next version
     * @throws bo.org.tecnoweb.cpms.common.DomainValidationException if the new file is invalid
     */
    public StoredFile storeNewVersion(UUID previousId, String originalName, String contentType, byte[] content) {
        StoredFile previous = files.getByIdForUpdate(previousId)
                .orElseThrow(() -> new StoredFileNotFoundException(previousId));
        StoredFile next = newFile(originalName, contentType, content);
        previous.supersedeBy(next);
        storage.write(next.getPath(), content);
        files.insert(next);
        files.update(previous);
        return next;
    }

    /**
     * Reads the bytes of a file.
     *
     * @param id the identifier of the file
     * @return the bytes that were stored
     * @throws StoredFileNotFoundException if {@code id} does not exist
     */
    public byte[] readContent(UUID id) {
        StoredFile file = files.getById(id).orElseThrow(() -> new StoredFileNotFoundException(id));
        return storage.read(file.getPath());
    }

    /**
     * Returns every version of the chain a file belongs to.
     *
     * @param id the identifier of any version of the chain
     * @return the versions, from the oldest to the newest; the last one is the current version
     * @throws StoredFileNotFoundException if {@code id} does not exist
     */
    public List<StoredFile> getHistory(UUID id) {
        StoredFile file = files.getById(id).orElseThrow(() -> new StoredFileNotFoundException(id));
        Map<UUID, StoredFile> chain = new HashMap<>();
        files.getByFirstVersionId(file.getFirstVersionId()).forEach(version -> chain.put(version.getId(), version));
        // Follow the links instead of trusting storedAt, which two quick versions may share.
        List<StoredFile> history = new ArrayList<>();
        for (StoredFile version = chain.get(file.getFirstVersionId()); version != null;
                version = version.isCurrent() ? null : chain.get(version.getNextVersionId())) {
            history.add(version);
        }
        return history;
    }

    private static StoredFile newFile(String originalName, String contentType, byte[] content) {
        return StoredFile.builder().withOriginalName(originalName).withContentType(contentType)
                .withSize(content == null ? 0 : content.length).store();
    }
}
