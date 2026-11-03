package grupo03sc.mailapp.users.application;

import java.util.UUID;

/** A graduation process left pending reassignment, with the name of its student (FR-034). */
public record AffectedProcess(UUID id, String studentName) {
}
