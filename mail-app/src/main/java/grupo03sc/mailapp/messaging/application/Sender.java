package grupo03sc.mailapp.messaging.application;

import java.util.Set;
import java.util.UUID;

/**
 * Identified sender: the active user account that sent the command, and its roles.
 */
public record Sender(UUID accountId, String email, Set<String> roles) {

    public Sender {
        roles = Set.copyOf(roles);
    }

    public boolean hasAnyRole(Set<String> allowedRoles) {
        return allowedRoles.isEmpty() || allowedRoles.stream().anyMatch(roles::contains);
    }
}
