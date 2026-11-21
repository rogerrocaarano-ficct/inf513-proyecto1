package bo.org.tecnoweb.cpms.users.domain;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.common.DomainException;
import org.junit.jupiter.api.Test;

class UserNotAuthorizedExceptionTest {

    @Test
    void missingPermissionIsADomainExceptionWithASpanishMessage() {
        UserNotAuthorizedException e = new UserNotAuthorizedException("luis@uagrm.bo", "crear cuentas de director");

        assertThat(e).isInstanceOf(DomainException.class);
        assertThat(e.getErrorCode()).isEqualTo(UserNotAuthorizedException.ERROR_CODE);
        assertThat(e.getMessage()).isEqualTo("La cuenta luis@uagrm.bo no tiene permiso para crear cuentas de director.");
    }

    @Test
    void notRegisteredSaysTheAddressIsNotRegistered() {
        UserNotAuthorizedException e = UserNotAuthorizedException.notRegistered("extrano@gmail.com");

        assertThat(e.getErrorCode()).isEqualTo(UserNotAuthorizedException.ERROR_CODE);
        assertThat(e.getMessage()).contains("extrano@gmail.com").contains("no está registrada en el sistema");
    }
}
