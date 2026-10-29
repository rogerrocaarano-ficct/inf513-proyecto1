package grupo03sc.mailapp.users.adapter.in;

import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.application.SenderAuthenticator;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Identifies the sender of a command with their active user account (AM-001, AD-007).
 */
@Component
class UserSenderAuthenticator implements SenderAuthenticator {

    private final UserAccountRepository accounts;

    UserSenderAuthenticator(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sender> authenticate(String senderEmail) {
        if (senderEmail == null || senderEmail.isBlank()) {
            return Optional.empty();
        }
        return accounts.findByEmail(UserAccount.normalizeEmail(senderEmail))
                .filter(UserAccount::isActive)
                .map(account -> new Sender(account.getId(), account.getEmail(),
                        account.getRoles().stream().map(Role::name).collect(Collectors.toSet())));
    }
}
