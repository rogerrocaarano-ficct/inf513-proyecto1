package bo.org.tecnoweb.cpms.mail.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Attachment of a {@link ReceivedEmail}.
 *
 * <p>Built with {@link #builder()} for a new attachment or {@link #rehydrate()} for one read from
 * storage, and completed with the {@code with*} methods.
 */
public class File {

    private UUID id;
    private String name;
    private byte[] data;

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
     * Sets the content.
     *
     * @param data the bytes of the file; {@code null} is stored as an empty content
     * @return this attachment
     */
    public File withData(byte[] data) {
        this.data = data == null ? new byte[0] : data;
        return this;
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
     * @return the file name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the content.
     *
     * @return the bytes of the file
     */
    public byte[] getData() {
        return data;
    }
}
