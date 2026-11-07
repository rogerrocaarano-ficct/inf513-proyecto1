package grupo03sc.mailapp.storage.application;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Root directory of the stored files ({@code app.files.root}, AD-012). */
@ConfigurationProperties("app.files")
public record FileStorageProperties(@DefaultValue("./data/files") Path root) {
}
