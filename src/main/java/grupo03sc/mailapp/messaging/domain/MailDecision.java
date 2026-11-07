package grupo03sc.mailapp.messaging.domain;

public enum MailDecision {
    /** A command for the system: handle it and delete it afterwards. */
    PROCESS,
    /** Not for the system, or sent by it: leave it in the mailbox. */
    LEAVE,
    /** For the system but not to be answered (automatic reply): delete it. */
    DISCARD
}
