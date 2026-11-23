package bo.org.tecnoweb.cpms.users.infrastructure;

import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Creates the test accounts of the local development environment at startup, so that every role of
 * UC-001 can be tried without creating the accounts by email first.
 *
 * <p>Runs only with the {@code local} profile ({@code ./gradlew bootRun}): never in production nor in the
 * automated tests. Each account goes through {@link User#signUp(User)}, so it follows the same data rules
 * as an account created by email, but its events are discarded: no email is sent. An address that already
 * has an account is skipped, and that account is never changed. The administrator is not created here: it
 * comes from {@link InitialAdminAccount}.
 */
@Component
@Profile("local")
class LocalTestAccounts implements ApplicationRunner {

    /** Domain of every test account. */
    static final String DOMAIN = "@test.local";

    private static final Logger log = LoggerFactory.getLogger(LocalTestAccounts.class);

    private final UserRepository users;

    /**
     * Creates the runner.
     *
     * @param users storage of the accounts
     */
    LocalTestAccounts(UserRepository users) {
        this.users = users;
    }

    /**
     * Creates the test accounts that do not exist yet.
     *
     * @param args application arguments, not used
     */
    @Override
    public void run(ApplicationArguments args) {
        User requester = User.builder()
                .withEmailAddress("seed" + DOMAIN)
                .withRole(Role.SYSTEM_ADMIN)
                .withFirstName("Datos")
                .withLastName("Prueba");
        int created = 0;
        for (User account : accounts()) {
            if (users.getByEmail(account.getEmailAddress()).isPresent()) {
                continue;
            }
            account.signUp(requester);
            account.pullEvents();
            users.insert(account);
            created++;
        }
        log.info("Created {} local test accounts ({})", created, DOMAIN);
    }

    /**
     * Returns the test accounts, not yet signed up: one administrative staff member, one capstone project
     * professor, one director, five professors and three students.
     *
     * @return new account instances, in that order
     */
    static List<User> accounts() {
        return List.of(
                staff("administrativo", Role.ADMINISTRATIVE, "Carla", "Méndez", "Suárez"),
                staff("taller", Role.CAPSTONE_PROJECT_PROFESSOR, "Jorge", "Salvatierra", "Ríos"),
                staff("director", Role.DIRECTOR, "Mónica", "Vargas", "Peña"),
                staff("docente1", Role.PROFESSOR, "Luis", "Rojas", "Antelo"),
                staff("docente2", Role.PROFESSOR, "Patricia", "Gutiérrez", "Lima"),
                staff("docente3", Role.PROFESSOR, "Ricardo", "Flores", "Castro"),
                staff("docente4", Role.PROFESSOR, "Silvia", "Mercado", "Rocha"),
                staff("docente5", Role.PROFESSOR, "Fernando", "Justiniano", "Paz"),
                student(1, "Ana María", "Pérez", "Rojas"),
                student(2, "Diego", "Arteaga", "Soliz"),
                student(3, "Valeria", "Montaño", "Cuéllar"));
    }

    private static User staff(String mailbox, Role role, String firstName, String lastName,
            String secondLastName) {
        return User.builder()
                .withEmailAddress(mailbox + DOMAIN)
                .withRole(role)
                .withFirstName(firstName)
                .withLastName(lastName)
                .withSecondLastName(secondLastName);
    }

    private static User student(int number, String firstName, String lastName, String secondLastName) {
        return staff("estudiante" + number, Role.STUDENT, firstName, lastName, secondLastName)
                .withCi("900000" + number)
                .withUagrmCode("90000000" + number)
                .withPhoneNumber("7000000" + number);
    }
}
