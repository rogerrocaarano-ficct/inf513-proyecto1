package grupo03sc.mailapp.users.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.graduationprocesses.application.ProfessorContact;
import grupo03sc.mailapp.graduationprocesses.application.ProfessorContacts;
import grupo03sc.mailapp.graduationprocesses.application.StudentAccounts;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** The {@link ProfessorContacts} port and {@link StudentAccounts#findById} as Users implements them (UC-004). */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserProfessorContactsTests {

    @Autowired
    private ProfessorContacts contacts;

    @Autowired
    private StudentAccounts studentAccounts;

    @Autowired
    private UserManagementService service;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID adminId;

    @BeforeEach
    void findAdmin() {
        adminId = accounts.findByEmail("admin@mailapp.test").orElseThrow().getId();
    }

    private static String email(String name) {
        return name + "-" + UUID.randomUUID() + "@uagrm.edu.bo";
    }

    /** The only active thesis workshop professor, created if the shared database has none (FR-039). */
    private UserAccount thesisWorkshopProfessor() {
        List<UserAccount> active = accounts.findActiveWithRole(Role.THESIS_WORKSHOP_PROFESSOR);
        if (!active.isEmpty()) {
            return active.getFirst();
        }
        return service.createAccount(adminId, UserAccountData.of("Docente de Taller", email("taller"),
                Set.of(Role.PROFESSOR, Role.THESIS_WORKSHOP_PROFESSOR)));
    }

    @Test
    void findsTheActiveThesisWorkshopProfessor() {
        UserAccount professor = thesisWorkshopProfessor();

        assertThat(contacts.thesisWorkshopProfessor())
                .contains(new ProfessorContact(professor.getId(), professor.getFullName(), professor.getEmail()));
    }

    @Test
    void findsNoThesisWorkshopProfessorWhenNoneIsActive() {
        UserAccount professor = thesisWorkshopProfessor();
        jdbc.update("UPDATE user_accounts SET status = 'INACTIVE' WHERE id = ?", professor.getId());
        try {
            assertThat(contacts.thesisWorkshopProfessor()).isEmpty();
        } finally {
            jdbc.update("UPDATE user_accounts SET status = 'ACTIVE' WHERE id = ?", professor.getId());
        }
    }

    @Test
    void findsAnActiveProfessorByIdButNotADeactivatedOne() {
        String tutorEmail = email("tutor");
        UserAccount tutor = service.createAccount(adminId, UserAccountData.of("Tutor", tutorEmail, Set.of(Role.PROFESSOR)));

        assertThat(contacts.findById(tutor.getId())).map(ProfessorContact::email).contains(tutorEmail);

        service.deactivateAccount(adminId, tutorEmail);
        assertThat(contacts.findById(tutor.getId())).isEmpty();
        assertThat(contacts.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findsAStudentAccountByIdOnlyWithTheStudentRole() {
        UserAccount student = service.createAccount(adminId, new UserAccountData("Juan Soto", email("juan"),
                Set.of(Role.STUDENT), "9" + Math.abs(UUID.randomUUID().getMostSignificantBits() % 100_000_000L),
                "1234567", "70000000"));
        UserAccount professor = service.createAccount(adminId,
                UserAccountData.of("Docente", email("docente"), Set.of(Role.PROFESSOR)));

        assertThat(studentAccounts.findById(student.getId())).map(account -> account.fullName()).contains("Juan Soto");
        assertThat(studentAccounts.findById(professor.getId())).isEmpty();
    }
}
