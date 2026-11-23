package bo.org.tecnoweb.cpms.storage.infrastructure;

import bo.org.tecnoweb.cpms.storage.application.FileStorage;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring of the storage subsystem (issue #9).
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(StorageProperties.class)
class StorageConfiguration {

    /**
     * Creates the single entry point to file storage.
     *
     * @param files   JPA adapter of the metadata
     * @param storage disk adapter of the bytes
     * @return the service
     */
    @Bean
    StoredFileService storedFileService(StoredFileRepository files, FileStorage storage) {
        return new StoredFileService(files, storage);
    }
}
