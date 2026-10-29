package grupo03sc.mailapp.users.application;

import grupo03sc.mailapp.audit.application.ChangeLog;
import grupo03sc.mailapp.users.domain.AccountStatus;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the initial administrator account on startup when no active one exists (FR-038, D8).
 */
@Component
@EnableConfigurationProperties(InitialAdministratorProperties.class)
class InitialAdministratorCreator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdministratorCreator.class);

    private final UserAccountRepository accounts;
    private final ChangeLog changeLog;
    private final InitialAdministratorProperties properties;

    InitialAdministratorCreator(UserAccountRepository accounts, ChangeLog changeLog,
            InitialAdministratorProperties properties) {
        this.accounts = accounts;
        this.changeLog = changeLog;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (accounts.existsWithRoleAndStatus(Role.ADMINISTRATOR, AccountStatus.ACTIVE)) {
            return;
        }
        List<String> missing = new ArrayList<>();
        if (isBlank(properties.name())) {
            missing.add("app.initial-admin.name (INITIAL_ADMIN_NAME)");
        }
        if (isBlank(properties.email())) {
            missing.add("app.initial-admin.email (INITIAL_ADMIN_EMAIL)");
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "No active administrator account exists and these settings are missing: " + String.join(", ", missing));
        }
        UserAccount admin = accounts.saveAndFlush(
                UserAccount.create(properties.name(), properties.email(), Set.of(Role.ADMINISTRATOR)));
        changeLog.record(admin.getId(), "UserAccount", admin.getId(), admin.describe());
        log.info("Initial administrator account created for {}", admin.getEmail());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
