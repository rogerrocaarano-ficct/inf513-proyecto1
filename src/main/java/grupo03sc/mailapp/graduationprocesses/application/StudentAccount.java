package grupo03sc.mailapp.graduationprocesses.application;

import java.util.UUID;

/** The user account of a student, as GraduationProcesses sees it; {@code created} if it was just created. */
public record StudentAccount(UUID id, String fullName, String email, boolean created) {
}
