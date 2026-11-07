package grupo03sc.mailapp.storage.application;

/**
 * Interface provided by Storage (AM-004, AD-012): keeps the files that users send in the file
 * system of the server. Business subsystems keep the key it returns.
 */
public interface FileStorage {

    /**
     * Writes the file under {@code <root>/<category>/<UUID>.<extension of fileName>} and returns that
     * relative path as its key. If the current transaction rolls back, the file is deleted.
     *
     * @throws java.io.UncheckedIOException if the file cannot be written
     */
    String store(String category, String fileName, byte[] content);

    /**
     * Deletes the file after the current transaction commits; if it rolls back, the file is kept.
     * A failure to delete is logged.
     */
    void delete(String storageKey);
}
