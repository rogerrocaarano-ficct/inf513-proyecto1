package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfileStatus;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionOperations;

/** Verifies the JPA mapping of projects and their profile versions (UC-003, UC-004) and migration V12. */
@SpringBootTest(properties = "cpms.users.admin-email=admin@repository-projects.test")
@Import(TestcontainersConfiguration.class)
class JpaDegreeProjectRepositoryTest {
    @Autowired private DegreeProcedureRepository procedures;
    @Autowired private DegreeProjectRepository projects;
    @Autowired private UserRepository users;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;
    @Autowired private StoredFileService storedFiles;
    @Autowired private TransactionOperations transactions;

    /**
     * Round-trips the profile versions in order with their evaluation, and saves a tutor once the
     * current version is approved (FR-003, FR-004, FR-022, FR-023).
     */
    @Test
    void persistsProfileVersionsAndAssignment() {
        var student = student();
        var secretary = secretary();
        var tutor = secretary().withRole(Role.PROFESSOR).withEmailAddress("tutor@repository-projects.test");
        users.insert(student);
        users.insert(secretary);
        users.insert(tutor);
        var procedure = DegreeProcedure.builder().open(student, secretary, SEMESTER,
                storedDocuments(storedFiles, transactions));
        procedures.insert(procedure);
        var first = storedPdf(storedFiles, transactions, "perfil.pdf");
        var project = DegreeProject.builder().withProcedureId(procedure.getId())
                .registerProfile(first, "Redes", student.getEmailAddress(), "taller@test", REFERENCE);
        projects.insert(project);

        var pending = projects.getByProcedureId(procedure.getId()).orElseThrow();
        assertThat(pending.getProfile().storedFileId()).isEqualTo(first.getId());
        assertThat(pending.getProfile().status()).isEqualTo(ProjectProfileStatus.PENDING);
        assertThat(pending.pullEvents()).isEmpty();
        assertThatThrownBy(() -> projects.insert(DegreeProject.builder().withProcedureId(procedure.getId())
                .registerProfile(storedPdf(storedFiles, transactions, "otro.pdf"), "Otro",
                        student.getEmailAddress(), "taller@test", REFERENCE)))
                .isInstanceOf(DataIntegrityViolationException.class);

        pending.observeProfile("Corregir la bibliografía.", student.getEmailAddress(), REFERENCE);
        var second = storedPdf(storedFiles, transactions, "perfil-v2.pdf");
        pending.registerProfile(second, "Redes rurales", student.getEmailAddress(), "taller@test", REFERENCE);
        projects.update(pending);
        var resubmitted = projects.getByProcedureId(procedure.getId()).orElseThrow();
        assertThat(resubmitted.getTitle()).isEqualTo("Redes rurales");
        assertThat(resubmitted.getProfiles()).extracting(ProjectProfile::storedFileId)
                .containsExactly(first.getId(), second.getId());
        assertThat(resubmitted.getProfiles()).extracting(ProjectProfile::version).containsExactly(1, 2);
        assertThat(jdbc.queryForList("SELECT version FROM degree_project_profile_version WHERE project_id = ?"
                + " ORDER BY version", Integer.class, project.getId())).containsExactly(1, 2);
        assertThat(resubmitted.getProfiles().getFirst().status()).isEqualTo(ProjectProfileStatus.OBSERVED);
        assertThat(resubmitted.getProfiles().getFirst().observations()).isEqualTo("Corregir la bibliografía.");
        assertThat(resubmitted.getProfiles().getFirst().evaluatedOn()).isNotNull();

        resubmitted.approveProfile(student.getEmailAddress(), null, REFERENCE);
        resubmitted.assignTutor(tutor.getId(), tutor.getFullName(), student.getEmailAddress(), REFERENCE);
        projects.update(resubmitted);
        var assigned = projects.getByProcedureId(procedure.getId()).orElseThrow();
        assertThat(assigned.getTutorId()).isEqualTo(tutor.getId());
        assertThat(assigned.getProfile().status()).isEqualTo(ProjectProfileStatus.APPROVED);
        assertThat(assigned.getProfiles()).hasSize(2);
    }

    /** V12 turns the profile of a project recorded before UC-004 into its first version. */
    @Test
    void migratesTheProfileOfAnExistingProject() {
        String schema = "v12_" + UUID.randomUUID().toString().replace("-", "");
        try {
            migrate(schema, "11");
            UUID approved = existingProject(schema, "1", true);
            UUID pending = existingProject(schema, "2", false);
            migrate(schema, "12");

            assertThat(jdbc.queryForMap("SELECT version, status, title, evaluated_on IS NOT NULL AS evaluated"
                    + " FROM " + schema + ".degree_project_profile_version WHERE project_id = ?", approved))
                    .containsEntry("version", 1).containsEntry("status", "APPROVED")
                    .containsEntry("title", "Redes").containsEntry("evaluated", true);
            assertThat(jdbc.queryForObject("SELECT status FROM " + schema
                    + ".degree_project_profile_version WHERE project_id = ?", String.class, pending))
                    .isEqualTo("PENDING");
            assertThat(jdbc.queryForObject("SELECT count(*) FROM information_schema.columns WHERE table_schema = ?"
                    + " AND table_name = 'degree_project' AND column_name LIKE 'profile%'", Integer.class, schema))
                    .isZero();
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }

    private void migrate(String schema, String target) {
        Flyway.configure().dataSource(dataSource).schemas(schema).target(target)
                .locations("classpath:db/migration").load().migrate();
    }

    private UUID existingProject(String schema, String code, boolean approved) {
        UUID student = UUID.randomUUID();
        UUID procedure = UUID.randomUUID();
        UUID file = UUID.randomUUID();
        UUID project = UUID.randomUUID();
        jdbc.update("INSERT INTO " + schema + ".user_account (id, email_address, role, first_name, last_name)"
                + " VALUES (?, ?, 'STUDENT', 'Ana', 'Pérez')", student, code + "@v12.test");
        jdbc.update("INSERT INTO " + schema + ".degree_procedure (id, student_id, opened_by, reference)"
                + " VALUES (?, ?, ?, ?)", procedure, student, student, "2026-2-" + code);
        jdbc.update("INSERT INTO " + schema + ".stored_file (id, path, original_name, content_type, size, stored_at,"
                + " first_version_id) VALUES (?, ?, 'perfil.pdf', 'application/pdf', 10, now(), ?)",
                file, "v12/" + file, file);
        jdbc.update("INSERT INTO " + schema + ".degree_project (id, procedure_id, title, profile_approved,"
                + " profile_stored_file_id) VALUES (?, ?, 'Redes', ?, ?)", project, procedure, approved, file);
        return project;
    }
}
