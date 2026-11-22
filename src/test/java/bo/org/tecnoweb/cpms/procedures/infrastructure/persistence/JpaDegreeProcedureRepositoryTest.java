package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.InitialDocument;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
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

    /** The database generates the reference and guards concurrent openings (FR-056). */
    @Test
    void persistsAndPreventsSecondOpenProcedure() {
        var requester = secretary();
        var student = student();
        users.insert(requester);
        users.insert(student);
        var stored = procedures.insert(DegreeProcedure.builder().withStudent(student)
                .open(student, requester, documents()));

        assertThat(stored.getReference()).isPositive();
        assertThat(procedures.existsOpenForStudent(student.getId())).isTrue();
        assertThatThrownBy(() -> procedures.insert(DegreeProcedure.builder().withStudent(student)
                .open(student, requester, documents()))).isInstanceOf(DataIntegrityViolationException.class);

        transactions.executeWithoutResult(status -> {
            var read = procedures.getOpenByReferenceForUpdate(stored.getReference()).orElseThrow();
            assertThat(read.getId()).isEqualTo(stored.getId());
            assertThat(read.getDocuments()).hasSize(4);
            assertThat(read.pullEvents()).isEmpty();
            read.replaceInitialDocument(requester, pdf("ci_corregida.pdf"));
            procedures.update(read);
        });
        assertThat(procedures.getOpenByReferenceForUpdate(stored.getReference()).orElseThrow()
                .getDocuments().get(InitialDocument.IDENTITY).filename()).isEqualTo("ci_corregida.pdf");

        jdbc.update("UPDATE degree_procedure SET closed_at = now() WHERE reference = ?", stored.getReference());
        assertThat(procedures.existsOpenForStudent(student.getId())).isFalse();
        assertThat(procedures.getOpenByReferenceForUpdate(stored.getReference())).isEmpty();
    }
}
