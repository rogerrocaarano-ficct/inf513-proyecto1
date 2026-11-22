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
        project.registerInitialProfile(pdf("perfil.pdf"), "student@test", "workshop@test", "TR-001");

        assertThat(project.getTitle()).isEqualTo("Redes");
        assertThat(project.getProfile().approved()).isFalse();
        assertThat(project.pullEvents()).singleElement().isInstanceOf(DegreeProjectProfileRegisteredEvent.class);
        assertThatThrownBy(() -> project.registerInitialProfile(
                pdf("otro.pdf"), "student@test", "workshop@test", "TR-001")).isInstanceOf(DomainValidationException.class);
        assertThat(project.pullEvents()).isEmpty();
    }

    /** Invalid filenames are rejected even with PDF bytes (FR-004). */
    @Test
    void rejectsInvalidPdfWithoutRegistering() {
        var project = DegreeProject.builder().withProcedureId(UUID.randomUUID()).withTitle("Redes");

        assertThatThrownBy(() -> project.registerInitialProfile(
                pdf("perfil.txt"), "student@test", "workshop@test", "TR-001")).isInstanceOf(DomainValidationException.class);
        assertThat(project.getProfile()).isNull();
        assertThat(project.pullEvents()).isEmpty();
    }

    /** Approval is required and a subsequent assignment is rejected (FR-003). */
    @Test
    void assignsOnlyAfterApproval() {
        var project = DegreeProject.builder().withProcedureId(UUID.randomUUID()).withTitle("Redes")
                .registerInitialProfile(pdf("perfil.pdf"), "student@test", "workshop@test", "TR-001");
        var tutorId = UUID.randomUUID();

        assertThatThrownBy(() -> project.assignTutor(tutorId, "Luis", "student@test", "TR-001"))
                .isInstanceOf(DomainValidationException.class);
        assertThat(project.getTutorId()).isNull();

        var approved = DegreeProject.rehydrate(project.getId(), project.getProcedureId(), project.getTitle(),
                new ProjectProfile("perfil.pdf", project.getProfile().content(), true), null);
        assertThat(approved.pullEvents()).isEmpty();
        approved.assignTutor(tutorId, "Luis", "student@test", "TR-001");
        assertThat(approved.getTutorId()).isEqualTo(tutorId);
        assertThat(approved.pullEvents()).singleElement().isInstanceOf(DegreeProjectTutorAssignedEvent.class);
        assertThatThrownBy(() -> approved.assignTutor(UUID.randomUUID(), "Rosa", "student@test", "TR-001"))
                .isInstanceOf(DomainValidationException.class);
        assertThat(approved.getTutorId()).isEqualTo(tutorId);
    }
}
