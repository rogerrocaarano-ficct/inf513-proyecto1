package bo.org.tecnoweb.cpms.users.domain;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port to persist user accounts.
 */
public interface UserRepository {

    /**
     * Saves a new account.
     *
     * @param user the account to insert
     * @throws DomainValidationException if its email address or its code already belong to another
     *                                   account, with one error per repeated field (FR-033)
     */
    void insert(User user);

    /**
     * Saves the changes of an existing account.
     *
     * @param user the account to update
     * @throws DomainValidationException if its email address or its code already belong to another
     *                                   account, with one error per repeated field (FR-033)
     */
    void update(User user);

    /**
     * Finds an account by its email address, without case.
     *
     * @param email the address
     * @return the account, or empty if no account has that address
     */
    Optional<User> getByEmail(String email);

    /**
     * Finds an account by its code (the student registration number) and locks it until the
     * transaction ends, so that two openings for the same student run one after the other (UC-002,
     * FR-056).
     *
     * @param code the code, without surrounding spaces
     * @return the account, or empty if no account has that code
     */
    Optional<User> getByUagrmCodeForUpdate(String code);

    /**
     * Finds an account by its identifier.
     *
     * @param id the identifier
     * @return the account, or empty if it does not exist
     */
    Optional<User> getById(UUID id);

    /**
     * Finds every account of a role.
     *
     * @param role the role
     * @return the accounts of that role, ordered by first surname, second surname (an account without
     *         one goes first) and names; empty if there are none
     */
    List<User> getByRole(Role role);
}
