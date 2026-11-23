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

/** Reset of the database in the deployed test environment (requirement "Entorno limpio en cada arranque"). */
class StagingDatabaseResetTest {

    @Test
    void theStagingProfileCleansTheSchemaBeforeMigrating() {
        contextWithProfiles("staging").run(context -> {
            Flyway flyway = mock(Flyway.class);

            context.getBean(FlywayMigrationStrategy.class).migrate(flyway);

            InOrder order = inOrder(flyway);
            order.verify(flyway).clean();
            order.verify(flyway).migrate();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"local", "default"})
    void otherProfilesDoNotCleanTheSchema(String profile) {
        contextWithProfiles(profile)
                .run(context -> assertThat(context).doesNotHaveBean(FlywayMigrationStrategy.class));
    }

    private ApplicationContextRunner contextWithProfiles(String... profiles) {
        return new ApplicationContextRunner()
                .withInitializer(context -> context.getEnvironment().setActiveProfiles(profiles))
                .withUserConfiguration(StagingDatabaseReset.class);
    }
}
