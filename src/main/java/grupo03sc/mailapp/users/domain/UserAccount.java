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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    private static final Pattern EMAIL_FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

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

    @Column(name = "student_registration_number")
    private String studentRegistrationNumber;

    @Column(name = "id_card")
    private String idCard;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    protected UserAccount() {
    }

    /** A new active account; rejects missing data (E5) and an invalid email address. */
    public static UserAccount create(UserAccountData data) {
        UserAccount account = new UserAccount();
        account.fullName = clean(data.fullName());
        account.email = data.email() == null ? null : normalizeEmail(data.email());
        account.roles = data.roles().isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(data.roles());
        account.studentRegistrationNumber = clean(data.studentRegistrationNumber());
        account.idCard = clean(data.idCard());
        account.phone = clean(data.phone());
        account.checkComplete();
        return account;
    }

    /** Emails are stored and compared trimmed and in lower case. */
    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    /** Changes one field other than the roles, and returns the change for the change log. */
    public FieldChange changeField(AccountField field, String value) {
        String newValue = field == AccountField.EMAIL && value != null ? normalizeEmail(value) : clean(value);
        String oldValue = switch (field) {
            case FULL_NAME -> replace(fullName, () -> fullName = newValue);
            case EMAIL -> replace(email, () -> email = newValue);
            case STUDENT_REGISTRATION_NUMBER -> replace(studentRegistrationNumber, () -> studentRegistrationNumber = newValue);
            case ID_CARD -> replace(idCard, () -> idCard = newValue);
            case PHONE -> replace(phone, () -> phone = newValue);
            case ROLES -> throw new IllegalArgumentException("Roles are changed with changeRoles");
        };
        checkComplete();
        return new FieldChange(field.property(), oldValue, newValue);
    }

    /** Replaces the roles, and returns the change for the change log. */
    public FieldChange changeRoles(Set<Role> newRoles) {
        String oldValue = roleNames(roles);
        roles = newRoles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(newRoles);
        checkComplete();
        return new FieldChange(AccountField.ROLES.property(), oldValue, roleNames(roles));
    }

    public FieldChange deactivate() {
        if (!isActive()) {
            throw new UserRuleViolationException(RejectionReason.INVALID_DATA, "la cuenta de usuario ya está desactivada");
        }
        status = AccountStatus.INACTIVE;
        return new FieldChange("status", AccountStatus.ACTIVE.name(), AccountStatus.INACTIVE.name());
    }

    public FieldChange reactivate() {
        if (isActive()) {
            throw new UserRuleViolationException(RejectionReason.INVALID_DATA, "la cuenta de usuario ya está activa");
        }
        status = AccountStatus.ACTIVE;
        return new FieldChange("status", AccountStatus.INACTIVE.name(), AccountStatus.ACTIVE.name());
    }

    /** The initial data of the account, for the change log. */
    public List<FieldChange> describe() {
        List<FieldChange> changes = new ArrayList<>(List.of(
                FieldChange.created(AccountField.FULL_NAME.property(), fullName),
                FieldChange.created(AccountField.EMAIL.property(), email),
                FieldChange.created(AccountField.ROLES.property(), roleNames(roles))));
        if (studentRegistrationNumber != null) {
            changes.add(FieldChange.created(AccountField.STUDENT_REGISTRATION_NUMBER.property(), studentRegistrationNumber));
        }
        if (idCard != null) {
            changes.add(FieldChange.created(AccountField.ID_CARD.property(), idCard));
        }
        if (phone != null) {
            changes.add(FieldChange.created(AccountField.PHONE.property(), phone));
        }
        changes.add(FieldChange.created("status", status));
        return changes;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    /** Rejects an account without its mandatory data (E5) or with an invalid email address. */
    private void checkComplete() {
        List<String> missing = new ArrayList<>();
        if (fullName == null) {
            missing.add("nombre completo");
        }
        if (email == null || email.isEmpty()) {
            missing.add("dirección de correo");
        }
        if (roles.isEmpty()) {
            missing.add("roles");
        }
        if (roles.contains(Role.STUDENT)) {
            if (studentRegistrationNumber == null) {
                missing.add("registro universitario");
            }
            if (idCard == null) {
                missing.add("cédula de identidad");
            }
            if (phone == null) {
                missing.add("teléfono");
            }
        }
        if (!missing.isEmpty()) {
            throw new UserRuleViolationException(RejectionReason.MISSING_DATA, String.join(", ", missing));
        }
        if (!EMAIL_FORMAT.matcher(email).matches()) {
            throw new UserRuleViolationException(RejectionReason.INVALID_DATA,
                    "la dirección de correo " + email + " no es válida");
        }
    }

    private static String replace(String oldValue, Runnable assignment) {
        assignment.run();
        return oldValue;
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String roleNames(Set<Role> roles) {
        return roles.stream().map(Role::name).sorted().collect(Collectors.joining(","));
    }

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Set<Role> getRoles() { return Set.copyOf(roles); }
    public String getStudentRegistrationNumber() { return studentRegistrationNumber; }
    public String getIdCard() { return idCard; }
    public String getPhone() { return phone; }
    public AccountStatus getStatus() { return status; }

    @Override
    public String toString() {
        return "UserAccount[" + Objects.toString(email) + "]";
    }
}
