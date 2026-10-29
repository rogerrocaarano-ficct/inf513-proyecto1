package grupo03sc.mailapp.users.application;

import grupo03sc.mailapp.users.domain.AccountStatus;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface UserAccountRepository extends Repository<UserAccount, UUID> {

    UserAccount save(UserAccount account);

    UserAccount saveAndFlush(UserAccount account);

    /** Looks up an already normalized address (see {@link UserAccount#normalizeEmail}). */
    Optional<UserAccount> findByEmail(String email);

    @Query("select count(a) > 0 from UserAccount a join a.roles r where r = :role and a.status = :status")
    boolean existsWithRoleAndStatus(Role role, AccountStatus status);
}
