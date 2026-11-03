package grupo03sc.mailapp.users.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.audit.application.ChangeLogEntryRepository;
import grupo03sc.mailapp.graduationprocesses.application.StudentAccount;
import grupo03sc.mailapp.graduationprocesses.application.StudentAccounts;
import grupo03sc.mailapp.graduationprocesses.application.StudentData;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.RejectionReason;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import grupo03sc.mailapp.users.domain.UserRuleViolationException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** The {@link StudentAccounts} port as Users implements it (spec user-accounts, UC-003). */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserStudentAccountsTests {

    @Autowired
    private StudentAccounts studentAccounts;

    @Autowired
    private UserManagementService service;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private ChangeLogEntryRepository changeLog;

    private UUID adminId;
    private UUID secretaryId;

    @BeforeEach
    void createSecretary() {
        adminId = accounts.findByEmail("admin@mailapp.test").orElseThrow().getId();
        secretaryId = service.createAccount(adminId,
                UserAccountData.of("Secretaria", email("secretaria"), Set.of(Role.PROGRAM_SECRETARY))).getId();
    }

    private static String email(String name) {
        return name + "-" + UUID.randomUUID() + "@uagrm.edu.bo";
    }

    private static String registration() {
        return String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000L));
    }

    private static StudentData student(String email, String registration) {
        return new StudentData("Juan Soto", email, registration, "1234567", "70000000");
    }

    private UserAccount existingStudent(String email, String registration) {
        return service.createAccount(adminId, new UserAccountData("Juan Soto", email, Set.of(Role.STUDENT),
                registration, "1234567", "70000000"));
    }

    @Test
    void theSecretaryCreatesTheStudentAccount() {
        String email = email("juan");

        StudentAccount account = studentAccounts.obtain(secretaryId, student(email, registration()));

        assertThat(account.created()).isTrue();
        UserAccount saved = accounts.findById(account.id()).orElseThrow();
        assertThat(saved.getRoles()).containsExactly(Role.STUDENT);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(changeLog.findByTargetTypeAndTargetIdOrderByChangedAt("UserAccount", account.id()))
                .isNotEmpty().allSatisfy(entry -> assertThat(entry.getActorId()).isEqualTo(secretaryId));
    }

    @Test
    void usesTheExistingActiveStudentAccount() {
        String email = email("juan");
        String registration = registration();
        UserAccount existing = existingStudent(email, registration);
        int entries = changeLog.findByTargetTypeAndTargetIdOrderByChangedAt("UserAccount", existing.getId()).size();

        StudentAccount account = studentAccounts.obtain(secretaryId, student(email.toUpperCase(), registration));

        assertThat(account.id()).isEqualTo(existing.getId());
        assertThat(account.created()).isFalse();
        assertThat(changeLog.findByTargetTypeAndTargetIdOrderByChangedAt("UserAccount", existing.getId()))
                .hasSize(entries);
    }

    @Test
    void rejectsTheDataOfTwoDifferentAccounts() {
        UserAccount one = existingStudent(email("uno"), registration());
        UserAccount other = existingStudent(email("otro"), registration());

        assertThatThrownBy(() -> studentAccounts.obtain(secretaryId,
                student(one.getEmail(), other.getStudentRegistrationNumber())))
                .isInstanceOfSatisfying(UserRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(RejectionReason.DUPLICATE_EMAIL));
    }

    @Test
    void rejectsTheEmailOfAnAccountThatIsNotAStudent() {
        String email = email("docente");
        service.createAccount(adminId, UserAccountData.of("Docente", email, Set.of(Role.PROFESSOR)));

        assertThatThrownBy(() -> studentAccounts.obtain(secretaryId, student(email, registration())))
                .isInstanceOfSatisfying(UserRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(RejectionReason.DUPLICATE_EMAIL));
    }

    @Test
    void rejectsARegistrationNumberOfAnotherAccount() {
        UserAccount other = existingStudent(email("otro"), registration());

        assertThatThrownBy(() -> studentAccounts.obtain(secretaryId,
                student(email("nuevo"), other.getStudentRegistrationNumber())))
                .isInstanceOfSatisfying(UserRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(RejectionReason.DUPLICATE_REGISTRATION_NUMBER));
    }

    @Test
    void rejectsADeactivatedStudentAccount() {
        String email = email("juan");
        String registration = registration();
        existingStudent(email, registration);
        service.deactivateAccount(adminId, email);

        assertThatThrownBy(() -> studentAccounts.obtain(secretaryId, student(email, registration)))
                .isInstanceOfSatisfying(UserRuleViolationException.class, e -> {
                    assertThat(e.getReason()).isEqualTo(RejectionReason.INACTIVE_ACCOUNT);
                    assertThat(e.getMessage()).contains("desactivada");
                });
    }

    @Test
    void rejectsMissingStudentData() {
        assertThatThrownBy(() -> studentAccounts.obtain(secretaryId,
                new StudentData("Juan Soto", email("juan"), registration(), null, "70000000")))
                .isInstanceOfSatisfying(UserRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(RejectionReason.MISSING_DATA));
    }

    @Test
    void findsOnlyStudentsByRegistrationNumber() {
        String registration = registration();
        UserAccount student = existingStudent(email("juan"), registration);

        assertThat(studentAccounts.findByRegistrationNumber(" " + registration + " "))
                .map(StudentAccount::id).contains(student.getId());
        assertThat(studentAccounts.findByRegistrationNumber(registration())).isEmpty();
    }
}
