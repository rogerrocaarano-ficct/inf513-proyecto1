package bo.org.tecnoweb.cpms.storage.infrastructure.persistence;

import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter of {@link StoredFileRepository} (issue #9). It saves metadata only; the bytes are kept
 * by {@link bo.org.tecnoweb.cpms.storage.application.FileStorage}.
 */
@Repository
class JpaStoredFileRepository implements StoredFileRepository {

    private final SpringDataStoredFileRepository files;

    /**
     * Creates the adapter.
     *
     * @param files Spring Data repository of the entities
     */
    JpaStoredFileRepository(SpringDataStoredFileRepository files) {
        this.files = files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void insert(StoredFile file) {
        files.save(new StoredFileEntity(file));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void update(StoredFile file) {
        StoredFileEntity entity = files.findById(file.getId())
                .orElseThrow(() -> new IllegalStateException("No stored file with id " + file.getId()));
        entity.update(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<StoredFile> getById(UUID id) {
        return files.findById(id).map(StoredFileEntity::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Optional<StoredFile> getByIdForUpdate(UUID id) {
        return files.findForUpdateById(id).map(StoredFileEntity::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<StoredFile> getByFirstVersionId(UUID firstVersionId) {
        return files.findByFirstVersionIdOrderByStoredAtAsc(firstVersionId).stream()
                .map(StoredFileEntity::toDomain).toList();
    }
}
