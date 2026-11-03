package grupo03sc.mailapp.graduationprocesses.application;

import java.util.UUID;

/** The name and email of a professor that GraduationProcesses notifies (UC-004). */
public record ProfessorContact(UUID id, String fullName, String email) {
}
