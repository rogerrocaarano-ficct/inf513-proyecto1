package grupo03sc.mailapp.storage;

import grupo03sc.mailapp.storage.application.FileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FileStorageProperties.class)
class StorageConfiguration {
}
