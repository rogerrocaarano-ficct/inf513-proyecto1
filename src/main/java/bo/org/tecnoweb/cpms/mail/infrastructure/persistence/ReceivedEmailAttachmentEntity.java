package bo.org.tecnoweb.cpms.mail.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * JPA entity of a {@link bo.org.tecnoweb.cpms.mail.domain.File}, table {@code received_email_attachment}.
 * Its position and owner are managed by {@link ReceivedEmailEntity}.
 *
 * <p>It keeps only the reference to the file in which file storage keeps the content; the name,
 * type and size live in {@code stored_file} (issue #9).
 */
@Entity
@Table(name = "received_email_attachment")
class ReceivedEmailAttachmentEntity {

    @Id
    private UUID id;

    @Column(name = "stored_file_id", nullable = false, unique = true)
    private UUID storedFileId;

    /**
     * Constructor required by JPA.
     */
    protected ReceivedEmailAttachmentEntity() {
    }

    /**
     * Creates the entity of an attachment.
     *
     * @param id           identifier of the domain file
     * @param storedFileId identifier of the stored file with its content
     */
    ReceivedEmailAttachmentEntity(UUID id, UUID storedFileId) {
        this.id = id;
        this.storedFileId = storedFileId;
    }

    UUID getId() {
        return id;
    }

    UUID getStoredFileId() {
        return storedFileId;
    }
}
