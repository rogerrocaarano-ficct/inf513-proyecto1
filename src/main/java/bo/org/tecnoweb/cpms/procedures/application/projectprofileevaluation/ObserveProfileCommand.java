package bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation;

/**
 * Observation of the pending profile (UC-004 A2, FR-022).
 *
 * @param reference    the reference of the procedure
 * @param observations the observations, taken from the body of the email
 */
public record ObserveProfileCommand(String reference, String observations) {}
