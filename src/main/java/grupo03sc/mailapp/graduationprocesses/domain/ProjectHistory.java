package grupo03sc.mailapp.graduationprocesses.domain;

import grupo03sc.mailapp.audit.application.LoggedChange;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the profiles and versions of a graduation project from its change log (D2 of
 * implementar-uc-004). The entries of one change share their date and time: a change with the storage
 * key is a delivery; a later change with only the status (UC-006) updates the status of the last
 * delivery.
 */
public final class ProjectHistory {

    private ProjectHistory() {
    }

    /** The deliveries, oldest first; the last one is the current document. */
    public static List<ProjectHistoryItem> from(List<LoggedChange> changes) {
        List<ProjectHistoryItem> items = new ArrayList<>();
        int start = 0;
        while (start < changes.size()) {
            Instant changedAt = changes.get(start).changedAt();
            Map<String, String> values = new HashMap<>();
            int end = start;
            while (end < changes.size() && changes.get(end).changedAt().equals(changedAt)) {
                values.put(changes.get(end).field(), changes.get(end).newValue());
                end++;
            }
            apply(items, values, changedAt);
            start = end;
        }
        return items;
    }

    private static void apply(List<ProjectHistoryItem> items, Map<String, String> values, Instant changedAt) {
        ProjectHistoryItem last = items.isEmpty() ? null : items.getLast();
        if (values.containsKey(GraduationProject.STORAGE_KEY)) {
            items.add(new ProjectHistoryItem(
                    enumValue(ProjectDocumentKind.class, value(values, GraduationProject.DOCUMENT_KIND,
                            last == null || last.documentKind() == null ? null : last.documentKind().name())),
                    integer(value(values, GraduationProject.VERSION_NUMBER, null)),
                    value(values, GraduationProject.TITLE, last == null ? null : last.title()),
                    value(values, GraduationProject.GENERAL_COMMENT, null),
                    value(values, GraduationProject.FILE_NAME, null),
                    values.get(GraduationProject.STORAGE_KEY),
                    changedAt,
                    enumValue(ProjectStatus.class, value(values, GraduationProject.STATUS, null))));
        } else if (values.containsKey(GraduationProject.STATUS) && last != null) {
            items.set(items.size() - 1,
                    last.withStatus(enumValue(ProjectStatus.class, values.get(GraduationProject.STATUS))));
        }
    }

    /** The value of the change, or the fallback when the change does not include that datum. */
    private static String value(Map<String, String> values, String field, String fallback) {
        return values.containsKey(field) ? values.get(field) : fallback;
    }

    private static Integer integer(String value) {
        return value == null ? null : Integer.valueOf(value);
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }
}
