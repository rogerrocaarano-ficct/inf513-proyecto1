package bo.org.tecnoweb.cpms.storage.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/**
 * Spring Data repository of {@link StoredFileEntity}, used only by {@link JpaStoredFileRepository}.
 */
interface SpringDataStoredFileRepository extends JpaRepository<StoredFileEntity, UUID> {

    /**
     * Finds and locks a file, so two new versions of it are not created at the same time.
     *
     * @param id the identifier
     * @return the entity, or empty
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StoredFileEntity> findForUpdateById(UUID id);

    /**
     * Finds every version of a chain.
     *
     * @param firstVersionId the identifier of the first version
     * @return the entities, from the oldest to the newest
     */
    List<StoredFileEntity> findByFirstVersionIdOrderByStoredAtAsc(UUID firstVersionId);
}
