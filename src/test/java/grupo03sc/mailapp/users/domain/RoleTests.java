package grupo03sc.mailapp.users.domain;

import static grupo03sc.mailapp.users.domain.Role.ADMINISTRATOR;
import static grupo03sc.mailapp.users.domain.Role.PROFESSOR;
import static grupo03sc.mailapp.users.domain.Role.PROGRAM_DIRECTOR;
import static grupo03sc.mailapp.users.domain.Role.PROGRAM_SECRETARY;
import static grupo03sc.mailapp.users.domain.Role.STUDENT;
import static grupo03sc.mailapp.users.domain.Role.THESIS_WORKSHOP_PROFESSOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.graduationprocesses.application.ParticipantKind;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RoleTests {

    @Test
    void rejectsAStudentWithAnotherRole() {
        assertThatThrownBy(() -> Role.validateCombination(Set.of(STUDENT, PROFESSOR)))
                .isInstanceOf(UserRuleViolationException.class)
                .hasMessage("Combinación de roles no permitida: el rol estudiante no se combina con otros roles.")
                .extracting("reason").isEqualTo(RejectionReason.INVALID_ROLE_COMBINATION);
    }

    @ParameterizedTest
    @EnumSource(names = {"PROGRAM_SECRETARY", "ADMINISTRATOR"})
    void rejectsOtherSoleRolesWithAnotherRole(Role role) {
        assertThatThrownBy(() -> Role.validateCombination(Set.of(role, PROFESSOR)))
                .isInstanceOf(UserRuleViolationException.class);
    }

    @Test
    void allowsTheThesisWorkshopProfessorOnlyWithProfessor() {
        assertThatCode(() -> Role.validateCombination(Set.of(THESIS_WORKSHOP_PROFESSOR, PROFESSOR))).doesNotThrowAnyException();
        assertThatThrownBy(() -> Role.validateCombination(Set.of(THESIS_WORKSHOP_PROFESSOR, PROGRAM_DIRECTOR)))
                .hasMessageContaining("solo se combina con el rol docente");
    }

    @Test
    void allowsTheProgramDirectorWithProfessorAndAnySingleRole() {
        assertThatCode(() -> Role.validateCombination(Set.of(PROGRAM_DIRECTOR, PROFESSOR))).doesNotThrowAnyException();
        for (Role role : Role.values()) {
            assertThatCode(() -> Role.validateCombination(Set.of(role))).doesNotThrowAnyException();
        }
    }

    @Test
    void letsTheAdministratorManageEveryRole() {
        assertThat(Role.canManage(Set.of(ADMINISTRATOR), Set.of(Role.values()))).isTrue();
    }

    @Test
    void letsTheProgramDirectorManageOnlyProfessorsTheWorkshopProfessorAndSecretaries() {
        Set<Role> director = Set.of(PROGRAM_DIRECTOR, PROFESSOR);
        assertThat(Role.canManage(director, Set.of(PROGRAM_SECRETARY))).isTrue();
        assertThat(Role.canManage(director, Set.of(THESIS_WORKSHOP_PROFESSOR, PROFESSOR))).isTrue();
        assertThat(Role.canManage(director, Set.of(PROGRAM_DIRECTOR))).isFalse();
        assertThat(Role.canManage(director, Set.of(PROGRAM_DIRECTOR, PROFESSOR))).isFalse();
        assertThat(Role.canManage(director, Set.of(ADMINISTRATOR))).isFalse();
        assertThat(Role.canManage(director, Set.of(STUDENT))).isFalse();
    }

    @Test
    void letsNoOtherRoleManageAccounts() {
        assertThat(Role.canManage(Set.of(PROFESSOR, THESIS_WORKSHOP_PROFESSOR), Set.of(PROFESSOR))).isFalse();
        assertThat(Role.canManage(Set.of(PROGRAM_SECRETARY), Set.of(PROFESSOR))).isFalse();
    }

    @Test
    void readsLabelsIgnoringCaseAndSpaces() {
        assertThat(Role.fromLabel("  Docente  de Taller   de GRADO ")).contains(THESIS_WORKSHOP_PROFESSOR);
        assertThat(Role.fromLabel("decano")).isEmpty();
        assertThat(Role.labels(Set.of(THESIS_WORKSHOP_PROFESSOR, PROFESSOR))).isEqualTo("docente, docente de taller de grado");
    }

    @Test
    void mapsRolesToParticipantKinds() {
        assertThat(STUDENT.participantKinds()).containsExactly(ParticipantKind.STUDENT);
        assertThat(PROFESSOR.participantKinds()).containsExactlyInAnyOrder(ParticipantKind.TUTOR, ParticipantKind.TRIBUNAL_MEMBER);
        assertThat(ADMINISTRATOR.participantKinds()).isEmpty();
    }
}
