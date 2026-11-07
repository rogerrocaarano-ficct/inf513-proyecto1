package grupo03sc.mailapp.graduationprocesses.application;

import java.util.UUID;

/**
 * A graduation process affected by a change of a user account, with the user account id of its
 * student; Users resolves the name (GraduationProcesses does not depend on Users).
 */
public record GraduationProcessSummary(UUID id, UUID studentAccountId) {
}
