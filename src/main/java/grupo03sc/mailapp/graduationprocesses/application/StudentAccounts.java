package grupo03sc.mailapp.graduationprocesses.application;

import java.util.Optional;
import java.util.UUID;

/**
 * Port that GraduationProcesses defines and Users implements (dependency inversion): the user
 * account of a student. GraduationProcesses does not depend on Users, which already uses
 * {@link GraduationParticipation}.
 */
public interface StudentAccounts {

    /**
     * The active user account with the student role and this email and university registration
     * number, or a new one created with the rules and the notice of UC-001 (FR-051, FR-056).
     *
     * @throws grupo03sc.mailapp.messaging.application.CommandRejectedException for missing or invalid
     *         data, data of another account, or a deactivated account
     */
    StudentAccount obtain(UUID requesterId, StudentData student);

    /** The user account with the student role and this university registration number. */
    Optional<StudentAccount> findByRegistrationNumber(String studentRegistrationNumber);

    /** The user account with the student role and this id, to name the student in notices (UC-004). */
    Optional<StudentAccount> findById(UUID userAccountId);
}
