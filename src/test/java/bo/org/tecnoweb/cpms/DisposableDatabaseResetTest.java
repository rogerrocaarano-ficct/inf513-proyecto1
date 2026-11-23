package bo.org.tecnoweb.cpms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Reset of the database in the disposable environments (requirements "Entorno local limpio en cada arranque",
 * "Entorno limpio en cada arranque" and "Solo en entornos desechables").
 */
class DisposableDatabaseResetTest {

    @ParameterizedTest
    @ValueSource(strings = {"local", "staging"})
    void disposableProfilesCleanTheSchemaBeforeMigrating(String profile) {
        contextWithProfiles(profile).run(context -> {
            Flyway flyway = mock(Flyway.class);

            context.getBean(FlywayMigrationStrategy.class).migrate(flyway);

            InOrder order = inOrder(flyway);
            order.verify(flyway).clean();
            order.verify(flyway).migrate();
        });
    }

    @Test
    void theDefaultProfileDoesNotCleanTheSchema() {
        contextWithProfiles()
                .run(context -> assertThat(context).doesNotHaveBean(FlywayMigrationStrategy.class));
    }

    private ApplicationContextRunner contextWithProfiles(String... profiles) {
        return new ApplicationContextRunner()
                .withInitializer(context -> context.getEnvironment().setActiveProfiles(profiles))
                .withUserConfiguration(DisposableDatabaseReset.class);
    }
}
