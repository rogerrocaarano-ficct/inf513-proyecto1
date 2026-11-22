package bo.org.tecnoweb.cpms.storage.domain;

import bo.org.tecnoweb.cpms.common.DomainException;
import java.util.UUID;

/**
 * A new version was requested from a version that already has a next one, so the version chain
 * would fork (issue #9, file storage).
 */
public class StoredFileNotCurrentException extends DomainException {

    /** Error code of every attempt to version a version that is not the current one. */
    public static final String ERROR_CODE = "STORED_FILE_NOT_CURRENT";

    /**
     * Creates the exception.
     *
     * @param id the identifier of the version that already has a next one
     */
    public StoredFileNotCurrentException(UUID id) {
        super(ERROR_CODE, "Solo se puede crear una versión nueva a partir de la versión vigente del archivo.");
    }
}
