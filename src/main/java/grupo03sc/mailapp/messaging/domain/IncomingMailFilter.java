package grupo03sc.mailapp.messaging.domain;

import java.util.Locale;

/**
 * Decides which emails the system handles, so that it never answers itself in a loop.
 */
public final class IncomingMailFilter {

    private final String systemAddress;

    public IncomingMailFilter(String systemAddress) {
        this.systemAddress = normalize(systemAddress);
    }

    public MailDecision decide(IncomingMail mail) {
        if (systemAddress.equals(normalize(mail.from()))) {
            return MailDecision.LEAVE;
        }
        boolean addressedToSystem = mail.recipients().stream().map(IncomingMailFilter::normalize).anyMatch(systemAddress::equals);
        if (!addressedToSystem) {
            return MailDecision.LEAVE;
        }
        if (isAutomaticReply(mail.autoSubmitted())) {
            return MailDecision.DISCARD;
        }
        return MailDecision.PROCESS;
    }

    private static boolean isAutomaticReply(String autoSubmitted) {
        return autoSubmitted != null && !autoSubmitted.isBlank() && !autoSubmitted.strip().equalsIgnoreCase("no");
    }

    private static String normalize(String address) {
        return address == null ? "" : address.strip().toLowerCase(Locale.ROOT);
    }
}
