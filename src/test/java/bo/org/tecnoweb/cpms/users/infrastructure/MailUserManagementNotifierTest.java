package bo.org.tecnoweb.cpms.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.users.application.ListUsersQueryItem;
import bo.org.tecnoweb.cpms.users.application.ListUsersQueryResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Text of the reply to {@code usuarios.listar}. */
class MailUserManagementNotifierTest {

    @Test
    void listWithAFullAccountAndOneWithMissingData() {
        ListUsersQueryResult result = new ListUsersQueryResult(List.of(
                new ListUsersQueryItem("Ana María Pérez Rojas", "71234567", "218001234", "ana@uagrm.bo",
                        "estudiante", "1234567"),
                new ListUsersQueryItem("Beto Arce", null, null, "beto@uagrm.bo", "estudiante", null)),
                "estudiante", "secretaria@uagrm.bo");

        assertThat(MailUserManagementNotifier.userListSubject(result)).isEqualTo("Cuentas de usuario: estudiante (2)");
        assertThat(MailUserManagementNotifier.userListBody(result)).isEqualTo("""
                Cuentas con rol estudiante:

                1. Ana María Pérez Rojas
                   Correo: ana@uagrm.bo
                   CI: 1234567 | Código: 218001234 | Teléfono: 71234567

                2. Beto Arce
                   Correo: beto@uagrm.bo
                   CI: - | Código: - | Teléfono: -
                """);
    }

    @Test
    void emptyList() {
        ListUsersQueryResult result = new ListUsersQueryResult(List.of(), "docente_taller", "admin@uagrm.bo");

        assertThat(MailUserManagementNotifier.userListSubject(result))
                .isEqualTo("Cuentas de usuario: docente_taller (0)");
        assertThat(MailUserManagementNotifier.userListBody(result)).isEqualTo("No hay cuentas con rol docente_taller.");
    }
}
