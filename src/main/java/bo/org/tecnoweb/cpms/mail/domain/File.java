package bo.org.tecnoweb.cpms.mail.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Attachment of a {@link ReceivedEmail}.
 *
 * <p>An attachment just read from the mailbox carries its name, media type and bytes. The mail
 * channel then keeps the bytes in file storage and records here the identifier of the stored file
 * (issue #9); only that identifier is persisted with the email. An attachment read back from the
 * database has its identity and stored file, but no name, type or bytes. An outgoing attachment
 * carries only its name and bytes.
 *
 * <p>Built with {@link #builder()} for a new attachment or {@link #rehydrate()} for one read from
 * storage, and completed with the {@code with*} methods.
 */
public class File {

    private UUID id;
    private String name;
    private String contentType;
    private byte[] data;
    private UUID storedFileId;

    private File() {
    }

    /**
     * Starts a new attachment with a new identity.
     *
     * @return a new attachment with a random identifier
     */
    public static File builder() {
        File file = new File();
        file.id = UUID.randomUUID();
        return file;
    }

    /**
     * Starts an attachment that already exists, read from storage.
     *
     * @return an empty attachment whose identity must be set with {@link #withId(UUID)}
     */
    public static File rehydrate() {
        return new File();
    }

    /**
     * Sets the identity.
     *
     * @param id the identifier
     * @return this attachment
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public File withId(UUID id) {
        this.id = Objects.requireNonNull(id);
        return this;
    }

    /**
     * Sets the file name.
     *
     * @param name the file name, as sent by the user
     * @return this attachment
     */
    public File withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the media type of the content.
     *
     * @param contentType the media type declared by the email, without parameters
     * @return this attachment
     */
    public File withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Sets the content.
     *
     * @param data the bytes of the file; {@code null} is kept as an empty content
     * @return this attachment
     */
    public File withData(byte[] data) {
        this.data = data == null ? new byte[0] : data;
        return this;
    }

    /**
     * Sets the file in which file storage keeps the content (issue #9).
     *
     * @param storedFileId the identifier of the stored file
     * @return this attachment
     */
    public File withStoredFileId(UUID storedFileId) {
        this.storedFileId = storedFileId;
        return this;
    }

    /**
     * Tells whether the attachment was read with no content at all.
     *
     * @return {@code true} if the content is known and has 0 bytes; {@code false} if it has bytes or
     *         was not loaded (an attachment read back from the database)
     */
    public boolean isEmpty() {
        return data != null && data.length == 0;
    }

    /**
     * Returns the identity.
     *
     * @return the identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the file name.
     *
     * @return the file name, or {@code null} for an attachment read back from the database
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the media type of the content.
     *
     * @return the media type, or {@code null} if it is not known
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the content.
     *
     * @return the bytes of the file, or {@code null} for an attachment read back from the database
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the file in which file storage keeps the content.
     *
     * @return the identifier of the stored file, or {@code null} before the channel stores it
     */
    public UUID getStoredFileId() {
        return storedFileId;
    }
}
