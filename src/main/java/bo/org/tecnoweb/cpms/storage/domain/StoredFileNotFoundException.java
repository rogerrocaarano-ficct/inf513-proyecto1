package bo.org.tecnoweb.cpms.storage.domain;

import bo.org.tecnoweb.cpms.common.DomainException;
import java.util.UUID;

/**
 * No stored file has the requested identifier (issue #9, file storage).
 */
public class StoredFileNotFoundException extends DomainException {

    /** Error code of every missing stored file. */
    public static final String ERROR_CODE = "STORED_FILE_NOT_FOUND";

    /**
     * Creates the exception.
     *
     * @param id the identifier that has no stored file
     */
    public StoredFileNotFoundException(UUID id) {
        super(ERROR_CODE, "El archivo solicitado no existe.");
    }
}
