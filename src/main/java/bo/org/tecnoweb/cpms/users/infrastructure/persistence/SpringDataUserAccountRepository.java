package bo.org.tecnoweb.cpms.users.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository of {@link UserAccountEntity}, used only by {@link JpaUserRepository}.
 */
interface SpringDataUserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {

    /**
     * Finds an account by its email address.
     *
     * @param emailAddress the address, in lower case
     * @return the entity, or empty
     */
    Optional<UserAccountEntity> findByEmailAddress(String emailAddress);

    /**
     * Tells whether another account has an email address.
     *
     * @param emailAddress the address, in lower case
     * @param id           the account to leave out
     * @return {@code true} if an account other than {@code id} has the address
     */
    boolean existsByEmailAddressAndIdNot(String emailAddress, UUID id);

    /**
     * Tells whether another account has a code.
     *
     * @param uagrmCode the code
     * @param id        the account to leave out
     * @return {@code true} if an account other than {@code id} has the code
     */
    boolean existsByUagrmCodeAndIdNot(String uagrmCode, UUID id);
}
