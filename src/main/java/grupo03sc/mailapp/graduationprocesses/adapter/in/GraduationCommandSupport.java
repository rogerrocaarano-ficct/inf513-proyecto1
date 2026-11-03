package grupo03sc.mailapp.graduationprocesses.adapter.in;

import grupo03sc.mailapp.graduationprocesses.domain.DocumentFile;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRejectionReason;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRuleViolationException;
import grupo03sc.mailapp.messaging.domain.MailAttachment;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shared by the graduation process and graduation project commands: reading the arguments and the
 * attachments (UC-003, UC-004).
 */
final class GraduationCommandSupport {

    /** Role allowed to run every graduation process command (UC-003 preconditions). */
    static final Set<String> SECRETARY_ROLES = Set.of("PROGRAM_SECRETARY");

    /** Role allowed to run every graduation project command, on the student's own process (UC-004). */
    static final Set<String> STUDENT_ROLES = Set.of("STUDENT");

    private GraduationCommandSupport() {
    }

    /**
     * The arguments, one per name: rejects a command with more arguments than its form (INVALID_DATA)
     * and lists every missing or blank one (E1).
     */
    static List<String> required(List<String> arguments, String form, String... names) {
        if (arguments.size() > names.length) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.INVALID_DATA,
                    "el comando tiene la forma " + form);
        }
        List<String> values = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String value = i < arguments.size() ? arguments.get(i).strip() : "";
            if (value.isEmpty()) {
                missing.add(names[i]);
            }
            values.add(value);
        }
        if (!missing.isEmpty()) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.MISSING_DATA, String.join(", ", missing));
        }
        return values;
    }

    /** The attachments of the email as files of GraduationProcesses. */
    static List<DocumentFile> files(List<MailAttachment> attachments) {
        return attachments.stream()
                .map(attachment -> new DocumentFile(attachment.fileName(), attachment.contentType(), attachment.content()))
                .toList();
    }
}
