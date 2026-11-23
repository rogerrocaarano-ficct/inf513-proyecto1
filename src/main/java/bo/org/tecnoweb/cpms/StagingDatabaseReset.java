package bo.org.tecnoweb.cpms;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Empties the database at every startup of the deployed test environment (capability
 * {@code test-environment}, requirement "Entorno limpio en cada arranque").
 *
 * <p>Active only with the {@code staging} profile ({@code compose.staging.yaml}). The mail server of that
 * environment (GreenMail) keeps the mail in memory and numbers the POP3 messages from 1 again when it
 * restarts; a database that kept the received emails would make the mail channel drop new commands with a
 * reused UIDL. So the schema is cleaned and migrated again before the application starts, and the test
 * accounts are created afterwards (UC-001). {@code spring.flyway.clean-disabled=false} is set only in
 * {@code application-staging.properties}.
 */
@Configuration(proxyBeanMethods = false)
@Profile("staging")
class StagingDatabaseReset {

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
