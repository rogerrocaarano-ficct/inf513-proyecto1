package grupo03sc.mailapp.graduationprocesses.application;

import java.util.Optional;
import java.util.UUID;

/**
 * Port that GraduationProcesses defines and Users implements (dependency inversion): the professors it
 * notifies of a project profile or a version (UC-004). GraduationProcesses does not depend on Users.
 */
public interface ProfessorContacts {

    /** The active user account with the thesis workshop professor role, if there is one (FR-039). */
    Optional<ProfessorContact> thesisWorkshopProfessor();

    /** The active user account with this id, or empty if it does not exist or is deactivated. */
    Optional<ProfessorContact> findById(UUID userAccountId);
}
