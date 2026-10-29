package grupo03sc.mailapp.messaging.application;

import grupo03sc.mailapp.messaging.domain.ProcessedMailMessage;
import java.util.UUID;
import org.springframework.data.repository.Repository;

public interface ProcessedMailMessageRepository extends Repository<ProcessedMailMessage, UUID> {

    ProcessedMailMessage save(ProcessedMailMessage message);

    boolean existsByMessageId(String messageId);
}
