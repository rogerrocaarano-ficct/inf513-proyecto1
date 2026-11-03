package grupo03sc.mailapp.users.application;

import grupo03sc.mailapp.users.domain.AccountStatus;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * Persistence port of Users, implemented by Spring Data JPA. Emails are looked up already
 * normalized (see {@link UserAccount#normalizeEmail}).
 */
public interface UserAccountRepository extends Repository<UserAccount, UUID> {

    UserAccount save(UserAccount account);

    UserAccount saveAndFlush(UserAccount account);

    Optional<UserAccount> findById(UUID id);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByStudentRegistrationNumber(String number);

    List<UserAccount> findAllByOrderByFullNameAsc();

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByStudentRegistrationNumber(String number);

    boolean existsByStudentRegistrationNumberAndIdNot(String number, UUID id);

    @Query("select count(a) > 0 from UserAccount a join a.roles r where r = :role and a.status = :status")
    boolean existsWithRoleAndStatus(Role role, AccountStatus status);

    /** Whether another active account than the excluded one has the role. */
    @Query("select count(a) > 0 from UserAccount a join a.roles r"
            + " where r = :role and a.status = grupo03sc.mailapp.users.domain.AccountStatus.ACTIVE and a.id <> :excludedId")
    boolean existsOtherActiveWithRole(Role role, UUID excludedId);
}
