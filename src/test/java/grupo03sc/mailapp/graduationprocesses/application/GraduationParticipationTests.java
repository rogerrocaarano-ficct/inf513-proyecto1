package grupo03sc.mailapp.graduationprocesses.application;

import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.email;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.fourDocuments;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.registration;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.student;
import static org.assertj.core.api.Assertions.assertThat;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.users.application.AffectedProcess;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccountData;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** The real {@link GraduationParticipation} over the processes that UC-003 opens (FR-034). */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GraduationParticipationTests {

    @Autowired
    private GraduationParticipation participation;

    @Autowired
    private GraduationProcessService service;

    @Autowired
    private UserManagementService users;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID admin() {
        return accounts.findByEmail("admin@mailapp.test").orElseThrow().getId();
    }

    private OpenedProcess openProcess(String email) {
        UUID secretary = users.createAccount(admin(),
                UserAccountData.of("Secretaria", email("secretaria"), Set.of(Role.PROGRAM_SECRETARY))).getId();
        return service.openProcess(secretary, student(email, registration()), fourDocuments());
    }

    @Test
    void findsNoProcessesForAnAccountWithoutThem() {
        assertThat(participation.markPendingReassignment(UUID.randomUUID(), EnumSet.allOf(ParticipantKind.class)))
                .isEmpty();
    }

    @Test
    void marksThePendingReassignmentOfTheStudentProcessInProgress() {
        OpenedProcess opened = openProcess(email("juan"));

        assertThat(participation.markPendingReassignment(opened.student().id(), EnumSet.of(ParticipantKind.STUDENT)))
                .containsExactly(new GraduationProcessSummary(opened.process().getId(), opened.student().id()));
        assertThat(jdbc.queryForObject("SELECT pending_reassignment FROM graduation_processes WHERE id = ?",
                Boolean.class, opened.process().getId())).isTrue();
    }

    @Test
    void deactivatingAStudentWithAProcessInProgressReportsItWithTheStudentName() {
        String email = email("juan");
        OpenedProcess opened = openProcess(email);

        assertThat(users.deactivateAccount(admin(), email).affectedProcesses())
                .containsExactly(new AffectedProcess(opened.process().getId(), "Juan Soto"));
    }
}
