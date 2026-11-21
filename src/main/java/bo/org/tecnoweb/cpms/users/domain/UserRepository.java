package bo.org.tecnoweb.cpms.users.domain;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
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
     * Finds an account by its identifier.
     *
     * @param id the identifier
     * @return the account, or empty if it does not exist
     */
    Optional<User> getById(UUID id);
}
