package grupo03sc.mailapp.users.domain;

import grupo03sc.mailapp.graduationprocesses.application.ParticipantKind;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Roles of a user account, with their scope and combination rules (FR-036, FR-050, FR-053).
 * The label is the Spanish name used in commands, replies and notices.
 */
public enum Role {
    STUDENT("estudiante"),
    PROFESSOR("docente"),
    THESIS_WORKSHOP_PROFESSOR("docente de taller de grado"),
    PROGRAM_SECRETARY("secretario"),
    PROGRAM_DIRECTOR("director de carrera"),
    ADMINISTRATOR("administrador");

    /** Roles of the accounts that the program director manages, and the only roles they assign. */
    private static final Set<Role> DIRECTOR_SCOPE = EnumSet.of(PROFESSOR, THESIS_WORKSHOP_PROFESSOR, PROGRAM_SECRETARY);

    /** Roles that an account cannot combine with any other role. */
    private static final Set<Role> SOLE_ROLES = EnumSet.of(STUDENT, PROGRAM_SECRETARY, ADMINISTRATOR);

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** The role with this label, ignoring case and repeated spaces. */
    public static Optional<Role> fromLabel(String text) {
        String normalized = text.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(role -> role.label.equals(normalized)).findFirst();
    }

    /** The labels of the roles, in a fixed order and separated by commas. */
    public static String labels(Set<Role> roles) {
        return roles.stream().sorted().map(Role::label).collect(Collectors.joining(", "));
    }

    /**
     * Whether an account with the manager roles may manage an account with the target roles, or
     * assign or remove them: the administrator manages every role, and the program director only
     * professors, the thesis workshop professor and program secretaries.
     */
    public static boolean canManage(Set<Role> managerRoles, Set<Role> targetRoles) {
        if (managerRoles.contains(ADMINISTRATOR)) {
            return true;
        }
        return managerRoles.contains(PROGRAM_DIRECTOR) && DIRECTOR_SCOPE.containsAll(targetRoles);
    }

    /** Rejects the combinations of roles that FR-036 does not allow. */
    public static void validateCombination(Set<Role> roles) {
        if (roles.size() < 2) {
            return;
        }
        for (Role role : roles) {
            if (SOLE_ROLES.contains(role)) {
                throw new UserRuleViolationException(RejectionReason.INVALID_ROLE_COMBINATION,
                        "el rol " + role.label + " no se combina con otros roles");
            }
        }
        if (roles.contains(THESIS_WORKSHOP_PROFESSOR) && !roles.equals(EnumSet.of(THESIS_WORKSHOP_PROFESSOR, PROFESSOR))) {
            throw new UserRuleViolationException(RejectionReason.INVALID_ROLE_COMBINATION,
                    "el rol " + THESIS_WORKSHOP_PROFESSOR.label + " solo se combina con el rol " + PROFESSOR.label);
        }
    }

    /** How an account with this role can take part in a graduation process. */
    public Set<ParticipantKind> participantKinds() {
        return switch (this) {
            case STUDENT -> EnumSet.of(ParticipantKind.STUDENT);
            case PROFESSOR -> EnumSet.of(ParticipantKind.TUTOR, ParticipantKind.TRIBUNAL_MEMBER);
            default -> EnumSet.noneOf(ParticipantKind.class);
        };
    }

    /** The participant kinds of all the given roles. */
    public static Set<ParticipantKind> participantKinds(Set<Role> roles) {
        Set<ParticipantKind> kinds = EnumSet.noneOf(ParticipantKind.class);
        roles.forEach(role -> kinds.addAll(role.participantKinds()));
        return kinds;
    }
}
