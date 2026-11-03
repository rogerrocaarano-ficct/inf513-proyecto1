package grupo03sc.mailapp.users.adapter.in;

import grupo03sc.mailapp.users.application.AffectedProcess;
import grupo03sc.mailapp.users.domain.AccountField;
import grupo03sc.mailapp.users.domain.RejectionReason;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserRuleViolationException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared by the user management commands: reading the arguments and writing the replies (D1, D7).
 */
final class UserCommandSupport {

    /**
     * Roles allowed to run every user management command (UC-001 preconditions).
     */
    static final Set<String> MANAGER_ROLES = Set.of(Role.ADMINISTRATOR.name(), Role.PROGRAM_DIRECTOR.name());

    private UserCommandSupport() {
    }

    /**
     * The argument at the index, or null when it is missing or blank.
     */
    static String argument(List<String> arguments, int index) {
        if (index >= arguments.size() || arguments.get(index).isBlank()) {
            return null;
        }
        return arguments.get(index).strip();
    }

    /**
     * The argument at the index, rejecting the command when it is missing (E5).
     */
    static String required(List<String> arguments, int index, String name) {
        String value = argument(arguments, index);
        if (value == null) {
            throw new UserRuleViolationException(RejectionReason.MISSING_DATA, name);
        }
        return value;
    }

    /**
     * Rejects a command with more arguments than its form allows.
     */
    static void atMost(List<String> arguments, int count, String form) {
        if (arguments.size() > count) {
            throw new UserRuleViolationException(RejectionReason.INVALID_DATA,
                    "el comando tiene la forma " + form);
        }
    }

    /**
     * Roles written by their Spanish names and separated by commas, ignoring case.
     */
    static Set<Role> roles(String text) {
        Set<Role> roles = EnumSet.noneOf(Role.class);
        List<String> unknown = new ArrayList<>();
        if (text != null) {
            for (String name : text.split(",")) {
                if (name.isBlank()) {
                    continue;
                }
                Role.fromLabel(name).ifPresentOrElse(roles::add, () -> unknown.add(name.strip()));
            }
        }
        if (!unknown.isEmpty()) {
            throw new UserRuleViolationException(RejectionReason.INVALID_DATA,
                    "no se reconoce el rol " + String.join(", ", unknown) + "; los roles son: " + allRoleLabels());
        }
        return roles;
    }

    static AccountField field(String text) {
        return AccountField.fromLabel(text).orElseThrow(() -> new UserRuleViolationException(
                RejectionReason.INVALID_DATA, "el campo " + text + " no existe; los campos son: " + AccountField.labels()));
    }

    static String status(UserAccount account) {
        return account.isActive() ? "activa" : "desactivada";
    }

    /**
     * One line of ListarUsuarios(): name, email, roles and status.
     */
    static String summary(UserAccount account) {
        return "%s <%s> %s (%s)".formatted(account.getFullName(), account.getEmail(),
                Role.labels(account.getRoles()), status(account));
    }

    /**
     * VerUsuario(correo): one field per line.
     */
    static String details(UserAccount account) {
        List<String> lines = new ArrayList<>(List.of(
                "Nombre completo: " + account.getFullName(),
                "Dirección de correo: " + account.getEmail(),
                "Roles: " + Role.labels(account.getRoles()),
                "Estado: " + status(account)));
        if (account.getStudentRegistrationNumber() != null) {
            lines.add("Registro universitario: " + account.getStudentRegistrationNumber());
        }
        if (account.getIdCard() != null) {
            lines.add("Cédula de identidad: " + account.getIdCard());
        }
        if (account.getPhone() != null) {
            lines.add("Teléfono: " + account.getPhone());
        }
        return String.join("\n", lines);
    }

    /**
     * Confirmation followed, when there are any, by the processes left pending reassignment (FR-034).
     */
    static String withAffectedProcesses(String confirmation, List<AffectedProcess> processes) {
        if (processes.isEmpty()) {
            return confirmation;
        }
        return confirmation + "\n\nAtención: estos trámites de titulación en curso quedaron pendientes de reasignación:\n"
                + processes.stream().map(p -> "- Trámite de " + p.studentName() + " (" + p.id() + ")")
                .collect(Collectors.joining("\n"));
    }

    private static String allRoleLabels() {
        return Role.labels(EnumSet.allOf(Role.class));
    }
}
