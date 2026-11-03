package grupo03sc.mailapp.graduationprocesses.domain;

import grupo03sc.mailapp.messaging.application.CommandRejectedException;

/**
 * A graduation process request that breaks a rule of UC-003. Messaging rolls back the command and
 * replies with the reason and its detail.
 */
public class GraduationRuleViolationException extends CommandRejectedException {

    private final GraduationRejectionReason reason;

    public GraduationRuleViolationException(GraduationRejectionReason reason, String detail) {
        super(reason.message(detail));
        this.reason = reason;
    }

    public GraduationRejectionReason getReason() {
        return reason;
    }
}
