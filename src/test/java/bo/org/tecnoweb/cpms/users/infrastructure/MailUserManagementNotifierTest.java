package bo.org.tecnoweb.cpms.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.users.application.usermanagement.ListUsersQueryItem;
import bo.org.tecnoweb.cpms.users.application.usermanagement.ListUsersQueryResult;
import bo.org.tecnoweb.cpms.users.domain.UserDisabledEvent;
import bo.org.tecnoweb.cpms.users.domain.UserEnabledEvent;
import bo.org.tecnoweb.cpms.users.infrastructure.MailUserManagementNotifier.OutgoingMail;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Text of the replies and notices of {@code usuarios.listar}, {@code usuarios.desactivar} and {@code usuarios.activar}. */
class MailUserManagementNotifierTest {

    @Test
    void listWithAFullActiveAccountAndADisabledOneWithMissingData() {
        ListUsersQueryResult result = new ListUsersQueryResult(List.of(
                new ListUsersQueryItem("Ana María Pérez Rojas", "71234567", "218001234", "ana@uagrm.bo",
                        "estudiante", "1234567", true),
                new ListUsersQueryItem("Beto Arce", null, null, "beto@uagrm.bo", "estudiante", null, false)),
                "estudiante", "secretaria@uagrm.bo");

        assertThat(MailUserManagementNotifier.userListSubject(result)).isEqualTo("Cuentas de usuario: estudiante (2)");
        assertThat(MailUserManagementNotifier.userListBody(result)).isEqualTo("""
                Cuentas con rol estudiante:

                1. Ana María Pérez Rojas
                   Correo: ana@uagrm.bo
                   CI: 1234567 | Código: 218001234 | Teléfono: 71234567
                   Estado: activa

                2. Beto Arce
                   Correo: beto@uagrm.bo
                   CI: - | Código: - | Teléfono: -
                   Estado: desactivada
                """);
    }

    @Test
    void emptyList() {
        ListUsersQueryResult result = new ListUsersQueryResult(List.of(), "docente_taller", "admin@uagrm.bo");

        assertThat(MailUserManagementNotifier.userListSubject(result))
                .isEqualTo("Cuentas de usuario: docente_taller (0)");
        assertThat(MailUserManagementNotifier.userListBody(result)).isEqualTo("No hay cuentas con rol docente_taller.");
    }

    @Test
    void disabledAccountMails() {
        UserDisabledEvent event = new UserDisabledEvent("luis@uagrm.bo", "Luis Rojas", Instant.now(),
                "Carla Suárez Vaca", "director@uagrm.bo");

        assertThat(MailUserManagementNotifier.userDisabledMails(event)).containsExactly(
                new OutgoingMail("director@uagrm.bo", "Cuenta desactivada: Luis Rojas", """
                        Se desactivó la cuenta de Luis Rojas (luis@uagrm.bo).

                        Esa persona ya no puede enviar comandos al sistema; su historial se conserva."""),
                new OutgoingMail("luis@uagrm.bo", "Su cuenta en el sistema de trámites de titulación fue desactivada", """
                        Hola, Luis Rojas:

                        Carla Suárez Vaca desactivó su cuenta en el sistema de trámites de titulación.

                        Desde ahora el sistema no atenderá los comandos que envíe desde esta dirección. \
                        Si cree que es un error, comuníquese con la Dirección de Carrera."""));
    }

    @Test
    void enabledAccountMails() {
        UserEnabledEvent event = new UserEnabledEvent("luis@uagrm.bo", "Luis Rojas", Instant.now(),
                "Carla Suárez Vaca", "director@uagrm.bo");

        assertThat(MailUserManagementNotifier.userEnabledMails(event)).containsExactly(
                new OutgoingMail("director@uagrm.bo", "Cuenta activada: Luis Rojas", """
                        Se activó la cuenta de Luis Rojas (luis@uagrm.bo).

                        Esa persona ya puede volver a enviar comandos al sistema."""),
                new OutgoingMail("luis@uagrm.bo", "Su cuenta en el sistema de trámites de titulación fue activada", """
                        Hola, Luis Rojas:

                        Carla Suárez Vaca activó nuevamente su cuenta en el sistema de trámites de titulación.

                        Desde esta dirección (luis@uagrm.bo) puede volver a enviar solicitudes al sistema."""));
    }
}
