package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.InitialDocument;
import jakarta.persistence.EntityManager;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class PostgresDegreeProcedureRepository implements DegreeProcedureRepository {
    private final JdbcTemplate jdbc;
    private final EntityManager entityManager;

    PostgresDegreeProcedureRepository(JdbcTemplate jdbc, EntityManager entityManager) {
        this.jdbc = jdbc;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<UUID> lockAccountByEmail(String email) {
        return jdbc.query("SELECT id FROM user_account WHERE email_address = ? FOR UPDATE",
                (row, index) -> row.getObject("id", UUID.class), email).stream().findFirst();
    }

    @Override
    public boolean isStudentCodeTaken(String code) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM user_account WHERE uagrm_code = ?)", Boolean.class, code));
    }

    @Override
    public Optional<Long> open(UUID student, UUID requester, Map<InitialDocument, File> documents) {
        // A newly created account must be visible to the foreign key before the JDBC insert.
        entityManager.flush();
        Optional<Long> reference = jdbc.query("""
                INSERT INTO degree_procedure (student_id, opened_by) VALUES (?, ?)
                ON CONFLICT (student_id) WHERE closed_at IS NULL DO NOTHING
                RETURNING reference
                """, (row, index) -> row.getLong("reference"), student, requester).stream().findFirst();
        reference.ifPresent(id -> documents.forEach((type, file) -> jdbc.update("""
                INSERT INTO initial_document (procedure_reference, document_type, filename, content, updated_by)
                VALUES (?, ?, ?, ?, ?)
                """, id, type.name(), file.getName(), file.getData(), requester)));
        return reference;
    }

    @Override
    public boolean replace(long reference, UUID requester, InitialDocument type, File document) {
        return jdbc.update("""
                WITH open_procedure AS (
                    SELECT reference FROM degree_procedure WHERE reference = ? AND closed_at IS NULL FOR UPDATE
                )
                UPDATE initial_document AS document
                SET filename = ?, content = ?, updated_by = ?, updated_at = now()
                FROM open_procedure
                WHERE document.procedure_reference = open_procedure.reference AND document.document_type = ?
                """, reference, document.getName(), document.getData(), requester, type.name()) == 1;
    }
}
