package bo.org.tecnoweb.cpms.procedures.application;

/**
 * Rejection of the pending profile (UC-004 A3, FR-022).
 *
 * @param reference    the reference of the procedure
 * @param observations why the topic must change, taken from the body of the email
 */
public record RejectProfileCommand(String reference, String observations) {}
