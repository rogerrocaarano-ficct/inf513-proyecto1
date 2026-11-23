package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionOperations;

/** Verifies JPA mapping and project constraints for UC-003. */
@SpringBootTest(properties = "cpms.users.admin-email=admin@repository-projects.test")
@Import(TestcontainersConfiguration.class)
class JpaDegreeProjectRepositoryTest {
    @Autowired private DegreeProcedureRepository procedures;
    @Autowired private DegreeProjectRepository projects;
    @Autowired private UserRepository users;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private StoredFileService storedFiles;
    @Autowired private TransactionOperations transactions;

    /** Round-trips the stored file of the profile and saves a tutor only after approval (FR-003, FR-004). */
    @Test
    void persistsProfileAndAssignment() {
        var student = student();
        var secretary = secretary();
        var tutor = secretary().withRole(Role.PROFESSOR).withEmailAddress("tutor@repository-projects.test");
        users.insert(student);
        users.insert(secretary);
        users.insert(tutor);
        var procedure = procedures.insert(DegreeProcedure.builder().withStudent(student)
                .open(student, secretary, storedDocuments(storedFiles, transactions)));
        var file = storedPdf(storedFiles, transactions, "perfil.pdf");
        var project = DegreeProject.builder().withProcedureId(procedure.getId()).withTitle("Redes")
                .registerInitialProfile(file, student.getEmailAddress(), "taller@test", "TR-001");
        projects.insert(project);

        var pending = projects.getByProcedureId(procedure.getId()).orElseThrow();
        assertThat(pending.getProfile().storedFileId()).isEqualTo(file.getId());
        assertThat(pending.getProfile().approved()).isFalse();
        assertThat(pending.pullEvents()).isEmpty();
        assertThatThrownBy(() -> projects.insert(DegreeProject.builder().withProcedureId(procedure.getId())
                .withTitle("Otro").registerInitialProfile(file, student.getEmailAddress(), "taller@test", "TR-001")))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.update("UPDATE degree_project SET profile_approved = true WHERE id = ?", project.getId());
        var approved = projects.getByProcedureId(procedure.getId()).orElseThrow();
        approved.assignTutor(tutor.getId(), tutor.getFullName(), student.getEmailAddress(), "TR-001");
        projects.update(approved);
        assertThat(projects.getByProcedureId(procedure.getId()).orElseThrow().getTutorId()).isEqualTo(tutor.getId());
    }
}
