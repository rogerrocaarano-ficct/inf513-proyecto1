package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.procedures.domain.InitialDocument;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DegreeProcedureRepository {
    Optional<UUID> lockAccountByEmail(String email);

    boolean isStudentCodeTaken(String code);

    Optional<Long> open(UUID student, UUID requester, Map<InitialDocument, File> documents);

    boolean replace(long reference, UUID requester, InitialDocument type, File document);
}
