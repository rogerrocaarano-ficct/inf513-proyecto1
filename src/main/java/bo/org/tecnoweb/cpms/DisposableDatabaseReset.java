package bo.org.tecnoweb.cpms;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Empties the database at every startup of the disposable environments: the local development environment
 * and the deployed test environment (capabilities {@code local-environment} and {@code test-environment},
 * requirements "Entorno local limpio en cada arranque" and "Entorno limpio en cada arranque").
 *
 * <p>Active only with the {@code local} profile ({@code ./gradlew bootRun}) and the {@code staging} profile
 * ({@code compose.staging.yaml}). The mail server of those environments (GreenMail) keeps the mail in memory
 * and numbers the POP3 messages from 1 again when it is recreated; a database that kept the received emails
 * would make the mail channel drop new commands with a reused UIDL. So the schema is cleaned and migrated
 * again before the application starts, and the test accounts are created afterwards (UC-001).
 * {@code spring.flyway.clean-disabled=false} is set only in the properties of those two profiles. The stored
 * files are emptied by {@code storage.infrastructure.DisposableStorageReset}.
 */
@Configuration(proxyBeanMethods = false)
@Profile({"local", "staging"})
class DisposableDatabaseReset {

    /**
     * Returns the migration strategy that cleans the schema and then applies every migration.
     *
     * @return the strategy Flyway runs at startup
     */
    @Bean
    FlywayMigrationStrategy cleanThenMigrate() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
