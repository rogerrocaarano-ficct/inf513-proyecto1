package bo.org.tecnoweb.cpms.storage.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class StoredFileExceptionsTest {

    @Test
    void notFoundHasItsCodeAndSpanishMessage() {
        StoredFileNotFoundException exception = new StoredFileNotFoundException(UUID.randomUUID());

        assertThat(exception.getErrorCode()).isEqualTo("STORED_FILE_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("El archivo solicitado no existe.");
    }

    @Test
    void notCurrentHasItsCodeAndSpanishMessage() {
        StoredFileNotCurrentException exception = new StoredFileNotCurrentException(UUID.randomUUID());

        assertThat(exception.getErrorCode()).isEqualTo("STORED_FILE_NOT_CURRENT");
        assertThat(exception.getMessage())
                .isEqualTo("Solo se puede crear una versión nueva a partir de la versión vigente del archivo.");
    }

    @Test
    void alreadyVersionedHasItsCodeAndSpanishMessage() {
        StoredFileAlreadyVersionedException exception = new StoredFileAlreadyVersionedException(UUID.randomUUID());

        assertThat(exception.getErrorCode()).isEqualTo("STORED_FILE_ALREADY_VERSIONED");
        assertThat(exception.getMessage()).isEqualTo("El archivo ya forma parte del historial de otro archivo.");
    }
}
