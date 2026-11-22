package bo.org.tecnoweb.cpms.storage.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileNotCurrentException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileNotFoundException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileRepository;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StoredFileServiceTest {

    private final List<String> calls = new ArrayList<>();
    private final InMemoryFiles files = new InMemoryFiles();
    private final InMemoryStorage storage = new InMemoryStorage();
    private final StoredFileService service = new StoredFileService(files, storage);

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void storeWritesTheBytesAndThenInsertsTheMetadata() {
        StoredFile file = service.store("perfil.pdf", "application/pdf", bytes("v1"));

        assertThat(calls).containsExactly("write " + file.getPath(), "insert " + file.getId());
        assertThat(storage.content.get(file.getPath())).isEqualTo(bytes("v1"));
        assertThat(file.getSize()).isEqualTo(2);
        assertThat(file.getFirstVersionId()).isEqualTo(file.getId());
    }

    @Test
    void invalidFileNeverReachesTheDisk() {
        assertThatThrownBy(() -> service.store("", "application/pdf", new byte[0]))
                .isInstanceOf(DomainValidationException.class);

        assertThat(calls).isEmpty();
    }

    @Test
    void newVersionLocksThePreviousOneAndLinksIt() {
        StoredFile first = service.store("perfil.pdf", "application/pdf", bytes("v1"));
        calls.clear();

        StoredFile second = service.storeNewVersion(first.getId(), "perfil-corregido.pdf", "application/pdf",
                bytes("v2"));

        assertThat(calls).containsExactly("lock " + first.getId(), "write " + second.getPath(),
                "insert " + second.getId(), "update " + first.getId());
        assertThat(files.saved.get(first.getId()).getNextVersionId()).isEqualTo(second.getId());
        assertThat(second.getFirstVersionId()).isEqualTo(first.getId());
        assertThat(second.isCurrent()).isTrue();
        assertThat(storage.content.get(first.getPath())).isEqualTo(bytes("v1"));
    }

    @Test
    void newVersionOfAnOldVersionIsRejectedBeforeWriting() {
        StoredFile first = service.store("perfil.pdf", "application/pdf", bytes("v1"));
        service.storeNewVersion(first.getId(), "perfil.pdf", "application/pdf", bytes("v2"));
        calls.clear();

        assertThatThrownBy(() -> service.storeNewVersion(first.getId(), "perfil.pdf", "application/pdf",
                bytes("v3"))).isInstanceOf(StoredFileNotCurrentException.class);

        assertThat(calls).containsExactly("lock " + first.getId());
    }

    @Test
    void missingIdsAreRejected() {
        UUID missing = UUID.randomUUID();

        assertThatThrownBy(() -> service.storeNewVersion(missing, "a.pdf", "application/pdf", bytes("x")))
                .isInstanceOf(StoredFileNotFoundException.class);
        assertThatThrownBy(() -> service.readContent(missing)).isInstanceOf(StoredFileNotFoundException.class);
        assertThatThrownBy(() -> service.getHistory(missing)).isInstanceOf(StoredFileNotFoundException.class);
        assertThat(storage.content).isEmpty();
    }

    @Test
    void readContentReturnsTheStoredBytes() {
        StoredFile file = service.store("cv.pdf", "application/pdf", bytes("contenido"));

        assertThat(service.readContent(file.getId())).isEqualTo(bytes("contenido"));
    }

    @Test
    void historyFromAnIntermediateVersionListsTheWholeChain() {
        StoredFile first = service.store("perfil.pdf", "application/pdf", bytes("v1"));
        StoredFile second = service.storeNewVersion(first.getId(), "perfil.pdf", "application/pdf", bytes("v2"));
        StoredFile third = service.storeNewVersion(second.getId(), "perfil.pdf", "application/pdf", bytes("v3"));

        List<StoredFile> history = service.getHistory(second.getId());

        assertThat(history).extracting(StoredFile::getId).containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(history).filteredOn(StoredFile::isCurrent).extracting(StoredFile::getId)
                .containsExactly(third.getId());
    }

    /** Copies a file, as a database would, so the service cannot rely on shared instances. */
    private static StoredFile copy(StoredFile file) {
        return StoredFile.rehydrate().withId(file.getId()).withPath(file.getPath())
                .withOriginalName(file.getOriginalName()).withContentType(file.getContentType())
                .withSize(file.getSize()).withStoredAt(file.getStoredAt())
                .withFirstVersionId(file.getFirstVersionId()).withNextVersionId(file.getNextVersionId());
    }

    private final class InMemoryFiles implements StoredFileRepository {

        private final Map<UUID, StoredFile> saved = new LinkedHashMap<>();

        @Override
        public void insert(StoredFile file) {
            calls.add("insert " + file.getId());
            saved.put(file.getId(), copy(file));
        }

        @Override
        public void update(StoredFile file) {
            calls.add("update " + file.getId());
            saved.put(file.getId(), copy(file));
        }

        @Override
        public Optional<StoredFile> getById(UUID id) {
            return Optional.ofNullable(saved.get(id)).map(StoredFileServiceTest::copy);
        }

        @Override
        public Optional<StoredFile> getByIdForUpdate(UUID id) {
            calls.add("lock " + id);
            return getById(id);
        }

        @Override
        public List<StoredFile> getByFirstVersionId(UUID firstVersionId) {
            // Newest first on purpose: the service must order the chain by its links, not by this order.
            return saved.values().stream().filter(f -> f.getFirstVersionId().equals(firstVersionId))
                    .sorted(Comparator.comparing(StoredFile::getStoredAt).reversed())
                    .map(StoredFileServiceTest::copy).toList();
        }
    }

    private final class InMemoryStorage implements FileStorage {

        private final Map<String, byte[]> content = new HashMap<>();

        @Override
        public void write(String path, byte[] bytes) {
            calls.add("write " + path);
            content.put(path, bytes.clone());
        }

        @Override
        public byte[] read(String path) {
            return content.get(path).clone();
        }
    }
}
