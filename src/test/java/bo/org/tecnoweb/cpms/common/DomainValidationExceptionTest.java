package bo.org.tecnoweb.cpms.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DomainValidationExceptionTest {

    @Test
    void keepsOneErrorPerFieldInOrder() {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("email", "El correo no es válido.");
        errors.put("telefono", "El teléfono es obligatorio.");

        DomainValidationException exception = new DomainValidationException(errors);

        assertThat(exception.getErrors()).containsExactly(
                Map.entry("email", "El correo no es válido."),
                Map.entry("telefono", "El teléfono es obligatorio."));
        assertThat(exception.getErrorCode()).isEqualTo(DomainValidationException.ERROR_CODE);
        assertThat(exception).isInstanceOf(DomainException.class);
    }

    @Test
    void errorsCannotBeChangedAfterwards() {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("email", "El correo no es válido.");
        DomainValidationException exception = new DomainValidationException(errors);

        errors.put("otro", "x");

        assertThat(exception.getErrors()).hasSize(1);
        assertThatThrownBy(() -> exception.getErrors().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void requiresAtLeastOneError() {
        assertThatThrownBy(() -> new DomainValidationException(Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
