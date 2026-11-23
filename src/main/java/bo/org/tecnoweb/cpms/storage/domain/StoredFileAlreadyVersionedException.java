package bo.org.tecnoweb.cpms.storage.domain;

import bo.org.tecnoweb.cpms.common.DomainException;
import java.util.UUID;

/**
 * A file was given as the next version of another one, but it already belongs to a version chain
 * (it is a later version of a file, it already has a next version, or it is the same file), so two
 * chains would share it (issue #9, file storage).
 */
public class StoredFileAlreadyVersionedException extends DomainException {

    /** Error code of every attempt to link a file that is not standalone. */
    public static final String ERROR_CODE = "STORED_FILE_ALREADY_VERSIONED";

    /**
     * Creates the exception.
     *
     * @param id the identifier of the file that is not standalone
     */
    public StoredFileAlreadyVersionedException(UUID id) {
        super(ERROR_CODE, "El archivo ya forma parte del historial de otro archivo.");
    }
}
