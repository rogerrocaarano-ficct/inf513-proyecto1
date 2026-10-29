package grupo03sc.mailapp.users.application;

import static grupo03sc.mailapp.users.domain.Role.ADMINISTRATOR;
import static grupo03sc.mailapp.users.domain.Role.PROFESSOR;
import static grupo03sc.mailapp.users.domain.Role.PROGRAM_DIRECTOR;
import static grupo03sc.mailapp.users.domain.Role.PROGRAM_SECRETARY;
import static grupo03sc.mailapp.users.domain.Role.STUDENT;
import static grupo03sc.mailapp.users.domain.Role.THESIS_WORKSHOP_PROFESSOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.audit.application.ChangeLogEntryRepository;
import grupo03sc.mailapp.audit.domain.ChangeLogEntry;
import grupo03sc.mailapp.users.domain.AccountField;
import grupo03sc.mailapp.users.domain.RejectionReason;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import grupo03sc.mailapp.users.domain.UserRuleViolationException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserManagementServiceTests {

    @Autowired
    private UserManagementService service;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private ChangeLogEntryRepository changeLog;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID adminId;

    @BeforeEach
    void findAdministrator() {
        adminId = accounts.findByEmail("admin@mailapp.test").orElseThrow().getId();
    }

    private static String email(String name) {
        return name + "-" + UUID.randomUUID() + "@uagrm.edu.bo";
    }

    private static String registration() {
        return String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000L));
    }

    private UserAccount create(Set<Role> roles) {
        return service.createAccount(adminId, UserAccountData.of("Cuenta de Prueba", email("cuenta"), roles));
    }

    private UUID director() {
        return create(Set.of(PROGRAM_DIRECTOR, PROFESSOR)).getId();
    }

    private void noActiveThesisWorkshopProfessor() {
        jdbc.update("UPDATE user_accounts SET status = 'INACTIVE' WHERE id IN"
                + " (SELECT user_account_id FROM user_account_roles WHERE role = 'THESIS_WORKSHOP_PROFESSOR')");
    }

    private static AbstractThrowableAssert<?, ? extends Throwable> assertRejected(Runnable call, RejectionReason reason) {
        return assertThatThrownBy(call::run).isInstanceOf(UserRuleViolationException.class)
                .satisfies(e -> assertThat(((UserRuleViolationException) e).getReason()).isEqualTo(reason));
    }

    private List<String> loggedFields(UUID accountId) {
        return changeLog.findByTargetTypeAndTargetIdOrderByChangedAt("UserAccount", accountId).stream()
                .map(ChangeLogEntry::getField).toList();
    }

    // Creación de una cuenta de usuario

    @Test
    void createsAProfessorAndLogsTheCreation() {
        String email = email("ana");

        UserAccount account = service.createAccount(adminId, UserAccountData.of("Ana Pérez", email, Set.of(PROFESSOR)));

        UserAccount stored = accounts.findByEmail(email).orElseThrow();
        assertThat(stored.getId()).isEqualTo(account.getId());
        assertThat(stored.isActive()).isTrue();
        assertThat(stored.getRoles()).containsExactly(PROFESSOR);
        assertThat(changeLog.findByTargetTypeAndTargetIdOrderByChangedAt("UserAccount", account.getId()))
                .allSatisfy(entry -> assertThat(entry.getActorId()).isEqualTo(adminId))
                .extracting(ChangeLogEntry::getField).contains("fullName", "email", "roles", "status");
    }

    @Test
    void createsAStudentWithTheStudentData() {
        String number = registration();

        UserAccount account = service.createAccount(adminId, new UserAccountData("Juan Soto", email("juan"),
                Set.of(STUDENT), number, "1234567", "70000000"));

        assertThat(accounts.findById(account.getId()).orElseThrow().getStudentRegistrationNumber()).isEqualTo(number);
    }

    @Test
    void rejectsAStudentWithoutTheStudentDataWithoutCreatingIt() {
        String email = email("juan");

        assertRejected(() -> service.createAccount(adminId, UserAccountData.of("Juan Soto", email, Set.of(STUDENT))),
                RejectionReason.MISSING_DATA);
        assertThat(accounts.findByEmail(email)).isEmpty();
    }

    // Registro universitario único

    @Test
    void rejectsARepeatedRegistrationNumberAndEmail() {
        String number = registration();
        UserAccount first = service.createAccount(adminId, new UserAccountData("Juan Soto", email("juan"),
                Set.of(STUDENT), number, "1", "2"));

        assertRejected(() -> service.createAccount(adminId, new UserAccountData("Otro", email("otro"),
                Set.of(STUDENT), number, "3", "4")), RejectionReason.DUPLICATE_REGISTRATION_NUMBER)
                .hasMessageContaining(number);
        assertRejected(() -> service.createAccount(adminId, UserAccountData.of("Otro", first.getEmail().toUpperCase(),
                Set.of(PROFESSOR))), RejectionReason.DUPLICATE_EMAIL);
    }

    // Combinaciones de roles y un solo docente de Taller de Grado

    @Test
    void rejectsAStudentWithAnotherRole() {
        assertRejected(() -> service.createAccount(adminId, new UserAccountData("X", email("x"),
                Set.of(STUDENT, PROFESSOR), registration(), "1", "2")), RejectionReason.INVALID_ROLE_COMBINATION);
    }

    @Test
    void allowsOneActiveThesisWorkshopProfessorOnly() {
        noActiveThesisWorkshopProfessor();
        UserAccount first = create(Set.of(PROFESSOR, THESIS_WORKSHOP_PROFESSOR));
        assertThat(first.getRoles()).containsExactlyInAnyOrder(PROFESSOR, THESIS_WORKSHOP_PROFESSOR);

        assertRejected(() -> create(Set.of(THESIS_WORKSHOP_PROFESSOR, PROFESSOR)), RejectionReason.SECOND_THESIS_WORKSHOP_PROFESSOR);
        UserAccount other = create(Set.of(PROFESSOR));
        assertRejected(() -> service.updateRoles(adminId, other.getEmail(), Set.of(PROFESSOR, THESIS_WORKSHOP_PROFESSOR)),
                RejectionReason.SECOND_THESIS_WORKSHOP_PROFESSOR);
    }

    @Test
    void rejectsReactivatingASecondThesisWorkshopProfessor() {
        noActiveThesisWorkshopProfessor();
        UserAccount first = create(Set.of(THESIS_WORKSHOP_PROFESSOR, PROFESSOR));
        service.deactivateAccount(adminId, first.getEmail());
        create(Set.of(THESIS_WORKSHOP_PROFESSOR, PROFESSOR));

        assertRejected(() -> service.reactivateAccount(adminId, first.getEmail()), RejectionReason.SECOND_THESIS_WORKSHOP_PROFESSOR);
        assertThat(accounts.findById(first.getId()).orElseThrow().isActive()).isFalse();
    }

    // Alcance del director de carrera

    @Test
    void letsTheProgramDirectorCreateASecretary() {
        UserAccount secretary = service.createAccount(director(),
                UserAccountData.of("Rosa Vaca", email("rosa"), Set.of(PROGRAM_SECRETARY)));

        assertThat(secretary.getRoles()).containsExactly(PROGRAM_SECRETARY);
    }

    @Test
    void doesNotLetTheProgramDirectorAssignRolesOutsideTheirScope() {
        UUID director = director();
        String email = email("nuevo");

        assertRejected(() -> service.createAccount(director, UserAccountData.of("Nuevo", email, Set.of(PROGRAM_DIRECTOR))),
                RejectionReason.OUT_OF_SCOPE).hasMessageContaining("director de carrera");
        assertThat(accounts.findByEmail(email)).isEmpty();
        UserAccount professor = create(Set.of(PROFESSOR));
        assertRejected(() -> service.updateRoles(director, professor.getEmail(), Set.of(ADMINISTRATOR)),
                RejectionReason.OUT_OF_SCOPE);
    }

    @Test
    void doesNotLetTheProgramDirectorManageAccountsOutsideTheirScope() {
        UUID director = director();
        UserAccount student = service.createAccount(adminId, new UserAccountData("Est", email("est"),
                Set.of(STUDENT), registration(), "1", "2"));

        assertRejected(() -> service.deactivateAccount(director, student.getEmail()), RejectionReason.OUT_OF_SCOPE);
        assertRejected(() -> service.deactivateAccount(director, "admin@mailapp.test"), RejectionReason.OUT_OF_SCOPE);
        assertThat(accounts.findById(student.getId()).orElseThrow().isActive()).isTrue();
    }

    // Consulta de cuentas de usuario

    @Test
    void listsAllAccountsForTheAdministratorAndOnlyTheScopeForTheDirector() {
        UserAccount professor = create(Set.of(PROFESSOR));
        UserAccount student = service.createAccount(adminId, new UserAccountData("Est", email("est"),
                Set.of(STUDENT), registration(), "1", "2"));
        UUID director = director();

        assertThat(service.listAccounts(adminId)).extracting(UserAccount::getId)
                .contains(professor.getId(), student.getId(), director, adminId);
        assertThat(service.listAccounts(director)).extracting(UserAccount::getId)
                .contains(professor.getId()).doesNotContain(student.getId(), director, adminId);
    }

    @Test
    void findsAnAccountOrRejectsAnUnknownOne() {
        UserAccount professor = create(Set.of(PROFESSOR));

        assertThat(service.findAccount(adminId, professor.getEmail().toUpperCase()).getId()).isEqualTo(professor.getId());
        assertRejected(() -> service.findAccount(adminId, "nadie@uagrm.edu.bo"), RejectionReason.ACCOUNT_NOT_FOUND);
    }

    // Modificación de una cuenta de usuario

    @Test
    void changesAFieldAndLogsTheOldAndNewValues() {
        UserAccount student = service.createAccount(adminId, new UserAccountData("Juan", email("juan"),
                Set.of(STUDENT), registration(), "1", "70000000"));

        service.updateField(adminId, student.getEmail(), AccountField.PHONE, "71111111");

        assertThat(accounts.findById(student.getId()).orElseThrow().getPhone()).isEqualTo("71111111");
        assertThat(changeLog.findByTargetTypeAndTargetIdOrderByChangedAt("UserAccount", student.getId()))
                .filteredOn(entry -> entry.getField().equals("phone") && entry.getOldValue() != null)
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getOldValue()).isEqualTo("70000000");
                    assertThat(entry.getNewValue()).isEqualTo("71111111");
                });
    }

    @Test
    void rejectsChangingTheEmailToOneOfAnotherAccount() {
        UserAccount first = create(Set.of(PROFESSOR));
        UserAccount second = create(Set.of(PROFESSOR));

        assertRejected(() -> service.updateField(adminId, second.getEmail(), AccountField.EMAIL, first.getEmail()),
                RejectionReason.DUPLICATE_EMAIL);
    }

    @Test
    void changesTheRolesAndLogsThem() {
        noActiveThesisWorkshopProfessor();
        UserAccount professor = create(Set.of(PROFESSOR));

        service.updateRoles(adminId, professor.getEmail(), Set.of(PROFESSOR, THESIS_WORKSHOP_PROFESSOR));

        assertThat(accounts.findById(professor.getId()).orElseThrow().getRoles())
                .containsExactlyInAnyOrder(PROFESSOR, THESIS_WORKSHOP_PROFESSOR);
        assertThat(loggedFields(professor.getId())).filteredOn("roles"::equals).hasSize(2);
    }

    @Test
    void rejectsRemovingTheAdministratorRoleFromTheOnlyActiveAdministrator() {
        assertRejected(() -> service.updateRoles(adminId, "admin@mailapp.test", Set.of(PROFESSOR)),
                RejectionReason.LAST_ADMINISTRATOR);
        assertThat(accounts.findById(adminId).orElseThrow().getRoles()).containsExactly(ADMINISTRATOR);
    }

    // Desactivación y reactivación

    @Test
    void deactivatesAndReactivatesKeepingTheData() {
        UserAccount professor = create(Set.of(PROFESSOR));

        service.deactivateAccount(adminId, professor.getEmail());
        assertThat(accounts.findById(professor.getId()).orElseThrow().isActive()).isFalse();

        service.reactivateAccount(adminId, professor.getEmail());
        UserAccount stored = accounts.findById(professor.getId()).orElseThrow();
        assertThat(stored.isActive()).isTrue();
        assertThat(stored.getRoles()).containsExactly(PROFESSOR);
        assertThat(loggedFields(professor.getId())).filteredOn("status"::equals).hasSize(3);
    }

    @Test
    void rejectsDeactivatingTheOnlyActiveAdministrator() {
        assertRejected(() -> service.deactivateAccount(adminId, "admin@mailapp.test"), RejectionReason.LAST_ADMINISTRATOR);
        assertThat(accounts.findById(adminId).orElseThrow().isActive()).isTrue();
    }

    @Test
    void reportsNoAffectedProcessesWhileThereAreNone() {
        UserAccount professor = create(Set.of(PROFESSOR));

        assertThat(service.deactivateAccount(adminId, professor.getEmail()).affectedProcesses()).isEmpty();
    }
}
