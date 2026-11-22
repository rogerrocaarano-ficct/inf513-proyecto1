package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;

/** Notification port for UC-003. */
public interface TutorAssignmentNotifier {
    /** Notifies Taller of the newly registered profile (FR-057). */
    void notifyProfileRegistered(DegreeProjectProfileRegisteredEvent event);

    /** Notifies the student of the tutor designation (FR-020). */
    void notifyTutorAssigned(DegreeProjectTutorAssignedEvent event);
}
