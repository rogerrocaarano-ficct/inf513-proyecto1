package grupo03sc.mailapp.users.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.application.SenderAuthenticator;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserAccountsTests {

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private SenderAuthenticator authenticator;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void storesTheEmailInLowerCaseAndIdentifiesTheSenderIgnoringCase() {
        String local = "ana-" + UUID.randomUUID();
        UserAccount saved = save(local + "@UAGRM.edu.bo", Role.PROFESSOR);

        assertThat(saved.getEmail()).isEqualTo(local + "@uagrm.edu.bo");
        Optional<Sender> sender = authenticator.authenticate(local.toUpperCase() + "@uagrm.edu.bo");
        assertThat(sender).hasValueSatisfying(s -> {
            assertThat(s.accountId()).isEqualTo(saved.getId());
            assertThat(s.roles()).containsExactly("PROFESSOR");
        });
    }

    @Test
    void rejectsTwoAccountsWithTheSameEmail() {
        String email = "repetida-" + UUID.randomUUID() + "@uagrm.edu.bo";
        save(email, Role.PROFESSOR);

        assertThatThrownBy(() -> save(email.toUpperCase(), Role.STUDENT))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void doesNotAuthenticateAnUnknownSender() {
        assertThat(authenticator.authenticate("nadie-" + UUID.randomUUID() + "@uagrm.edu.bo")).isEmpty();
        assertThat(authenticator.authenticate("")).isEmpty();
    }

    @Test
    void doesNotAuthenticateAnInactiveAccount() {
        UserAccount saved = save("inactiva-" + UUID.randomUUID() + "@uagrm.edu.bo", Role.PROFESSOR);
        jdbc.update("UPDATE user_accounts SET status = 'INACTIVE' WHERE id = ?", saved.getId());

        assertThat(authenticator.authenticate(saved.getEmail())).isEmpty();
    }

    private UserAccount save(String email, Role role) {
        return transaction.execute(status ->
                accounts.saveAndFlush(UserAccount.create("Usuario de Prueba", email, Set.of(role))));
    }
}
