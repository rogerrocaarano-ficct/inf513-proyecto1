package bo.org.tecnoweb.cpms.storage.application;

/**
 * Outgoing port that keeps and reads bytes by key (issue #9). It knows nothing about
 * {@link bo.org.tecnoweb.cpms.storage.domain.StoredFile} or the database; only
 * {@link StoredFileService} uses it.
 */
public interface FileStorage {

    /**
     * Writes the bytes under a key, relative to the storage root.
     *
     * <p>It never overwrites an existing file. It must be called inside a transaction: if that
     * transaction rolls back, the written file is deleted, so no orphan file is left.
     *
     * @param path    the key of the file
     * @param content the bytes
     * @throws IllegalStateException       if no transaction is active, or a file already exists under
     *                                     the key
     * @throws java.io.UncheckedIOException if the file cannot be written
     */
    void write(String path, byte[] content);

    /**
     * Reads the bytes kept under a key.
     *
     * @param path the key of the file
     * @return the bytes
     * @throws java.io.UncheckedIOException if the file cannot be read
     */
    byte[] read(String path);
}
