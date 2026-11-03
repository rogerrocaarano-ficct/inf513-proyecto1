package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.graduationprocesses.domain.GraduationProject;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectHistoryItem;
import java.util.List;

/** A graduation project and its deliveries, oldest first; the last one is the current document (UC-004 A4). */
public record ProjectView(GraduationProject project, List<ProjectHistoryItem> history) {
}
