package bo.org.tecnoweb.cpms.storage.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings of file storage, bound from {@code cpms.storage.*} (issue #9).
 *
 * @param root folder where the files are kept; required. In production it comes from the
 *             environment variable {@code CPMS_STORAGE_ROOT}
 */
@ConfigurationProperties("cpms.storage")
public record StorageProperties(String root) {
}
