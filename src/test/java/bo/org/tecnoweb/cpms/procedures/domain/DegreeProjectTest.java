package bo.org.tecnoweb.cpms.procedures.domain;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.pdf;
import static org.assertj.core.api.Assertions.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Checks the profile rules of UC-004 and the tutor rules of UC-003. */
class DegreeProjectTest {
    private static final String REFERENCE = "2026-2-123456";

    private static DegreeProject pendingProject() {
        var project = DegreeProject.builder().withProcedureId(UUID.randomUUID())
                .registerProfile(pdf("perfil.pdf"), " Redes ", "student@test", "workshop@test", REFERENCE);
        project.pullEvents();
        return project;
    }

    /** The first profile is pending, keeps the stored file and makes its title the project title (FR-004). */
    @Test
    void registersTheFirstProfile() {
        var file = pdf("perfil.pdf");
        var project = DegreeProject.builder().withProcedureId(UUID.randomUUID())
                .registerProfile(file, " Redes ", "student@test", "workshop@test", REFERENCE);

        assertThat(project.getTitle()).isEqualTo("Redes");
        assertThat(project.getProfile().storedFileId()).isEqualTo(file.getId());
        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.PENDING);
        assertThat(project.pullEvents()).singleElement().isInstanceOfSatisfying(
                DegreeProjectProfileRegisteredEvent.class, event -> {
                    assertThat(event.profile().version()).isEqualTo(1);
                    assertThat(event.workshop()).isEqualTo("workshop@test");
                });
    }

    /** A blank title is rejected without registering the profile (FR-004). */
    @Test
    void rejectsBlankTitleWithoutRegistering() {
        var project = DegreeProject.builder().withProcedureId(UUID.randomUUID());

        assertThatThrownBy(() -> project.registerProfile(pdf("perfil.pdf"), "  ", "student@test", "workshop@test",
                REFERENCE)).isInstanceOf(DomainValidationException.class);
        assertThat(project.getProfile()).isNull();
        assertThat(project.pullEvents()).isEmpty();
    }

    /** A pending or approved profile admits no new version (FR-058). */
    @Test
    void rejectsNewVersionWhilePendingOrApproved() {
        var project = pendingProject();

        assertThatThrownBy(() -> project.registerProfile(pdf("otro.pdf"), "Otro", "student@test", "workshop@test",
                REFERENCE)).isInstanceOf(DomainValidationException.class);
        project.approveProfile("student@test", "director@test", REFERENCE);
        assertThatThrownBy(() -> project.registerProfile(pdf("otro.pdf"), "Otro", "student@test", "workshop@test",
                REFERENCE)).isInstanceOf(DomainValidationException.class);
        assertThat(project.getProfiles()).hasSize(1);
    }

    /** Approval records the date and notifies student and director (FR-005, FR-060, FR-061). */
    @Test
    void approvesThePendingProfile() {
        var project = pendingProject();

        project.approveProfile("student@test", "director@test", REFERENCE);

        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.APPROVED);
        assertThat(project.getProfile().evaluatedOn()).isNotNull();
        assertThat(project.pullEvents()).singleElement().isInstanceOfSatisfying(
                DegreeProjectProfileApprovedEvent.class, event -> {
                    assertThat(event.director()).isEqualTo("director@test");
                    assertThat(event.title()).isEqualTo("Redes");
                });
    }

    /** Observation keeps the stripped observations and admits a new version with another title (FR-022, FR-023). */
    @Test
    void observedProfileAdmitsANewVersion() {
        var project = pendingProject();

        project.observeProfile("  Corregir la bibliografía. ", "student@test", REFERENCE);
        assertThat(project.pullEvents()).singleElement().isInstanceOfSatisfying(
                DegreeProjectProfileObservedEvent.class,
                event -> assertThat(event.observations()).isEqualTo("Corregir la bibliografía."));

        var second = pdf("perfil-v2.pdf");
        project.registerProfile(second, "Redes rurales", "student@test", "workshop@test", REFERENCE);

        assertThat(project.getTitle()).isEqualTo("Redes rurales");
        assertThat(project.getProfiles()).extracting(ProjectProfile::status)
                .containsExactly(ProjectProfileStatus.OBSERVED, ProjectProfileStatus.PENDING);
        assertThat(project.getProfiles().getFirst().observations()).isEqualTo("Corregir la bibliografía.");
        assertThat(project.getProfiles().getFirst().title()).isEqualTo("Redes");
        assertThat(project.getProfile().storedFileId()).isEqualTo(second.getId());
        assertThat(project.pullEvents()).singleElement().isInstanceOfSatisfying(
                DegreeProjectProfileRegisteredEvent.class, event -> assertThat(event.profile().version()).isEqualTo(2));
    }

    /** Rejection keeps the reason and admits a new version (FR-022, FR-023). */
    @Test
    void rejectedProfileAdmitsANewVersion() {
        var project = pendingProject();

        project.rejectProfile("El tema no es viable.", "student@test", REFERENCE);

        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.REJECTED);
        assertThat(project.pullEvents()).singleElement().isInstanceOf(DegreeProjectProfileRejectedEvent.class);
        project.registerProfile(pdf("nuevo.pdf"), "Otro tema", "student@test", "workshop@test", REFERENCE);
        assertThat(project.getProfiles()).hasSize(2);
    }

    /** Observing or rejecting without observations changes nothing (FR-022). */
    @Test
    void requiresObservations() {
        var project = pendingProject();

        assertThatThrownBy(() -> project.observeProfile(" ", "student@test", REFERENCE))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> project.rejectProfile(null, "student@test", REFERENCE))
                .isInstanceOf(DomainValidationException.class);
        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.PENDING);
        assertThat(project.pullEvents()).isEmpty();
    }

    /** Only a pending profile is evaluated. */
    @Test
    void evaluatesOnlyAPendingProfile() {
        var project = pendingProject();
        project.observeProfile("Forma", "student@test", REFERENCE);
        project.pullEvents();

        assertThatThrownBy(() -> project.approveProfile("student@test", null, REFERENCE))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> project.rejectProfile("Tema", "student@test", REFERENCE))
                .isInstanceOf(DomainValidationException.class);
        assertThat(project.getProfile().status()).isEqualTo(ProjectProfileStatus.OBSERVED);
        assertThat(project.pullEvents()).isEmpty();
    }

    /** The tutor needs the current profile approved, and only one is assigned (FR-003). */
    @Test
    void assignsOnlyAfterApproval() {
        var project = pendingProject();
        var tutorId = UUID.randomUUID();

        assertThatThrownBy(() -> project.assignTutor(tutorId, "Luis", "student@test", REFERENCE))
                .isInstanceOf(DomainValidationException.class);
        assertThat(project.getTutorId()).isNull();

        project.approveProfile("student@test", "director@test", REFERENCE);
        project.pullEvents();
        project.assignTutor(tutorId, "Luis", "student@test", REFERENCE);
        assertThat(project.getTutorId()).isEqualTo(tutorId);
        assertThat(project.pullEvents()).singleElement().isInstanceOf(DegreeProjectTutorAssignedEvent.class);
        assertThatThrownBy(() -> project.assignTutor(UUID.randomUUID(), "Rosa", "student@test", REFERENCE))
                .isInstanceOf(DomainValidationException.class);
        assertThat(project.getTutorId()).isEqualTo(tutorId);
    }

    /** A rehydrated project keeps its versions and publishes nothing. */
    @Test
    void rehydratesWithoutEvents() {
        var first = ProjectProfile.pending(1, pdf("perfil.pdf"), "Redes");
        var project = DegreeProject.rehydrate(UUID.randomUUID(), UUID.randomUUID(), "Redes",
                java.util.List.of(first), null);

        assertThat(project.getProfiles()).containsExactly(first);
        assertThat(project.pullEvents()).isEmpty();
    }

    /** Versions are numbered 1, 2, … in order; a gap or a wrong start is rejected (FR-023). */
    @Test
    void numbersVersionsInOrder() {
        var project = pendingProject();
        project.rejectProfile("Tema", "student@test", REFERENCE);
        project.registerProfile(pdf("nuevo.pdf"), "Otro", "student@test", "workshop@test", REFERENCE);

        assertThat(project.getProfiles()).extracting(ProjectProfile::version).containsExactly(1, 2);
        assertThatThrownBy(() -> DegreeProject.rehydrate(UUID.randomUUID(), UUID.randomUUID(), "Redes",
                java.util.List.of(ProjectProfile.pending(2, pdf("perfil.pdf"), "Redes")), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
