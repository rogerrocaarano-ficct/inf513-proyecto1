package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileApprovedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileObservedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRejectedEvent;

/** Notification port of UC-004: one method per notice. */
public interface ProjectProfileEvaluationNotifier {
    /** Sends the submitted profile to the workshop teacher (FR-057). */
    void notifyProfileRegistered(DegreeProjectProfileRegisteredEvent event);

    /** Tells the student, and the director if there is one, that the profile was approved (FR-060, FR-061). */
    void notifyProfileApproved(DegreeProjectProfileApprovedEvent event);

    /** Sends the observations of the profile to the student (FR-060). */
    void notifyProfileObserved(DegreeProjectProfileObservedEvent event);

    /** Sends the rejection of the profile and its reason to the student (FR-060). */
    void notifyProfileRejected(DegreeProjectProfileRejectedEvent event);
}
