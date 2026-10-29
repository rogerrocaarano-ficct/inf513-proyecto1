package grupo03sc.mailapp.users.domain;

import java.util.Set;

/**
 * The data of a user account received in a command. The student data may be null for other roles.
 */
public record UserAccountData(String fullName, String email, Set<Role> roles,
        String studentRegistrationNumber, String idCard, String phone) {

    public UserAccountData {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    /** An account without student data. */
    public static UserAccountData of(String fullName, String email, Set<Role> roles) {
        return new UserAccountData(fullName, email, roles, null, null, null);
    }
}
