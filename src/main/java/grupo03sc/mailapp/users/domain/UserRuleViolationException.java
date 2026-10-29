package grupo03sc.mailapp.users.domain;

import grupo03sc.mailapp.messaging.application.CommandRejectedException;

/**
 * A user account request that breaks a rule of UC-001. Messaging rolls back the command and
 * replies with the reason and its detail (D2).
 */
public class UserRuleViolationException extends CommandRejectedException {

    private final RejectionReason reason;

    public UserRuleViolationException(RejectionReason reason, String detail) {
        super(reason.message(detail));
        this.reason = reason;
    }

    public RejectionReason getReason() {
        return reason;
    }
}
