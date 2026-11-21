package bo.org.tecnoweb.cpms.users.domain;

import bo.org.tecnoweb.cpms.common.AggregateRoot;
import bo.org.tecnoweb.cpms.common.DomainEvent;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User account: the aggregate root of the users subsystem.
 *
 * <p>The account is identified by its email address, which is the sender of its email commands. It
 * has one {@link Role}; what it may do is decided by the {@link RolePermission}s.
 *
 * <p>A new account is built fluently with {@link #builder()} and the {@code with*} methods, and then
 * registered with {@link #signUp(User)}, which checks the requester's permission, normalizes and
 * validates the data and raises a {@link UserSignedUpEvent}. An account read from storage is built
 * with {@link #rehydrate()} and raises no events.
 */
public class User implements AggregateRoot {

    /** Field name of the first name or names in validation errors. */
    public static final String FIRST_NAME = "nombres";
    /** Field name of the first surname in validation errors. */
    public static final String LAST_NAME = "primer apellido";
    /** Field name of the second surname in validation errors. */
    public static final String SECOND_LAST_NAME = "segundo apellido";
    /** Field name of the email address in validation errors. */
    public static final String EMAIL = "correo";
    /** Field name of the identity card number in validation errors. */
    public static final String CI = "CI";
    /** Field name of the university code in validation errors. */
    public static final String CODE = "código";
    /** Field name of the phone number in validation errors. */
    public static final String PHONE = "teléfono";

    private static final Locale SPANISH = Locale.forLanguageTag("es");
    private static final Pattern NAME = Pattern.compile("\\p{L}+( \\p{L}+)*");
    private static final Pattern EMAIL_ADDRESS = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final Pattern PHONE_NUMBER = Pattern.compile("\\d{1,8}");
    private static final Pattern UAGRM_CODE = Pattern.compile("\\d{1,10}");
    private static final Pattern CI_NUMBER = Pattern.compile("\\d{1,7}");

    private UUID id;
    private String emailAddress;
    private Role role;
    private String firstName;
    private String lastName;
    private String secondLastName;
    private String ci;
    private String uagrmCode;
    private String phoneNumber;
    private final List<DomainEvent> events = new ArrayList<>();

    private User() {
    }

    /**
     * Starts a new account with a new identity.
     *
     * @return an empty account with a random identifier, to complete with the {@code with*} methods
     */
    public static User builder() {
        User user = new User();
        user.id = UUID.randomUUID();
        return user;
    }

    /**
     * Starts an account that already exists, read from storage.
     *
     * @return an empty account whose identity must be set with {@link #withId(UUID)}
     */
    public static User rehydrate() {
        return new User();
    }

    /**
     * Sets the identity.
     *
     * @param id the identifier
     * @return this account
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public User withId(UUID id) {
        this.id = Objects.requireNonNull(id);
        return this;
    }

    /**
     * Sets the email address, stored without surrounding spaces and in lower case.
     *
     * @param emailAddress the address
     * @return this account
     */
    public User withEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress == null ? null : emailAddress.strip().toLowerCase(Locale.ROOT);
        return this;
    }

    /**
     * Sets the role.
     *
     * @param role the role of the account
     * @return this account
     */
    public User withRole(Role role) {
        this.role = role;
        return this;
    }

    /**
     * Sets the first name or names.
     *
     * @param firstName the names
     * @return this account
     */
    public User withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Sets the first surname.
     *
     * @param lastName the first surname
     * @return this account
     */
    public User withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Sets the second surname.
     *
     * @param secondLastName the second surname; may be empty
     * @return this account
     */
    public User withSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
        return this;
    }

    /**
     * Sets the identity card number.
     *
     * @param ci the CI; may be empty except for students
     * @return this account
     */
    public User withCi(String ci) {
        this.ci = ci;
        return this;
    }

    /**
     * Sets the code of the person in the university's system.
     *
     * @param uagrmCode student registration number, or administrative or professor code; may be empty
     *                  except for students
     * @return this account
     */
    public User withUagrmCode(String uagrmCode) {
        this.uagrmCode = uagrmCode;
        return this;
    }

    /**
     * Sets the phone number.
     *
     * @param phoneNumber the number; may be empty except for students
     * @return this account
     */
    public User withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /**
     * Registers this new account on behalf of a requester.
     *
     * <p>First checks that the requester's role allows creating accounts of this account's role. Then
     * normalizes the names (spaces, initial capital letters) and validates every field, reporting all
     * the invalid ones together. Finally raises a {@link UserSignedUpEvent}.
     *
     * @param requester the account that asked to create this one
     * @return this account
     * @throws UserNotAuthorizedException if the requester's role may not create accounts of this role
     * @throws DomainValidationException  if the role is missing or any field is invalid
     */
    public User signUp(User requester) {
        Objects.requireNonNull(requester, "requester");
        if (role == null) {
            throw new DomainValidationException(Map.of(Role.FIELD, "Es obligatorio."));
        }
        RolePermission permission = RolePermission.toSignUp(role);
        if (!requester.hasPermissionTo(permission)) {
            throw new UserNotAuthorizedException(requester.getEmailAddress(), permission.getLabel());
        }

        firstName = normalizeName(firstName);
        lastName = normalizeName(lastName);
        secondLastName = normalizeName(secondLastName);
        ci = blankToNull(ci);
        uagrmCode = blankToNull(uagrmCode);
        phoneNumber = blankToNull(phoneNumber);
        validate();

        events.add(new UserSignedUpEvent(emailAddress, getFullName(), role.getLabel(), Instant.now(),
                requester.getFullName(), requester.getEmailAddress()));
        return this;
    }

    private void validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        checkName(errors, FIRST_NAME, firstName, true);
        checkName(errors, LAST_NAME, lastName, true);
        checkName(errors, SECOND_LAST_NAME, secondLastName, false);
        if (emailAddress == null || !EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            errors.put(EMAIL, "No es una dirección de correo válida.");
        }
        boolean student = role == Role.STUDENT;
        checkDigits(errors, CI, ci, CI_NUMBER, 7, student);
        checkDigits(errors, CODE, uagrmCode, UAGRM_CODE, 10, student);
        checkDigits(errors, PHONE, phoneNumber, PHONE_NUMBER, 8, student);
        if (!errors.isEmpty()) {
            throw new DomainValidationException(errors);
        }
    }

    private static void checkName(Map<String, String> errors, String field, String value, boolean required) {
        if (value == null) {
            if (required) {
                errors.put(field, "Es obligatorio.");
            }
        } else if (!NAME.matcher(value).matches()) {
            errors.put(field, "Solo puede contener letras y espacios.");
        }
    }

    private static void checkDigits(Map<String, String> errors, String field, String value, Pattern format,
            int maxDigits, boolean required) {
        if (value == null) {
            if (required) {
                errors.put(field, "Es obligatorio para estudiantes.");
            }
        } else if (!format.matcher(value).matches()) {
            errors.put(field, "Debe tener solo dígitos, hasta " + maxDigits + ".");
        }
    }

    /**
     * Trims, collapses inner spaces and capitalizes each word; {@code null} if nothing is left.
     */
    private static String normalizeName(String value) {
        String collapsed = blankToNull(value);
        if (collapsed == null) {
            return null;
        }
        return Arrays.stream(collapsed.split("\\s+"))
                .map(User::capitalize)
                .collect(Collectors.joining(" "));
    }

    private static String capitalize(String word) {
        int first = word.codePointAt(0);
        int rest = Character.charCount(first);
        return new StringBuilder()
                .appendCodePoint(Character.toTitleCase(first))
                .append(word.substring(rest).toLowerCase(SPANISH))
                .toString();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    /**
     * Tells whether this account may perform an operation.
     *
     * @param rolePermission the permission the operation requires
     * @return {@code true} if this account's role holds the permission
     */
    public boolean hasPermissionTo(RolePermission rolePermission) {
        return role != null && role.isAllowedTo(rolePermission);
    }

    /**
     * Returns the full name: names, first surname and second surname, if any.
     *
     * @return the full name
     */
    public String getFullName() {
        return Stream.of(firstName, lastName, secondLastName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    /**
     * Returns the email address.
     *
     * @return the address, in lower case
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Returns the role.
     *
     * @return the role of the account
     */
    public Role getRole() {
        return role;
    }

    /**
     * Returns the first name or names.
     *
     * @return the names
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the first surname.
     *
     * @return the first surname
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the second surname.
     *
     * @return the second surname, or {@code null} if it has none
     */
    public String getSecondLastName() {
        return secondLastName;
    }

    /**
     * Returns the identity card number.
     *
     * @return the CI, or {@code null} if it has none
     */
    public String getCi() {
        return ci;
    }

    /**
     * Returns the code of the person in the university's system.
     *
     * @return the code, or {@code null} if it has none
     */
    public String getUagrmCode() {
        return uagrmCode;
    }

    /**
     * Returns the phone number.
     *
     * @return the number, or {@code null} if it has none
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
