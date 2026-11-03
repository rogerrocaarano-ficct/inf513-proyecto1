package grupo03sc.mailapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MigrationsTests {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void appliesTheMigrationsInOrder() {
        assertThat(flyway.info().applied())
                .extracting(MigrationInfo::getVersion)
                .extracting(Object::toString)
                .containsExactly("1", "2", "3", "4");
    }

    @Test
    void rejectsARepeatedEmail() {
        String email = "repetido-" + UUID.randomUUID() + "@mailapp.local";
        insertAccount(email);

        assertThatThrownBy(() -> insertAccount(email)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void rejectsUpdatesAndDeletesOfTheChangeLog() {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO change_log_entries (id, actor_id, target_type, target_id, field, new_value)"
                + " VALUES (?, ?, 'Test', ?, 'campo', 'valor')", id, UUID.randomUUID(), UUID.randomUUID());

        assertThatThrownBy(() -> jdbc.update("UPDATE change_log_entries SET new_value = 'otro' WHERE id = ?", id))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("append-only");
        assertThatThrownBy(() -> jdbc.update("DELETE FROM change_log_entries WHERE id = ?", id))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("append-only");
    }

    @Test
    void allowsOnlyOneGraduationProcessInProgressPerStudent() {
        UUID student = UUID.randomUUID();
        insertProcess(student, "CLOSED");
        insertProcess(student, "IN_PROGRESS");

        assertThatThrownBy(() -> insertProcess(student, "IN_PROGRESS")).isInstanceOf(DuplicateKeyException.class);
    }

    private void insertProcess(UUID student, String status) {
        jdbc.update("INSERT INTO graduation_processes (id, student_account_id, status, opened_at, closed_at,"
                + " physical_copies_received_at) VALUES (?, ?, ?, now(), now(), now())", UUID.randomUUID(), student, status);
    }

    private void insertAccount(String email) {
        jdbc.update("INSERT INTO user_accounts (id, full_name, email) VALUES (?, 'Prueba', ?)", UUID.randomUUID(), email);
    }
}
