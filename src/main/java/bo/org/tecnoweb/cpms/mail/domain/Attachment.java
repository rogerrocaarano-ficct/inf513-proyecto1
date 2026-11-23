package bo.org.tecnoweb.cpms.mail.domain;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Attachment of a {@link ReceivedEmail}: a value object with no identity of its own, identified by
 * its position in the email.
 *
 * <p>An attachment just read from the mailbox is {@link Content}: its name, media type and bytes.
 * Once the mail channel keeps the bytes in file storage (issue #9), it becomes {@link Stored}: only
 * the identifier of the stored file, which is all an email keeps outside
 * {@link ReceivedEmailState#FETCHED}.
 */
public sealed interface Attachment permits Attachment.Content, Attachment.Stored {

    /**
     * Content of an attachment just read from the mailbox, not stored yet.
     *
     * @param name        the file name, as sent by the user
     * @param contentType the media type declared by the email, without parameters
     * @param data        the bytes of the file; {@code null} is kept as an empty content
     */
    record Content(String name, String contentType, byte[] data) implements Attachment {

        /**
         * Creates the content, keeping a {@code null} content as empty.
         */
        public Content {
            data = data == null ? new byte[0] : data;
        }

        /**
         * Tells whether the attachment has no content at all, which the channel rejects before
         * keeping anything (issue #9).
         *
         * @return {@code true} if the content has 0 bytes
         */
        public boolean isEmpty() {
            return data.length == 0;
        }

        /**
         * Compares by name, media type and the bytes of the content.
         *
         * @param other the object to compare with
         * @return {@code true} if both attachments have the same name, media type and bytes
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof Content that && Objects.equals(name, that.name)
                    && Objects.equals(contentType, that.contentType) && Arrays.equals(data, that.data);
        }

        /**
         * Hashes the name, media type and bytes of the content.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(name, contentType, Arrays.hashCode(data));
        }

        /**
         * Describes the attachment with the size of its content instead of its bytes.
         *
         * @return a short description
         */
        @Override
        public String toString() {
            return "Content[name=" + name + ", contentType=" + contentType + ", size=" + data.length + "]";
        }
    }

    /**
     * Attachment already kept in file storage (issue #9).
     *
     * @param storedFileId the identifier of the stored file
     */
    record Stored(UUID storedFileId) implements Attachment {

        /**
         * Creates the reference.
         *
         * @throws NullPointerException if {@code storedFileId} is {@code null}
         */
        public Stored {
            Objects.requireNonNull(storedFileId, "storedFileId");
        }
    }
}
