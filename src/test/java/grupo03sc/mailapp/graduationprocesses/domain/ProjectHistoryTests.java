package grupo03sc.mailapp.graduationprocesses.domain;

import static org.assertj.core.api.Assertions.assertThat;

import grupo03sc.mailapp.audit.application.FieldChange;
import grupo03sc.mailapp.audit.application.LoggedChange;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The history of a graduation project from its change log (D2 of implementar-uc-004). */
class ProjectHistoryTests {

    private static final Instant T1 = Instant.parse("2026-11-03T12:00:00Z");
    private static final Instant T2 = T1.plusSeconds(60);
    private static final Instant T3 = T1.plusSeconds(120);
    private static final Instant T4 = T1.plusSeconds(180);
    private static final UUID STUDENT = UUID.randomUUID();

    private final List<LoggedChange> log = new ArrayList<>();

    private void record(Instant at, List<FieldChange> changes) {
        changes.forEach(c -> log.add(new LoggedChange(at, STUDENT, c.field(), c.oldValue(), c.newValue())));
    }

    private static DocumentFile pdf(String name) {
        return new DocumentFile(name, "application/pdf", new byte[10]);
    }

    private static void setStatus(GraduationProject project, ProjectStatus status) throws Exception {
        Field field = GraduationProject.class.getDeclaredField("status");
        field.setAccessible(true);
        field.set(project, status);
    }

    @Test
    void buildsAReplacedProfileAVersionAndALaterStatusChange() throws Exception {
        GraduationProject project = GraduationProject.createWithProfile(UUID.randomUUID(), "Sistema de inventario",
                pdf("perfil.pdf"), "k1", T1);
        record(T1, project.describe());
        setStatus(project, ProjectStatus.PROFILE_OBSERVED);
        record(T2, List.of(new FieldChange("status", "PROFILE_PENDING", "PROFILE_OBSERVED")));
        record(T3, project.submitProfile("Sistema de ventas", pdf("perfil2.pdf"), "k2", T3));
        setStatus(project, ProjectStatus.PROFILE_APPROVED);
        record(T3.plusSeconds(1), List.of(new FieldChange("status", "PROFILE_PENDING", "PROFILE_APPROVED")));
        record(T4, project.submitVersion("Primera versión", pdf("documento.pdf"), "kv1", false, T4));

        List<ProjectHistoryItem> items = ProjectHistory.from(log);

        assertThat(items).extracting(ProjectHistoryItem::documentKind, ProjectHistoryItem::title,
                ProjectHistoryItem::storageKey, ProjectHistoryItem::status, ProjectHistoryItem::submittedAt)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(ProjectDocumentKind.PROJECT_PROFILE, "Sistema de inventario",
                                "k1", ProjectStatus.PROFILE_OBSERVED, T1),
                        org.assertj.core.groups.Tuple.tuple(ProjectDocumentKind.PROJECT_PROFILE, "Sistema de ventas",
                                "k2", ProjectStatus.PROFILE_APPROVED, T3),
                        org.assertj.core.groups.Tuple.tuple(ProjectDocumentKind.PROJECT_DOCUMENT, "Sistema de ventas",
                                "kv1", ProjectStatus.TUTOR_REVIEW, T4));
        assertThat(items.getLast().versionNumber()).isEqualTo(1);
        assertThat(items.getLast().generalComment()).isEqualTo("Primera versión");
        assertThat(items.getFirst().versionNumber()).isNull();
    }

    @Test
    void aVersionWithoutATitleInItsChangeKeepsThePreviousTitle() {
        record(T1, List.of(FieldChange.created("documentKind", "PROJECT_PROFILE"),
                FieldChange.created("title", "Sistema de inventario"), FieldChange.created("storageKey", "k1"),
                FieldChange.created("status", "PROFILE_APPROVED")));
        record(T2, List.of(FieldChange.created("documentKind", "PROJECT_DOCUMENT"),
                FieldChange.created("storageKey", "kv1"), FieldChange.created("versionNumber", "1"),
                FieldChange.created("status", "TUTOR_REVIEW")));

        assertThat(ProjectHistory.from(log).getLast().title()).isEqualTo("Sistema de inventario");
    }

    @Test
    void anEmptyLogHasNoHistory() {
        assertThat(ProjectHistory.from(log)).isEmpty();
    }
}
