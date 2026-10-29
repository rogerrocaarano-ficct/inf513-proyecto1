package grupo03sc.mailapp.users.domain;

import grupo03sc.mailapp.audit.application.FieldChange;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_account_roles", joinColumns = @JoinColumn(name = "user_account_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    protected UserAccount() {
    }

    private UserAccount(String fullName, String email, Set<Role> roles) {
        this.fullName = fullName.strip();
        this.email = normalizeEmail(email);
        this.roles = EnumSet.copyOf(roles);
    }

    public static UserAccount create(String fullName, String email, Set<Role> roles) {
        if (fullName == null || fullName.isBlank() || email == null || email.isBlank() || roles.isEmpty()) {
            throw new IllegalArgumentException("A user account needs a full name, an email address and at least one role");
        }
        return new UserAccount(fullName, email, roles);
    }

    /** Emails are stored and compared trimmed and in lower case. */
    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    /** The initial data of the account, for the change log. */
    public List<FieldChange> describe() {
        String roleNames = roles.stream().map(Role::name).sorted().collect(Collectors.joining(","));
        return List.of(
                FieldChange.created("fullName", fullName),
                FieldChange.created("email", email),
                FieldChange.created("roles", roleNames),
                FieldChange.created("status", status));
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Set<Role> getRoles() { return Set.copyOf(roles); }
    public AccountStatus getStatus() { return status; }
}
