package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.InitialDocument;
import bo.org.tecnoweb.cpms.procedures.domain.Semester;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionOperations;

/** Checks aggregate mapping and PostgreSQL constraints for UC-002. */
@SpringBootTest(properties = "cpms.users.admin-email=admin@repository-procedures.test")
@Import(TestcontainersConfiguration.class)
class JpaDegreeProcedureRepositoryTest {
    @Autowired private DegreeProcedureRepository procedures;
    @Autowired private UserRepository users;
    @Autowired private TransactionOperations transactions;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private StoredFileService storedFiles;

    /** Round-trips the reference and the documents, and guards a second ongoing procedure (FR-056). */
    @Test
    void persistsAndPreventsSecondOpenProcedure() {
        var requester = secretary();
        var student = student();
        users.insert(requester);
        users.insert(student);
        var stored = DegreeProcedure.builder().open(student, requester, SEMESTER,
                storedDocuments(storedFiles, transactions));
        procedures.insert(stored);

        assertThat(stored.getReference()).isEqualTo(REFERENCE);
        assertThat(procedures.existsOpenForStudent(student.getId())).isTrue();
        assertThat(procedures.existsByReference(REFERENCE)).isTrue();
        assertThat(procedures.existsByReference("2026-1-123456")).isFalse();
        var otherSemester = DegreeProcedure.builder().open(student, requester, new Semester(2027, 1),
                storedDocuments(storedFiles, transactions));
        assertThatThrownBy(() -> procedures.insert(otherSemester)).isInstanceOf(DataIntegrityViolationException.class);

        var corrected = storedPdf(storedFiles, transactions, "ci_corregida.pdf");
        transactions.executeWithoutResult(status -> {
            var read = procedures.getOpenByReferenceForUpdate(stored.getReference()).orElseThrow();
            assertThat(read.getId()).isEqualTo(stored.getId());
            assertThat(read.getDocuments()).hasSize(4);
            assertThat(read.pullEvents()).isEmpty();
            read.replaceInitialDocument(requester, corrected);
            procedures.update(read);
        });
        assertThat(procedures.getOpenByReferenceForUpdate(stored.getReference()).orElseThrow()
                .getDocuments().get(InitialDocument.IDENTITY).storedFileId()).isEqualTo(corrected.getId());

        jdbc.update("UPDATE degree_procedure SET closed_at = now() WHERE id = ?", stored.getId());
        assertThat(procedures.existsOpenForStudent(student.getId())).isFalse();
        assertThat(procedures.getOpenByReferenceForUpdate(stored.getReference())).isEmpty();
        assertThat(procedures.existsByReference(REFERENCE)).isTrue();

        var sameSemester = DegreeProcedure.builder().open(student, requester, SEMESTER,
                storedDocuments(storedFiles, transactions));
        assertThatThrownBy(() -> procedures.insert(sameSemester)).isInstanceOf(DataIntegrityViolationException.class);
    }

    /** No procedures entity keeps file bytes: only the references to stored files (issue #9). */
    @Test
    void entitiesKeepNoFileContent() {
        for (Class<?> entity : List.of(DegreeProcedureEntity.class, InitialDocumentEntity.class,
                DegreeProjectEntity.class)) {
            assertThat(Arrays.stream(entity.getDeclaredFields()).<Class<?>>map(Field::getType)).as(entity.getSimpleName())
                    .doesNotContain(byte[].class);
        }
    }
}
