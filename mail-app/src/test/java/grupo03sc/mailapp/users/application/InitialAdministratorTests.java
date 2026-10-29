package grupo03sc.mailapp.users.application;

import static org.assertj.core.api.Assertions.assertThat;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.audit.application.ChangeLogEntryRepository;
import grupo03sc.mailapp.audit.domain.ChangeLogEntry;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class InitialAdministratorTests {

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private ChangeLogEntryRepository changeLog;

    @Autowired
    private InitialAdministratorCreator creator;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void createsTheInitialAdministratorOnStartupAndLogsIt() {
        UserAccount admin = accounts.findByEmail("admin@mailapp.test").orElseThrow();

        assertThat(admin.getFullName()).isEqualTo("Administrador de Pruebas");
        assertThat(admin.getRoles()).containsExactly(Role.ADMINISTRATOR);
        assertThat(admin.isActive()).isTrue();
        assertThat(changeLog.findByTargetTypeAndTargetIdOrderByChangedAt("UserAccount", admin.getId()))
                .extracting(ChangeLogEntry::getField)
                .containsExactlyInAnyOrder("fullName", "email", "roles", "status");
    }

    @Test
    void doesNotCreateAnotherAdministratorWhenOneIsActive() {
        creator.run(new DefaultApplicationArguments());

        Integer admins = jdbc.queryForObject(
                "SELECT count(*) FROM user_account_roles WHERE role = 'ADMINISTRATOR'", Integer.class);
        assertThat(admins).isEqualTo(1);
    }
}
