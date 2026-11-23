package bo.org.tecnoweb.cpms.users.infrastructure.persistence;

import bo.org.tecnoweb.cpms.users.domain.Role;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

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
     * Finds and locks an account by its code (UC-002, FR-056).
     *
     * @param uagrmCode the code
     * @return the entity, or empty
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserAccountEntity> findForUpdateByUagrmCode(String uagrmCode);

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

    /**
     * Finds the accounts of a role.
     *
     * @param role the role
     * @param sort the order of the result
     * @return the entities, in that order
     */
    List<UserAccountEntity> findByRole(Role role, Sort sort);
}
