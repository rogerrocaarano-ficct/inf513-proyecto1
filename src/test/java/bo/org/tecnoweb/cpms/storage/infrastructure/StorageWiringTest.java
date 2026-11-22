package bo.org.tecnoweb.cpms.storage.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.storage.application.FileStorage;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * The application starts with the storage subsystem wired: its single entry point and the disk
 * adapter of the bytes.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StorageWiringTest {

    @Autowired
    private StoredFileService service;

    @Autowired
    private FileStorage storage;

    @Test
    void storedFileServiceIsAvailable() {
        assertThat(service).isNotNull();
        assertThat(storage).isInstanceOf(LocalDiskFileStorage.class);
    }
}
