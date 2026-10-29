package grupo03sc.mailapp.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.audit.application.FieldChange;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserAccountTests {

    private static UserAccount student() {
        return UserAccount.create(new UserAccountData("Juan Soto", "Juan@UAGRM.edu.bo", Set.of(Role.STUDENT),
                "219000001", "1234567", "70000000"));
    }

    private static UserAccount professor() {
        return UserAccount.create(UserAccountData.of("Ana Pérez", "ana@uagrm.edu.bo", Set.of(Role.PROFESSOR)));
    }

    @Test
    void createsAStudentWithTheStudentData() {
        UserAccount account = student();

        assertThat(account.getEmail()).isEqualTo("juan@uagrm.edu.bo");
        assertThat(account.getStudentRegistrationNumber()).isEqualTo("219000001");
        assertThat(account.isActive()).isTrue();
        assertThat(account.describe()).extracting(FieldChange::field)
                .containsExactly("fullName", "email", "roles", "studentRegistrationNumber", "idCard", "phone", "status");
    }

    @Test
    void rejectsAStudentWithoutTheStudentData() {
        assertThatThrownBy(() -> UserAccount.create(UserAccountData.of("Juan Soto", "juan@uagrm.edu.bo", Set.of(Role.STUDENT))))
                .isInstanceOf(UserRuleViolationException.class)
                .hasMessage("Faltan datos: registro universitario, cédula de identidad, teléfono.");
    }

    @Test
    void rejectsAnAccountWithoutNameOrRoles() {
        assertThatThrownBy(() -> UserAccount.create(UserAccountData.of(" ", "ana@uagrm.edu.bo", Set.of())))
                .hasMessage("Faltan datos: nombre completo, roles.");
    }

    @Test
    void rejectsAnInvalidEmailAddress() {
        assertThatThrownBy(() -> UserAccount.create(UserAccountData.of("Ana Pérez", "ana-uagrm", Set.of(Role.PROFESSOR))))
                .extracting("reason").isEqualTo(RejectionReason.INVALID_DATA);
    }

    @Test
    void changesAFieldAndReturnsTheOldAndNewValues() {
        UserAccount account = student();

        FieldChange change = account.changeField(AccountField.PHONE, " 71111111 ");

        assertThat(change).isEqualTo(new FieldChange("phone", "70000000", "71111111"));
        assertThat(account.getPhone()).isEqualTo("71111111");
    }

    @Test
    void normalizesAChangedEmailAndValidatesIt() {
        UserAccount account = professor();

        assertThat(account.changeField(AccountField.EMAIL, "ANA.P@uagrm.edu.bo").newValue()).isEqualTo("ana.p@uagrm.edu.bo");
        assertThatThrownBy(() -> account.changeField(AccountField.EMAIL, "sin-arroba")).isInstanceOf(UserRuleViolationException.class);
    }

    @Test
    void rejectsRemovingAMandatoryStudentField() {
        UserAccount account = student();

        assertThatThrownBy(() -> account.changeField(AccountField.ID_CARD, ""))
                .hasMessage("Faltan datos: cédula de identidad.");
    }

    @Test
    void changesTheRoles() {
        UserAccount account = professor();

        FieldChange change = account.changeRoles(Set.of(Role.PROFESSOR, Role.THESIS_WORKSHOP_PROFESSOR));

        assertThat(change).isEqualTo(new FieldChange("roles", "PROFESSOR", "PROFESSOR,THESIS_WORKSHOP_PROFESSOR"));
        assertThat(account.getRoles()).containsExactlyInAnyOrder(Role.PROFESSOR, Role.THESIS_WORKSHOP_PROFESSOR);
    }

    @Test
    void deactivatesAndReactivates() {
        UserAccount account = professor();

        assertThat(account.deactivate()).isEqualTo(new FieldChange("status", "ACTIVE", "INACTIVE"));
        assertThat(account.isActive()).isFalse();
        assertThatThrownBy(account::deactivate).hasMessage("Dato inválido: la cuenta de usuario ya está desactivada.");
        assertThat(account.reactivate()).isEqualTo(new FieldChange("status", "INACTIVE", "ACTIVE"));
        assertThat(account.isActive()).isTrue();
    }
}
