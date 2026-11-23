package bo.org.tecnoweb.cpms.procedures.domain;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.pdf;
import static org.assertj.core.api.Assertions.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Checks the profile and tutor rules of UC-003. */
class DegreeProjectTest {
    /** An initial PDF creates one pending profile and a single event. */
    @Test
    void registersOnlyTheFirstProfile() {
        var project = DegreeProject.builder().withProcedureId(UUID.randomUUID()).withTitle(" Redes ");
        project.registerInitialProfile(pdf("perfil.pdf"), "student@test", "workshop@test", "2026-2-123456");

        assertThat(project.getTitle()).isEqualTo("Redes");
        assertThat(project.getProfile().approved()).isFalse();
        assertThat(project.pullEvents()).singleElement().isInstanceOf(DegreeProjectProfileRegisteredEvent.class);
        assertThatThrownBy(() -> project.registerInitialProfile(
                pdf("otro.pdf"), "student@test", "workshop@test", "2026-2-123456")).isInstanceOf(DomainValidationException.class);
        assertThat(project.pullEvents()).isEmpty();
    }

    /** The profile adopts the stored file (issue #9); the PDF rules are checked in InitialDocumentTest. */
    @Test
    void profileKeepsTheStoredFile() {
        var file = pdf("perfil.pdf");
        var project = DegreeProject.builder().withProcedureId(UUID.randomUUID()).withTitle("Redes")
                .registerInitialProfile(file, "student@test", "workshop@test", "2026-2-123456");

        assertThat(project.getProfile().storedFileId()).isEqualTo(file.getId());
    }

    /** A blank title is rejected without registering the profile (FR-004). */
    @Test
    void rejectsBlankTitleWithoutRegistering() {
        var project = DegreeProject.builder().withProcedureId(UUID.randomUUID()).withTitle("  ");

        assertThatThrownBy(() -> project.registerInitialProfile(
                pdf("perfil.pdf"), "student@test", "workshop@test", "2026-2-123456")).isInstanceOf(DomainValidationException.class);
        assertThat(project.getProfile()).isNull();
        assertThat(project.pullEvents()).isEmpty();
    }

    /** Approval is required and a subsequent assignment is rejected (FR-003). */
    @Test
    void assignsOnlyAfterApproval() {
        var project = DegreeProject.builder().withProcedureId(UUID.randomUUID()).withTitle("Redes")
                .registerInitialProfile(pdf("perfil.pdf"), "student@test", "workshop@test", "2026-2-123456");
        var tutorId = UUID.randomUUID();

        assertThatThrownBy(() -> project.assignTutor(tutorId, "Luis", "student@test", "2026-2-123456"))
                .isInstanceOf(DomainValidationException.class);
        assertThat(project.getTutorId()).isNull();

        var approved = DegreeProject.rehydrate(project.getId(), project.getProcedureId(), project.getTitle(),
                new ProjectProfile(project.getProfile().storedFileId(), true), null);
        assertThat(approved.pullEvents()).isEmpty();
        approved.assignTutor(tutorId, "Luis", "student@test", "2026-2-123456");
        assertThat(approved.getTutorId()).isEqualTo(tutorId);
        assertThat(approved.pullEvents()).singleElement().isInstanceOf(DegreeProjectTutorAssignedEvent.class);
        assertThatThrownBy(() -> approved.assignTutor(UUID.randomUUID(), "Rosa", "student@test", "2026-2-123456"))
                .isInstanceOf(DomainValidationException.class);
        assertThat(approved.getTutorId()).isEqualTo(tutorId);
    }
}
