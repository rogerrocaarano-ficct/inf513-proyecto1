package bo.org.tecnoweb.cpms.storage.domain;

import bo.org.tecnoweb.cpms.common.AggregateRoot;
import bo.org.tecnoweb.cpms.common.DomainEvent;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Metadata of a file kept on disk: the aggregate root of the storage subsystem (issue #9).
 *
 * <p>The bytes are not part of the aggregate: {@link #getPath()} is the key of the file, relative
 * to the storage root.
 *
 * <p>A new file is built fluently with {@link #builder()} and the {@code with*} methods, and then
 * registered with {@link #store()}, which validates it and assigns its key, its storage time and,
 * unless it is a version of another file, its chain. A file read from storage is built with
 * {@link #rehydrate()}.
 *
 * <p>Versions: each version is a different {@code StoredFile}. {@code nextVersionId} points to the
 * next version and is empty in the current one; {@code firstVersionId} identifies the chain, so its
 * whole history is found with one query. Only the current version can be superseded
 * ({@link #supersedeBy(StoredFile)}), so the chain never forks. The file raises no events.
 */
public class StoredFile implements AggregateRoot {

    /** Field name of the original file name in validation errors. */
    public static final String ORIGINAL_NAME = "nombre del archivo";
    /** Field name of the content type in validation errors. */
    public static final String CONTENT_TYPE = "tipo de contenido";
    /** Field name of the content in validation errors. */
    public static final String CONTENT = "contenido";

    private UUID id;
    private String path;
    private String originalName;
    private String contentType;
    private long size;
    private Instant storedAt;
    private UUID firstVersionId;
    private UUID nextVersionId;

    private StoredFile() {
    }

    /**
     * Starts a new file with a new identity.
     *
     * @return an empty file with a random identifier, to complete with the {@code with*} methods
     */
    public static StoredFile builder() {
        StoredFile file = new StoredFile();
        file.id = UUID.randomUUID();
        return file;
    }

    /**
     * Starts a file that already exists, read from storage.
     *
     * @return an empty file whose fields must be set with the {@code with*} methods
     */
    public static StoredFile rehydrate() {
        return new StoredFile();
    }

    /**
     * Sets the identity.
     *
     * @param id the identifier
     * @return this file
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public StoredFile withId(UUID id) {
        this.id = Objects.requireNonNull(id);
        return this;
    }

    /**
     * Sets the key of the file, relative to the storage root. Only for a file read from storage;
     * {@link #store()} assigns it to a new one.
     *
     * @param path the key
     * @return this file
     */
    public StoredFile withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Sets the name the file arrived with, for example the name of an email attachment.
     *
     * @param originalName the name
     * @return this file
     */
    public StoredFile withOriginalName(String originalName) {
        this.originalName = originalName == null ? null : originalName.strip();
        return this;
    }

    /**
     * Sets the media type of the content, for example {@code application/pdf}.
     *
     * @param contentType the media type
     * @return this file
     */
    public StoredFile withContentType(String contentType) {
        this.contentType = contentType == null ? null : contentType.strip().toLowerCase(Locale.ROOT);
        return this;
    }

    /**
     * Sets the size of the content.
     *
     * @param size the size in bytes
     * @return this file
     */
    public StoredFile withSize(long size) {
        this.size = size;
        return this;
    }

    /**
     * Sets when the file was stored. Only for a file read from storage; {@link #store()} assigns it to
     * a new one.
     *
     * @param storedAt the storage time
     * @return this file
     */
    public StoredFile withStoredAt(Instant storedAt) {
        this.storedAt = storedAt;
        return this;
    }

    /**
     * Sets the identifier of the first version of the chain. Only for a file read from storage;
     * {@link #store()} or {@link #supersedeBy(StoredFile)} assign it to a new one.
     *
     * @param firstVersionId the identifier of the first version
     * @return this file
     */
    public StoredFile withFirstVersionId(UUID firstVersionId) {
        this.firstVersionId = firstVersionId;
        return this;
    }

    /**
     * Sets the identifier of the next version. Only for a file read from storage;
     * {@link #supersedeBy(StoredFile)} assigns it.
     *
     * @param nextVersionId the identifier of the next version, or {@code null} for the current one
     * @return this file
     */
    public StoredFile withNextVersionId(UUID nextVersionId) {
        this.nextVersionId = nextVersionId;
        return this;
    }

    /**
     * Validates a new file and registers it: assigns its storage time, its key
     * {@code yyyy/MM/<id>} (UTC, without extension, never derived from the original name) and, unless
     * it is already a version of another file, makes it the first version of its own chain.
     *
     * <p>Size and format limits of each kind of document are checked by the use case that receives
     * it, not here.
     *
     * @return this file
     * @throws DomainValidationException if the original name or the content type is blank, or the
     *                                   content is empty, with one Spanish message per field
     * @throws IllegalStateException     if the file was already stored
     */
    public StoredFile store() {
        if (storedAt != null) {
            throw new IllegalStateException("File " + id + " is already stored");
        }
        Map<String, String> errors = new LinkedHashMap<>();
        if (originalName == null || originalName.isEmpty()) {
            errors.put(ORIGINAL_NAME, "Es obligatorio.");
        }
        if (contentType == null || contentType.isEmpty()) {
            errors.put(CONTENT_TYPE, "Es obligatorio.");
        }
        if (size <= 0) {
            errors.put(CONTENT, "El archivo está vacío.");
        }
        if (!errors.isEmpty()) {
            throw new DomainValidationException(errors);
        }
        // Microseconds: the precision of the database, so the stored value equals this one.
        storedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        ZonedDateTime utc = storedAt.atZone(ZoneOffset.UTC);
        path = String.format(Locale.ROOT, "%04d/%02d/%s", utc.getYear(), utc.getMonthValue(), id);
        if (firstVersionId == null) {
            firstVersionId = id;
        }
        return this;
    }

    /**
     * Links the next version to this one, which stops being the current version. The next version
     * joins the chain of this one.
     *
     * <p>The next version must be a standalone file: the first version of its own chain, with no
     * next version, and not this file. A file that already belongs to another chain would make two
     * chains share it.
     *
     * @param next the new version
     * @throws StoredFileNotCurrentException       if this version already has a next one
     * @throws StoredFileAlreadyVersionedException if {@code next} is not a standalone file
     * @throws NullPointerException                if {@code next} is {@code null}
     */
    public void supersedeBy(StoredFile next) {
        Objects.requireNonNull(next);
        if (nextVersionId != null) {
            throw new StoredFileNotCurrentException(id);
        }
        if (next.id.equals(id) || !next.id.equals(next.firstVersionId) || !next.isCurrent()) {
            throw new StoredFileAlreadyVersionedException(next.id);
        }
        nextVersionId = next.id;
        next.firstVersionId = firstVersionId;
    }

    /**
     * Tells whether this is the current version of its chain.
     *
     * @return {@code true} if no version follows this one
     */
    public boolean isCurrent() {
        return nextVersionId == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Returns the key of the file, relative to the storage root.
     *
     * @return the key, or {@code null} before {@link #store()}
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the name the file arrived with.
     *
     * @return the original name
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * Returns the media type of the content.
     *
     * @return the media type, in lower case
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the size of the content.
     *
     * @return the size in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns when the file was stored.
     *
     * @return the storage time, or {@code null} before {@link #store()}
     */
    public Instant getStoredAt() {
        return storedAt;
    }

    /**
     * Returns the identifier of the first version of the chain.
     *
     * @return the identifier of the first version; this file's own for a first version
     */
    public UUID getFirstVersionId() {
        return firstVersionId;
    }

    /**
     * Returns the identifier of the next version.
     *
     * @return the identifier of the next version, or {@code null} for the current one
     */
    public UUID getNextVersionId() {
        return nextVersionId;
    }

    /**
     * Returns no events: storing or versioning a file notifies nobody.
     *
     * @return an empty list
     */
    @Override
    public List<DomainEvent> pullEvents() {
        return List.of();
    }
}
