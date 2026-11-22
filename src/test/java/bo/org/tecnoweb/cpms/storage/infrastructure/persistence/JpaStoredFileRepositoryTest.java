package bo.org.tecnoweb.cpms.storage.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class JpaStoredFileRepositoryTest {

    @Autowired
    private StoredFileRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transaction;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM stored_file");
    }

    private static StoredFile pdf(String name) {
        return StoredFile.builder().withOriginalName(name).withContentType("application/pdf").withSize(1200).store();
    }

    /** Saves a new version as the service does: in one transaction, the new one first. */
    private void version(StoredFile previous, StoredFile next) {
        transaction.executeWithoutResult(status -> {
            StoredFile locked = repository.getByIdForUpdate(previous.getId()).orElseThrow();
            locked.supersedeBy(next);
            repository.insert(next);
            repository.update(locked);
        });
    }

    @Test
    void insertsAndReadsEveryField() {
        StoredFile file = pdf("perfil.pdf");

        repository.insert(file);

        StoredFile read = repository.getById(file.getId()).orElseThrow();
        assertThat(read.getPath()).isEqualTo(file.getPath());
        assertThat(read.getOriginalName()).isEqualTo("perfil.pdf");
        assertThat(read.getContentType()).isEqualTo("application/pdf");
        assertThat(read.getSize()).isEqualTo(1200);
        assertThat(read.getStoredAt()).isEqualTo(file.getStoredAt());
        assertThat(read.getFirstVersionId()).isEqualTo(file.getId());
        assertThat(read.isCurrent()).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM information_schema.columns "
                + "WHERE table_name = 'stored_file' AND data_type = 'bytea'", Integer.class)).isZero();
    }

    @Test
    void missingFileIsEmpty() {
        assertThat(repository.getById(UUID.randomUUID())).isEmpty();
        assertThat(repository.getByIdForUpdate(UUID.randomUUID())).isEmpty();
    }

    @Test
    void chainOfThreeVersionsIsReadInOrder() {
        StoredFile first = pdf("v1.pdf");
        repository.insert(first);
        StoredFile second = pdf("v2.pdf");
        version(first, second);
        StoredFile third = pdf("v3.pdf");
        version(second, third);

        assertThat(repository.getByFirstVersionId(first.getId())).extracting(StoredFile::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(repository.getById(first.getId()).orElseThrow().getNextVersionId()).isEqualTo(second.getId());
        assertThat(repository.getById(third.getId()).orElseThrow().isCurrent()).isTrue();
    }

    @Test
    void aVersionCannotHaveTwoNextVersions() {
        StoredFile first = pdf("v1.pdf");
        repository.insert(first);
        StoredFile second = pdf("v2.pdf");
        version(first, second);
        StoredFile other = pdf("otro.pdf");
        repository.insert(other);

        assertThatThrownBy(() -> jdbc.update("UPDATE stored_file SET next_version_id = ? WHERE id = ?",
                second.getId(), other.getId())).isInstanceOf(DataIntegrityViolationException.class);
    }
}
