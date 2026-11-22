package bo.org.tecnoweb.cpms.storage.infrastructure.persistence;

import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity of a {@link StoredFile}, table {@code stored_file} (issue #9). It holds metadata only,
 * never the bytes.
 *
 * <p>Accessors are package-private: only {@link JpaStoredFileRepository} uses this class.
 */
@Entity
@Table(name = "stored_file")
class StoredFileEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String path;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(name = "stored_at", nullable = false)
    private Instant storedAt;

    @Column(name = "first_version_id", nullable = false)
    private UUID firstVersionId;

    @Column(name = "next_version_id", unique = true)
    private UUID nextVersionId;

    /**
     * Constructor required by JPA.
     */
    protected StoredFileEntity() {
    }

    /**
     * Creates the entity of a new file, with every field of the domain file.
     *
     * @param file the stored file
     */
    StoredFileEntity(StoredFile file) {
        this.id = file.getId();
        this.path = file.getPath();
        this.originalName = file.getOriginalName();
        this.contentType = file.getContentType();
        this.size = file.getSize();
        this.storedAt = file.getStoredAt();
        this.firstVersionId = file.getFirstVersionId();
        this.nextVersionId = file.getNextVersionId();
    }

    /**
     * Copies the only field that changes after a file is stored: its next version.
     *
     * @param file the domain file
     */
    void update(StoredFile file) {
        this.nextVersionId = file.getNextVersionId();
    }

    /**
     * Rebuilds the domain file.
     *
     * @return the file, without events
     */
    StoredFile toDomain() {
        return StoredFile.rehydrate().withId(id).withPath(path).withOriginalName(originalName)
                .withContentType(contentType).withSize(size).withStoredAt(storedAt)
                .withFirstVersionId(firstVersionId).withNextVersionId(nextVersionId);
    }
}
