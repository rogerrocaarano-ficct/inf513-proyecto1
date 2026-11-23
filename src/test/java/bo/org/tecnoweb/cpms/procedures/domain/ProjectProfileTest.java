package bo.org.tecnoweb.cpms.procedures.domain;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.pdf;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Checks the invariants of a profile version (UC-004, FR-005, FR-022). */
class ProjectProfileTest {
    /** A new version is pending, without observations or evaluation date. */
    @Test
    void pendingVersionHasNoEvaluation() {
        var file = pdf("perfil.pdf");
        var profile = ProjectProfile.pending(1, file, "Redes");

        assertThat(profile.storedFileId()).isEqualTo(file.getId());
        assertThat(profile.title()).isEqualTo("Redes");
        assertThat(profile.status()).isEqualTo(ProjectProfileStatus.PENDING);
        assertThat(profile.observations()).isNull();
        assertThat(profile.evaluatedOn()).isNull();
    }

    /** Versions are numbered from 1 (FR-023). */
    @Test
    void versionStartsAtOne() {
        assertThatThrownBy(() -> ProjectProfile.pending(0, pdf("perfil.pdf"), "Redes"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Only an evaluated version has an evaluation date. */
    @Test
    void evaluationDateMatchesTheStatus() {
        var id = UUID.randomUUID();

        assertThatThrownBy(() -> new ProjectProfile(1, id, "Redes", ProjectProfileStatus.PENDING, null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProjectProfile(1, id, "Redes", ProjectProfileStatus.APPROVED, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new ProjectProfile(1, id, "Redes", ProjectProfileStatus.APPROVED, null, Instant.now()).status())
                .isEqualTo(ProjectProfileStatus.APPROVED);
    }

    /** An observed or rejected version needs its observations. */
    @Test
    void observedAndRejectedNeedObservations() {
        var id = UUID.randomUUID();

        assertThatThrownBy(() -> new ProjectProfile(1, id, "Redes", ProjectProfileStatus.OBSERVED, " ", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProjectProfile(1, id, "Redes", ProjectProfileStatus.REJECTED, null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** Only an observed or rejected profile admits a new version (FR-058). */
    @Test
    void newVersionOnlyAfterObservationOrRejection() {
        assertThat(ProjectProfileStatus.PENDING.allowsNewVersion()).isFalse();
        assertThat(ProjectProfileStatus.APPROVED.allowsNewVersion()).isFalse();
        assertThat(ProjectProfileStatus.OBSERVED.allowsNewVersion()).isTrue();
        assertThat(ProjectProfileStatus.REJECTED.allowsNewVersion()).isTrue();
    }
}
