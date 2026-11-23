package bo.org.tecnoweb.cpms.procedures;

import bo.org.tecnoweb.cpms.procedures.domain.Semester;
import bo.org.tecnoweb.cpms.storage.application.FileStorage;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileRepository;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.support.TransactionOperations;

/** Shared account and attachment inputs for UC-002, UC-003 and UC-004 tests. */
public final class ProcedureFixtures {
    private ProcedureFixtures() {}

    /** The semester of the openings in the tests. */
    public static final Semester SEMESTER = new Semester(2026, 2);

    /** The reference of the {@link #student()}'s procedure opened in {@link #SEMESTER}. */
    public static final String REFERENCE = "2026-2-123456";

    /** Returns an active secretary. */
    public static User secretary() {
        return User.builder().withRole(Role.ADMINISTRATIVE).withEmailAddress("secretaria@cpms.test")
                .withFirstName("Laura").withLastName("Rojas");
    }

    /** Returns an active student. */
    public static User student() {
        return User.builder().withRole(Role.STUDENT).withEmailAddress("ana@cpms.test")
                .withFirstName("Ana").withLastName("Pérez").withCi("1234")
                .withUagrmCode("123456").withPhoneNumber("7123456");
    }

    /** Returns the content of a test PDF: a PDF header followed by its name. */
    public static byte[] pdfContent(String name) {
        return ("%PDF-1.7\n" + name).getBytes(StandardCharsets.US_ASCII);
    }

    /** Returns the data of a stored test PDF, without its content, for domain tests. */
    public static StoredFile pdf(String name) {
        return StoredFile.builder().withOriginalName(name).withContentType("application/pdf")
                .withSize(pdfContent(name).length).store();
    }

    /** Returns the four stored PDFs required for opening, for domain tests. */
    public static List<StoredFile> documents() {
        return List.of(pdf("carta.pdf"), pdf("cv.pdf"), pdf("ci.pdf"), pdf("boleta.pdf"));
    }

    /** Stores a test PDF with the real file storage, for repository and integration tests. */
    public static StoredFile storedPdf(StoredFileService service, TransactionOperations transactions, String name) {
        return transactions.execute(status -> service.store(name, "application/pdf", pdfContent(name)));
    }

    /** Stores the four PDFs required for opening with the real file storage. */
    public static List<StoredFile> storedDocuments(StoredFileService service, TransactionOperations transactions) {
        return List.of(storedPdf(service, transactions, "carta.pdf"), storedPdf(service, transactions, "cv.pdf"),
                storedPdf(service, transactions, "ci.pdf"), storedPdf(service, transactions, "boleta.pdf"));
    }

    /**
     * A real {@link StoredFileService} over in-memory ports, as the mail channel leaves the files of
     * the attachments before a use case runs (issue #9).
     */
    public static final class Storage implements StoredFileRepository, FileStorage {
        private final Map<UUID, StoredFile> rows = new LinkedHashMap<>();
        private final Map<String, byte[]> content = new HashMap<>();
        /** Every write to the disk, by key. */
        public final List<String> writes = new ArrayList<>();
        /** The service the use cases receive. */
        public final StoredFileService service = new StoredFileService(this, this);

        /** Stores a file as the mail channel would and returns its identifier. */
        public UUID store(String name, byte[] bytes) {
            UUID id = service.store(name, "application/pdf", bytes).getId();
            writes.clear();
            return id;
        }

        /** Stores a valid test PDF and returns its identifier. */
        public UUID pdf(String name) {
            return store(name, pdfContent(name));
        }

        /** Stores the four PDFs required for opening and returns their identifiers. */
        public List<UUID> documents() {
            return List.of(pdf("carta.pdf"), pdf("cv.pdf"), pdf("ci.pdf"), pdf("boleta.pdf"));
        }

        @Override
        public void insert(StoredFile file) {
            rows.put(file.getId(), copy(file));
        }

        @Override
        public void update(StoredFile file) {
            rows.put(file.getId(), copy(file));
        }

        @Override
        public Optional<StoredFile> getById(UUID id) {
            return Optional.ofNullable(rows.get(id)).map(Storage::copy);
        }

        @Override
        public Optional<StoredFile> getByIdForUpdate(UUID id) {
            return getById(id);
        }

        @Override
        public List<StoredFile> getByFirstVersionId(UUID firstVersionId) {
            return rows.values().stream().filter(f -> f.getFirstVersionId().equals(firstVersionId))
                    .map(Storage::copy).toList();
        }

        @Override
        public void write(String path, byte[] bytes) {
            writes.add(path);
            content.put(path, bytes.clone());
        }

        @Override
        public byte[] read(String path) {
            return content.get(path).clone();
        }

        private static StoredFile copy(StoredFile file) {
            return StoredFile.rehydrate().withId(file.getId()).withPath(file.getPath())
                    .withOriginalName(file.getOriginalName()).withContentType(file.getContentType())
                    .withSize(file.getSize()).withStoredAt(file.getStoredAt())
                    .withFirstVersionId(file.getFirstVersionId()).withNextVersionId(file.getNextVersionId());
        }
    }
}
