package grupo03sc.mailapp.graduationprocesses.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

import grupo03sc.mailapp.audit.application.FieldChange;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** The rules of the graduation project (spec graduation-projects). */
class GraduationProjectTests {

    private static final Instant NOW = Instant.parse("2026-11-03T12:00:00Z");
    private static final Instant LATER = NOW.plusSeconds(60);
    private final UUID process = UUID.randomUUID();

    private static DocumentFile pdf(String name) {
        return new DocumentFile(name, "application/pdf", new byte[10]);
    }

    private GraduationProject profile() {
        return GraduationProject.createWithProfile(process, "Sistema de inventario", pdf("perfil.pdf"), "k1", NOW);
    }

    /** UC-006 changes the status; until then the tests set it directly. */
    private static void setStatus(GraduationProject project, ProjectStatus status) throws Exception {
        Field field = GraduationProject.class.getDeclaredField("status");
        field.setAccessible(true);
        field.set(project, status);
    }

    private GraduationProject version(int number, ProjectStatus status) throws Exception {
        GraduationProject project = profile();
        setStatus(project, ProjectStatus.PROFILE_APPROVED);
        for (int i = 1; i <= number; i++) {
            if (i > 1) {
                setStatus(project, ProjectStatus.TUTOR_OBSERVED);
            }
            project.submitVersion("Versión " + i, pdf("v" + i + ".pdf"), "kv" + i, false, NOW);
        }
        setStatus(project, status);
        return project;
    }

    private static void assertRejected(Runnable action, GraduationRejectionReason reason, String text) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(GraduationRuleViolationException.class, e -> {
            assertThat(e.getReason()).isEqualTo(reason);
            assertThat(e.getMessage()).contains(text);
        });
    }

    @Test
    void acceptsAPdfOfExactly25Megabytes() {
        assertThatNoException().isThrownBy(() -> GraduationProject.checkFile(
                new DocumentFile("perfil.pdf", "application/pdf", new byte[26_214_400])));
        assertThatNoException().isThrownBy(() -> GraduationProject.checkFile(
                new DocumentFile("perfil.PDF", "application/octet-stream", new byte[1])));
    }

    @Test
    void rejectsAFileOverTheMaximumEmptyOrNotPdf() {
        assertRejected(() -> GraduationProject.checkFile(
                new DocumentFile("perfil.pdf", "application/pdf", new byte[26_214_401])),
                GraduationRejectionReason.INVALID_FILE, "25 MB");
        assertRejected(() -> GraduationProject.checkFile(
                new DocumentFile("perfil.pdf", "application/pdf", new byte[0])),
                GraduationRejectionReason.INVALID_FILE, "vacío");
        assertRejected(() -> GraduationProject.checkFile(
                new DocumentFile("perfil.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        new byte[1])),
                GraduationRejectionReason.INVALID_FILE, "debe ser un PDF");
        assertRejected(() -> GraduationProject.checkFile(new DocumentFile("perfil.pdf", "image/png", new byte[1])),
                GraduationRejectionReason.INVALID_FILE, "debe ser un PDF");
    }

    @Test
    void theFirstProfileIsPendingAndRecordsTheSevenData() {
        GraduationProject project = profile();

        assertThat(project.getDocumentKind()).isEqualTo(ProjectDocumentKind.PROJECT_PROFILE);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PROFILE_PENDING);
        assertThat(project.getVersionNumber()).isNull();
        assertThat(project.describe()).containsExactly(
                FieldChange.created("graduationProcessId", process),
                FieldChange.created("documentKind", "PROJECT_PROFILE"),
                FieldChange.created("title", "Sistema de inventario"),
                FieldChange.created("fileName", "perfil.pdf"),
                FieldChange.created("storageKey", "k1"),
                FieldChange.created("versionNumber", null),
                FieldChange.created("generalComment", null),
                FieldChange.created("status", "PROFILE_PENDING"));
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"PROFILE_OBSERVED", "PROFILE_REJECTED"})
    void acceptsANewProfileAfterAnObservationOrARejection(ProjectStatus status) throws Exception {
        GraduationProject project = profile();
        setStatus(project, status);

        List<FieldChange> changes = project.submitProfile("Sistema de ventas", pdf("perfil2.pdf"), "k2", LATER);

        assertThat(project.getTitle()).isEqualTo("Sistema de ventas");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PROFILE_PENDING);
        assertThat(project.getStorageKey()).isEqualTo("k2");
        assertThat(changes).hasSize(7).contains(
                new FieldChange("title", "Sistema de inventario", "Sistema de ventas"),
                new FieldChange("storageKey", "k1", "k2"),
                new FieldChange("status", status.name(), "PROFILE_PENDING"));
    }

    @Test
    void rejectsANewProfileWhileTheCurrentOneIsPendingOrApproved() throws Exception {
        GraduationProject pending = profile();
        assertRejected(() -> pending.submitProfile("Otro", pdf("p.pdf"), "k2", LATER),
                GraduationRejectionReason.PROFILE_NOT_ALLOWED, "pendiente de evaluación");

        GraduationProject approved = profile();
        setStatus(approved, ProjectStatus.PROFILE_APPROVED);
        assertRejected(() -> approved.submitProfile("Otro", pdf("p.pdf"), "k2", LATER),
                GraduationRejectionReason.PROFILE_NOT_ALLOWED, "con visto bueno");

        GraduationProject withVersion = version(1, ProjectStatus.TUTOR_OBSERVED);
        assertRejected(() -> withVersion.submitProfile("Otro", pdf("p.pdf"), "k2", LATER),
                GraduationRejectionReason.PROFILE_NOT_ALLOWED, "ya entregó el documento del proyecto");
    }

    @Test
    void rejectsAVersionWithoutTheApprovalOfTheProfile() {
        GraduationProject project = profile();

        assertRejected(() -> project.checkVersionAllowed(false),
                GraduationRejectionReason.VERSION_PREREQUISITES, "todavía no tiene visto bueno");
    }

    @Test
    void theFirstVersionIsNumberOneInReviewByTheTutor() throws Exception {
        GraduationProject project = profile();
        setStatus(project, ProjectStatus.PROFILE_APPROVED);

        List<FieldChange> changes = project.submitVersion("Primera versión", pdf("documento.pdf"), "kv1", false, LATER);

        assertThat(project.getDocumentKind()).isEqualTo(ProjectDocumentKind.PROJECT_DOCUMENT);
        assertThat(project.getVersionNumber()).isEqualTo(1);
        assertThat(project.getGeneralComment()).isEqualTo("Primera versión");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.TUTOR_REVIEW);
        assertThat(project.getTitle()).isEqualTo("Sistema de inventario");
        assertThat(changes).hasSize(7).contains(
                new FieldChange("documentKind", "PROJECT_PROFILE", "PROJECT_DOCUMENT"),
                new FieldChange("versionNumber", null, "1"),
                new FieldChange("title", "Sistema de inventario", "Sistema de inventario"),
                new FieldChange("status", "PROFILE_APPROVED", "TUTOR_REVIEW"));
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"TUTOR_OBSERVED", "TRIBUNAL_OBSERVED"})
    void acceptsACorrectedVersionAfterObservations(ProjectStatus status) throws Exception {
        GraduationProject project = version(2, status);

        project.submitVersion("Correcciones", pdf("v3.pdf"), "kv3", false, LATER);

        assertThat(project.getVersionNumber()).isEqualTo(3);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.TUTOR_REVIEW);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"TUTOR_REVIEW", "TRIBUNAL_REVIEW"})
    void rejectsAVersionWhileTheCurrentOneIsInReview(ProjectStatus status) throws Exception {
        GraduationProject project = version(1, status);

        assertRejected(() -> project.submitVersion("Otra", pdf("v2.pdf"), "kv2", false, LATER),
                GraduationRejectionReason.VERSION_NOT_ALLOWED, "todavía está " + status.label());
    }

    @Test
    void rejectsAVersionAfterAnApprovedOneWithoutThePredefense() throws Exception {
        GraduationProject project = version(4, ProjectStatus.APPROVED);

        assertRejected(() -> project.checkVersionAllowed(false),
                GraduationRejectionReason.VERSION_NOT_ALLOWED, "la predefensa todavía no está aprobada");
    }

    @Test
    void theVersionAfterThePredefenseIsTheFinalDefenseVersion() throws Exception {
        GraduationProject project = version(4, ProjectStatus.APPROVED);

        project.submitVersion("Versión final", pdf("v5.pdf"), "kv5", true, LATER);

        assertThat(project.getVersionNumber()).isEqualTo(5);
        assertThat(project.getDocumentKind()).isEqualTo(ProjectDocumentKind.FINAL_DEFENSE_VERSION);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.TUTOR_REVIEW);
    }

    @Test
    void rejectsAVersionAfterTheApprovedFinalDefenseVersion() throws Exception {
        GraduationProject project = version(4, ProjectStatus.APPROVED);
        project.submitVersion("Versión final", pdf("v5.pdf"), "kv5", true, LATER);
        setStatus(project, ProjectStatus.APPROVED);

        assertRejected(() -> project.checkVersionAllowed(true),
                GraduationRejectionReason.VERSION_NOT_ALLOWED, "la versión para la defensa final ya está aprobada");
    }
}
