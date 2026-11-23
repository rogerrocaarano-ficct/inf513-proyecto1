package bo.org.tecnoweb.cpms.storage.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Emptying of the stored files in the disposable environments (requirements "Entorno local limpio en cada
 * arranque" and "Solo en entornos desechables").
 */
class DisposableStorageResetTest {

    @TempDir
    Path root;

    @Test
    void deletesFilesAndFoldersButKeepsTheRoot() throws IOException {
        Files.writeString(root.resolve("a.pdf"), "a");
        Files.createDirectories(root.resolve("2026/11"));
        Files.writeString(root.resolve("2026/11/b.pdf"), "b");

        new DisposableStorageReset(new StorageProperties(root.toString()));

        assertThat(root).isEmptyDirectory();
    }

    @Test
    void doesNothingWhenTheRootDoesNotExist() {
        Path missing = root.resolve("missing");

        new DisposableStorageReset(new StorageProperties(missing.toString()));

        assertThat(missing).doesNotExist();
    }

    @ParameterizedTest
    @ValueSource(strings = {"local", "staging"})
    void disposableProfilesEmptyTheFolderAtStartup(String profile) throws IOException {
        Files.writeString(root.resolve("a.pdf"), "a");

        contextWithProfiles(profile).run(context -> {
            assertThat(context).hasSingleBean(DisposableStorageReset.class);
            assertThat(root).isEmptyDirectory();
        });
    }

    @Test
    void theDefaultProfileKeepsTheFiles() throws IOException {
        Files.writeString(root.resolve("a.pdf"), "a");

        contextWithProfiles().run(context -> {
            assertThat(context).doesNotHaveBean(DisposableStorageReset.class);
            assertThat(root.resolve("a.pdf")).exists();
        });
    }

    private ApplicationContextRunner contextWithProfiles(String... profiles) {
        return new ApplicationContextRunner()
                .withInitializer(context -> context.getEnvironment().setActiveProfiles(profiles))
                .withBean(StorageProperties.class, () -> new StorageProperties(root.toString()))
                .withUserConfiguration(DisposableStorageReset.class);
    }
}
