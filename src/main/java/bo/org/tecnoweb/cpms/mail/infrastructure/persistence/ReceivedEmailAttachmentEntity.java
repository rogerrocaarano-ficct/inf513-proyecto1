package bo.org.tecnoweb.cpms.mail.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * JPA entity of a {@link bo.org.tecnoweb.cpms.mail.domain.File}, table {@code received_email_attachment}.
 * Its position and owner are managed by {@link ReceivedEmailEntity}.
 */
@Entity
@Table(name = "received_email_attachment")
class ReceivedEmailAttachmentEntity {

    @Id
    private UUID id;

    private String name;

    @Column(nullable = false)
    private byte[] data;

    /**
     * Constructor required by JPA.
     */
    protected ReceivedEmailAttachmentEntity() {
    }

    /**
     * Creates the entity of an attachment.
     *
     * @param id   identifier of the domain file
     * @param name file name
     * @param data content
     */
    ReceivedEmailAttachmentEntity(UUID id, String name, byte[] data) {
        this.id = id;
        this.name = name;
        this.data = data;
    }

    UUID getId() {
        return id;
    }

    String getName() {
        return name;
    }

    byte[] getData() {
        return data;
    }
}
