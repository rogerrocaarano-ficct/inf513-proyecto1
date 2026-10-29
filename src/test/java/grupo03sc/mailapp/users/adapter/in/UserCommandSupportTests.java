package grupo03sc.mailapp.users.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.users.domain.AccountField;
import grupo03sc.mailapp.users.domain.RejectionReason;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserRuleViolationException;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserCommandSupportTests {

    @Test
    void readsRolesBySpanishNameIgnoringCaseAndSpaces() {
        assertThat(UserCommandSupport.roles("Docente , Docente de Taller de Grado"))
                .containsExactlyInAnyOrder(Role.PROFESSOR, Role.THESIS_WORKSHOP_PROFESSOR);
        assertThat(UserCommandSupport.roles("ESTUDIANTE")).containsExactly(Role.STUDENT);
        assertThat(UserCommandSupport.roles("secretario, director de carrera, administrador"))
                .containsExactlyInAnyOrder(Role.PROGRAM_SECRETARY, Role.PROGRAM_DIRECTOR, Role.ADMINISTRATOR);
    }

    @Test
    void rejectsAnUnknownRoleNamingIt() {
        assertThatThrownBy(() -> UserCommandSupport.roles("docente, decano"))
                .isInstanceOf(UserRuleViolationException.class)
                .hasMessageStartingWith("Dato inválido: no se reconoce el rol decano")
                .extracting("reason").isEqualTo(RejectionReason.INVALID_DATA);
    }

    @Test
    void readsFieldsBySpanishNameAndRejectsUnknownOnes() {
        assertThat(UserCommandSupport.field("Telefono")).isEqualTo(AccountField.PHONE);
        assertThatThrownBy(() -> UserCommandSupport.field("edad"))
                .hasMessageContaining("nombre, correo, roles, registro, cedula, telefono");
    }

    @Test
    void treatsBlankArgumentsAsMissing() {
        List<String> args = List.of("Ana", " ");

        assertThat(UserCommandSupport.argument(args, 1)).isNull();
        assertThat(UserCommandSupport.argument(args, 5)).isNull();
        assertThatThrownBy(() -> UserCommandSupport.required(args, 1, "dirección de correo"))
                .hasMessage("Faltan datos: dirección de correo.");
    }
}
