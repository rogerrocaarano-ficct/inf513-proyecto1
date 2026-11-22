package bo.org.tecnoweb.cpms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
    }

    /**
     * Keeps the stored files of the tests and of {@code bootTestRun} in a throwaway folder (issue #9).
     *
     * @return the registrar of {@code cpms.storage.root}
     * @throws IOException if the folder cannot be created
     */
    @Bean
    DynamicPropertyRegistrar storageRoot() throws IOException {
        Path root = Files.createTempDirectory("cpms-storage");
        return registry -> registry.add("cpms.storage.root", root::toString);
    }
}
