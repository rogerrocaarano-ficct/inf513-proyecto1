package bo.org.tecnoweb.cpms.storage.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StoredFileTest {

    private static StoredFile pdf() {
        return StoredFile.builder().withOriginalName(" perfil.pdf ").withContentType("Application/PDF").withSize(1200);
    }

    @Test
    void storeAssignsTimeKeyAndOwnChain() {
        Instant before = Instant.now();

        StoredFile file = pdf().store();

        assertThat(file.getStoredAt()).isBetween(before.truncatedTo(ChronoUnit.MICROS), Instant.now());
        assertThat(file.getStoredAt().getNano() % 1000).isZero();
        ZonedDateTime utc = file.getStoredAt().atZone(ZoneOffset.UTC);
        assertThat(file.getPath()).isEqualTo("%04d/%02d/%s".formatted(utc.getYear(), utc.getMonthValue(), file.getId()));
        assertThat(file.getFirstVersionId()).isEqualTo(file.getId());
        assertThat(file.isCurrent()).isTrue();
        assertThat(file.getOriginalName()).isEqualTo("perfil.pdf");
        assertThat(file.getContentType()).isEqualTo("application/pdf");
        assertThat(file.pullEvents()).isEmpty();
    }

    @Test
    void keyNeverUsesTheOriginalName() {
        StoredFile file = StoredFile.builder().withOriginalName("../../etc/passwd").withContentType("text/plain")
                .withSize(1).store();

        assertThat(file.getPath()).doesNotContain("..").doesNotContain("passwd").endsWith(file.getId().toString());
    }

    @Test
    void storeRejectsEveryInvalidField() {
        StoredFile file = StoredFile.builder().withOriginalName("  ").withSize(0);

        assertThatThrownBy(file::store).isInstanceOfSatisfying(DomainValidationException.class,
                e -> assertThat(e.getErrors()).containsOnlyKeys(StoredFile.ORIGINAL_NAME, StoredFile.CONTENT_TYPE,
                        StoredFile.CONTENT));
        assertThat(file.getPath()).isNull();
    }

    @Test
    void storeTwiceIsAProgrammingError() {
        StoredFile file = pdf().store();

        assertThatThrownBy(file::store).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void supersedeByLinksTheNextVersionToTheSameChain() {
        StoredFile first = pdf().store();
        StoredFile second = pdf().store();
        first.supersedeBy(second);
        StoredFile third = pdf().store();

        second.supersedeBy(third);

        assertThat(first.getNextVersionId()).isEqualTo(second.getId());
        assertThat(second.getNextVersionId()).isEqualTo(third.getId());
        assertThat(first.isCurrent()).isFalse();
        assertThat(third.isCurrent()).isTrue();
        assertThat(second.getFirstVersionId()).isEqualTo(first.getId());
        assertThat(third.getFirstVersionId()).isEqualTo(first.getId());
    }

    @Test
    void onlyTheCurrentVersionCanBeSuperseded() {
        StoredFile first = pdf().store();
        first.supersedeBy(pdf().store());
        StoredFile other = pdf().store();

        assertThatThrownBy(() -> first.supersedeBy(other)).isInstanceOf(StoredFileNotCurrentException.class);
        assertThat(other.getFirstVersionId()).isEqualTo(other.getId());
    }

    @Test
    void rehydrateRestoresEveryField() {
        UUID id = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID next = UUID.randomUUID();
        Instant at = Instant.parse("2026-11-22T10:00:00Z");

        StoredFile file = StoredFile.rehydrate().withId(id).withPath("2026/11/" + id).withOriginalName("cv.pdf")
                .withContentType("application/pdf").withSize(42).withStoredAt(at).withFirstVersionId(first)
                .withNextVersionId(next);

        assertThat(file.getId()).isEqualTo(id);
        assertThat(file.getPath()).isEqualTo("2026/11/" + id);
        assertThat(file.getSize()).isEqualTo(42);
        assertThat(file.getStoredAt()).isEqualTo(at);
        assertThat(file.getFirstVersionId()).isEqualTo(first);
        assertThat(file.getNextVersionId()).isEqualTo(next);
        assertThat(file.isCurrent()).isFalse();
    }
}
