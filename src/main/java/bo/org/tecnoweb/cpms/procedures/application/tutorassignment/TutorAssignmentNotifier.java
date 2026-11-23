package bo.org.tecnoweb.cpms.procedures.application.tutorassignment;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;

/** Notification port for UC-003. */
public interface TutorAssignmentNotifier {
    /** Notifies the student of the tutor designation (FR-020). */
    void notifyTutorAssigned(DegreeProjectTutorAssignedEvent event);
}
