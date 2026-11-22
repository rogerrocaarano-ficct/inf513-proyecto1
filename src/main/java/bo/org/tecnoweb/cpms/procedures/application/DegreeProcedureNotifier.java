package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureDocumentReplacedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureOpenedEvent;

/** Outgoing notifications for UC-002. */
public interface DegreeProcedureNotifier {
    /** Confirms the opening to the requester and notifies the student (FR-054). */
    void notifyOpened(DegreeProcedureOpenedEvent event, String reference);

    /** Confirms the replacement to the requester (FR-055). */
    void notifyDocumentReplaced(DegreeProcedureDocumentReplacedEvent event);
}
