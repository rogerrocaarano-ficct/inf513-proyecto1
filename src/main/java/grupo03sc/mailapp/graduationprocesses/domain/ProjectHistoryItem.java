package grupo03sc.mailapp.graduationprocesses.domain;

import java.time.Instant;

/** A profile or version of a graduation project, as the change log keeps it (UC-004 A4). */
public record ProjectHistoryItem(ProjectDocumentKind documentKind, Integer versionNumber, String title,
        String generalComment, String fileName, String storageKey, Instant submittedAt, ProjectStatus status) {

    ProjectHistoryItem withStatus(ProjectStatus newStatus) {
        return new ProjectHistoryItem(documentKind, versionNumber, title, generalComment, fileName, storageKey,
                submittedAt, newStatus);
    }
}
