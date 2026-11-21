package bo.org.tecnoweb.cpms.users.infrastructure.persistence;

import bo.org.tecnoweb.cpms.users.domain.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * JPA entity of a {@link bo.org.tecnoweb.cpms.users.domain.User}, table {@code user_account}.
 *
 * <p>Accessors are package-private: only {@link JpaUserRepository} uses this class.
 */
@Entity
@Table(name = "user_account")
class UserAccountEntity {

    @Id
    private UUID id;

    @Column(name = "email_address", nullable = false, unique = true)
    private String emailAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "second_last_name")
    private String secondLastName;

    private String ci;

    @Column(name = "uagrm_code", unique = true)
    private String uagrmCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Constructor required by JPA.
     */
    protected UserAccountEntity() {
    }

    /**
     * Creates the entity of a new account.
     *
     * @param id identifier of the domain account
     */
    UserAccountEntity(UUID id) {
        this.id = id;
    }

    UUID getId() {
        return id;
    }

    String getEmailAddress() {
        return emailAddress;
    }

    void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    Role getRole() {
        return role;
    }

    void setRole(Role role) {
        this.role = role;
    }

    String getFirstName() {
        return firstName;
    }

    void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    String getLastName() {
        return lastName;
    }

    void setLastName(String lastName) {
        this.lastName = lastName;
    }

    String getSecondLastName() {
        return secondLastName;
    }

    void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
    }

    String getCi() {
        return ci;
    }

    void setCi(String ci) {
        this.ci = ci;
    }

    String getUagrmCode() {
        return uagrmCode;
    }

    void setUagrmCode(String uagrmCode) {
        this.uagrmCode = uagrmCode;
    }

    String getPhoneNumber() {
        return phoneNumber;
    }

    void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
