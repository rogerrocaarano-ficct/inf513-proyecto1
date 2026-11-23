package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRequirementReplacedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureOpenedEvent;

/** Outgoing notifications for UC-002. */
public interface DegreeProcedureNotifier {
    /** Confirms the opening, with its reference, to the requester and notifies the student (FR-054). */
    void notifyOpened(DegreeProcedureOpenedEvent event);

    /** Confirms the replacement to the requester (FR-055). */
    void notifyRequirementReplaced(DegreeProcedureRequirementReplacedEvent event);
}
