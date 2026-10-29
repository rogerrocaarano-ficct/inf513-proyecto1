package grupo03sc.mailapp.graduationprocesses.application;

import java.util.UUID;

/**
 * A graduation process affected by a change of a user account.
 */
public record GraduationProcessSummary(UUID id, String studentName) {
}
