package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.graduationprocesses.domain.GraduationStage;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectStatus;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Sets with SQL the states that UC-005 and UC-006 will produce (D8 of implementar-uc-004): the tutor,
 * the status of the current document, with its change log entry, and the approved pre-defense.
 */
public final class GraduationProjectFixtures {

    private final JdbcTemplate jdbc;
    private final UserManagementService users;
    private final UserAccountRepository accounts;

    public GraduationProjectFixtures(JdbcTemplate jdbc, UserManagementService users, UserAccountRepository accounts) {
        this.jdbc = jdbc;
        this.users = users;
        this.accounts = accounts;
    }

    private UUID adminId() {
        return accounts.findByEmail("admin@mailapp.test").orElseThrow().getId();
    }

    /** The only active thesis workshop professor, created if the shared database has none (FR-039). */
    public UserAccount thesisWorkshopProfessor() {
        List<UserAccount> active = accounts.findActiveWithRole(Role.THESIS_WORKSHOP_PROFESSOR);
        if (!active.isEmpty()) {
            return active.getFirst();
        }
        return users.createAccount(adminId(), UserAccountData.of("Docente de Taller",
                GraduationTestData.email("taller"), Set.of(Role.PROFESSOR, Role.THESIS_WORKSHOP_PROFESSOR)));
    }

    /** Runs the action with no active thesis workshop professor, and restores it afterwards. */
    public void withoutThesisWorkshopProfessor(Runnable action) {
        UUID professor = thesisWorkshopProfessor().getId();
        jdbc.update("UPDATE user_accounts SET status = 'INACTIVE' WHERE id = ?", professor);
        try {
            action.run();
        } finally {
            jdbc.update("UPDATE user_accounts SET status = 'ACTIVE' WHERE id = ?", professor);
        }
    }

    /** A new professor account, designated as tutor of the process (UC-005). */
    public UserAccount assignTutor(UUID processId) {
        UserAccount tutor = users.createAccount(adminId(),
                UserAccountData.of("Tutor", GraduationTestData.email("tutor"), Set.of(Role.PROFESSOR)));
        jdbc.update("INSERT INTO graduation_process_participants (graduation_process_id, user_account_id, kind)"
                + " VALUES (?, ?, 'TUTOR')", processId, tutor.getId());
        return tutor;
    }

    /** The status of the current document, as UC-006 will set it, with its change log entry. */
    public void setStatus(UUID projectId, ProjectStatus status) {
        String old = jdbc.queryForObject("SELECT status FROM graduation_projects WHERE id = ?", String.class, projectId);
        jdbc.update("UPDATE graduation_projects SET status = ?, version = version + 1 WHERE id = ?", status.name(),
                projectId);
        jdbc.update("INSERT INTO change_log_entries (id, changed_at, actor_id, target_type, target_id, field,"
                + " old_value, new_value) VALUES (?, clock_timestamp(), ?, 'GraduationProject', ?, 'status', ?, ?)",
                UUID.randomUUID(), adminId(), projectId, old, status.name());
    }

    /** The approved pre-defense of the process (UC-006). */
    public void approvePredefense(UUID processId) {
        jdbc.update("UPDATE graduation_processes SET stage = ? WHERE id = ?", GraduationStage.PREDEFENSE_APPROVED.name(),
                processId);
    }
}
