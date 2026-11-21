package bo.org.tecnoweb.cpms.users.infrastructure;

import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first administrator account at startup, so that someone can create the others.
 *
 * <p>Reads {@code cpms.users.admin-email} ({@code CPMS_ADMIN_EMAIL}). If it has a value and no account
 * uses it, creates a {@link Role#SYSTEM_ADMIN} account with it, without requester or email notice. An
 * existing account is never changed.
 */
@Component
class InitialAdminAccount implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminAccount.class);

    private final UserRepository users;
    private final String adminEmail;

    /**
     * Creates the runner.
     *
     * @param users      storage of the accounts
     * @param adminEmail address of the first administrator; empty to create none
     */
    InitialAdminAccount(UserRepository users, @Value("${cpms.users.admin-email:}") String adminEmail) {
        this.users = users;
        this.adminEmail = adminEmail;
    }

    /**
     * Creates the administrator account if needed.
     *
     * @param args application arguments, not used
     */
    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }
        if (users.getByEmail(adminEmail).isPresent()) {
            return;
        }
        User admin = User.builder()
                .withEmailAddress(adminEmail)
                .withRole(Role.SYSTEM_ADMIN)
                .withFirstName("Administrador")
                .withLastName("Sistema");
        users.insert(admin);
        log.info("Created the initial administrator account {}", admin.getEmailAddress());
    }
}
