package bo.org.tecnoweb.cpms.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InitialAdminAccountTest {

    private final Users users = new Users();

    @Test
    void firstStartCreatesTheAdministrator() {
        new InitialAdminAccount(users, "Admin@CPMS.local").run(null);

        assertThat(users.inserted).singleElement().satisfies(admin -> {
            assertThat(admin.getEmailAddress()).isEqualTo("admin@cpms.local");
            assertThat(admin.getRole()).isEqualTo(Role.SYSTEM_ADMIN);
            assertThat(admin.getFullName()).isEqualTo("Administrador Sistema");
            assertThat(admin.pullEvents()).isEmpty();
        });
    }

    @Test
    void laterStartsDoNotCreateOrChangeTheAccount() {
        InitialAdminAccount runner = new InitialAdminAccount(users, "admin@cpms.local");
        runner.run(null);

        runner.run(null);

        assertThat(users.inserted).hasSize(1);
        assertThat(users.updated).isZero();
    }

    @Test
    void withoutAConfiguredAddressNothingIsCreated() {
        new InitialAdminAccount(users, "").run(null);
        new InitialAdminAccount(users, null).run(null);

        assertThat(users.inserted).isEmpty();
    }

    static class Users implements UserRepository {
        final List<User> inserted = new ArrayList<>();
        int updated;

        @Override
        public void insert(User user) {
            inserted.add(user);
        }

        @Override
        public void update(User user) {
            updated++;
        }

        @Override
        public Optional<User> getByEmail(String email) {
            return inserted.stream().filter(u -> u.getEmailAddress().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public Optional<User> getById(UUID id) {
            return inserted.stream().filter(u -> u.getId().equals(id)).findFirst();
        }

        @Override
        public List<User> getByRole(Role role) {
            throw new UnsupportedOperationException();
        }
    }
}
