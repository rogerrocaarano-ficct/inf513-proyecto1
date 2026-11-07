package grupo03sc.mailapp.audit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.audit.domain.ChangeLogEntry;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ChangeLogTests {

    @Autowired
    private ChangeLog changeLog;

    @Autowired
    private ChangeLogEntryRepository entries;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void recordsEachChangedFieldWithItsActorAndValues() {
        UUID actor = UUID.randomUUID();
        UUID target = UUID.randomUUID();

        transaction.executeWithoutResult(status -> changeLog.record(actor, "Registro", target,
                List.of(new FieldChange("telefono", "700000", "711111"))));

        assertThat(entries.findByTargetTypeAndTargetIdOrderByChangedAt("Registro", target))
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getActorId()).isEqualTo(actor);
                    assertThat(entry.getChangedAt()).isNotNull();
                    assertThat(entry.getField()).isEqualTo("telefono");
                    assertThat(entry.getOldValue()).isEqualTo("700000");
                    assertThat(entry.getNewValue()).isEqualTo("711111");
                });
    }

    @Test
    void recordsACreationWithEmptyOldValues() {
        UUID target = UUID.randomUUID();

        transaction.executeWithoutResult(status -> changeLog.record(UUID.randomUUID(), "Registro", target,
                List.of(FieldChange.created("nombre", "Ana"), FieldChange.created("correo", "ana@uagrm.edu.bo"))));

        assertThat(entries.findByTargetTypeAndTargetIdOrderByChangedAt("Registro", target))
                .hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getOldValue()).isNull());
    }

    @Test
    void keepsNothingWhenTheTransactionIsRolledBack() {
        UUID target = UUID.randomUUID();

        transaction.executeWithoutResult(status -> {
            changeLog.record(UUID.randomUUID(), "Registro", target, List.of(FieldChange.created("nombre", "Ana")));
            status.setRollbackOnly();
        });

        assertThat(entries.findByTargetTypeAndTargetIdOrderByChangedAt("Registro", target)).isEmpty();
    }

    @Test
    void requiresATransactionInProgress() {
        assertThatThrownBy(() -> changeLog.record(UUID.randomUUID(), "Registro", UUID.randomUUID(),
                List.of(FieldChange.created("nombre", "Ana"))))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    @Test
    void theRepositoryOffersNoWayToModifyOrDeleteEntries() {
        assertThat(Arrays.stream(ChangeLogEntryRepository.class.getMethods()).map(Method::getName))
                .noneMatch(name -> name.startsWith("delete") || name.startsWith("update")
                        || name.startsWith("saveAll") || name.startsWith("merge"));
        assertThat(Arrays.stream(ChangeLogEntry.class.getMethods()).map(Method::getName))
                .noneMatch(name -> name.startsWith("set"));
    }

    @Test
    void entriesCannotBeChangedWithDirectSql() {
        UUID target = UUID.randomUUID();
        transaction.executeWithoutResult(status -> changeLog.record(UUID.randomUUID(), "Registro", target,
                List.of(FieldChange.created("nombre", "Ana"))));

        assertThatThrownBy(() -> jdbc.update("UPDATE change_log_entries SET new_value = 'Otra' WHERE target_id = ?", target))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update("DELETE FROM change_log_entries WHERE target_id = ?", target))
                .isInstanceOf(DataAccessException.class);
        assertThat(entries.findByTargetTypeAndTargetIdOrderByChangedAt("Registro", target))
                .singleElement()
                .satisfies(entry -> assertThat(entry.getNewValue()).isEqualTo("Ana"));
    }

    @Test
    void returnsTheHistoryOfARecordInOrder() {
        UUID actor = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        transaction.executeWithoutResult(status -> changeLog.record(actor, "Registro", target,
                List.of(FieldChange.created("nombre", "Ana"), FieldChange.created("telefono", "700000"))));
        transaction.executeWithoutResult(status -> changeLog.record(actor, "Registro", target,
                List.of(new FieldChange("telefono", "700000", "711111"))));
        transaction.executeWithoutResult(status -> changeLog.record(actor, "Registro", target,
                List.of(new FieldChange("nombre", "Ana", "Ana María"))));

        List<LoggedChange> history = changeLog.history("Registro", target);

        assertThat(history).extracting(LoggedChange::field, LoggedChange::oldValue, LoggedChange::newValue)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("nombre", null, "Ana"),
                        org.assertj.core.groups.Tuple.tuple("telefono", null, "700000"),
                        org.assertj.core.groups.Tuple.tuple("telefono", "700000", "711111"),
                        org.assertj.core.groups.Tuple.tuple("nombre", "Ana", "Ana María"));
        assertThat(history).allSatisfy(change -> assertThat(change.actorId()).isEqualTo(actor));
        assertThat(history.get(1).changedAt()).isBefore(history.get(2).changedAt());
    }

    @Test
    void theEntriesOfOneChangeShareTheirDateAndTime() {
        UUID target = UUID.randomUUID();
        transaction.executeWithoutResult(status -> changeLog.record(UUID.randomUUID(), "Registro", target,
                List.of(FieldChange.created("a", "1"), FieldChange.created("b", "2"), FieldChange.created("c", "3"))));

        assertThat(changeLog.history("Registro", target)).extracting(LoggedChange::changedAt).hasSize(3)
                .containsOnly(changeLog.history("Registro", target).getFirst().changedAt());
    }

    @Test
    void theHistoryOfARecordWithoutEntriesIsEmpty() {
        assertThat(changeLog.history("Registro", UUID.randomUUID())).isEmpty();
    }
}
