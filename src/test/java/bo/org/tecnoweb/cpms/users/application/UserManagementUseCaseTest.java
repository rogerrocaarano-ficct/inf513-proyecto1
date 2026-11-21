package bo.org.tecnoweb.cpms.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import bo.org.tecnoweb.cpms.users.domain.UserSignedUpEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserManagementUseCaseTest {

    private final InMemoryUsers users = new InMemoryUsers();
    private final RecordingBus bus = new RecordingBus();
    private final UserManagementUseCase useCase = new UserManagementUseCase(users, bus);

    private static SignUpUserCommand professor(String email) {
        return new SignUpUserCommand("Luis", "Vaca", "", "Docente", "", "", email, "");
    }

    private void existing(String email, Role role) {
        users.saved.add(User.rehydrate().withId(UUID.randomUUID()).withEmailAddress(email).withRole(role)
                .withFirstName("Carla").withLastName("Suárez"));
    }

    @Test
    void createsTheAccountAndPublishesOneEvent() {
        existing("director@uagrm.bo", Role.DIRECTOR);

        useCase.signUpUser("Director@UAGRM.bo", professor("luis@uagrm.bo"));

        assertThat(users.getByEmail("luis@uagrm.bo")).get()
                .satisfies(u -> assertThat(u.getRole()).isEqualTo(Role.PROFESSOR));
        assertThat(bus.events).singleElement().satisfies(e -> {
            assertThat(e.signedUpUser()).isEqualTo("luis@uagrm.bo");
            assertThat(e.requesterUserEmail()).isEqualTo("director@uagrm.bo");
            assertThat(e.signedUpUserRole()).isEqualTo("docente");
        });
    }

    @Test
    void senderWithoutAccountIsNotRegistered() {
        assertThatThrownBy(() -> useCase.signUpUser("extrano@gmail.com", professor("luis@uagrm.bo")))
                .isInstanceOf(UserNotAuthorizedException.class)
                .hasMessageContaining("no está registrada");
        assertThat(users.inserted).isEmpty();
        assertThat(bus.events).isEmpty();
    }

    @Test
    void unknownRoleIsAValidationError() {
        existing("admin@uagrm.bo", Role.SYSTEM_ADMIN);
        SignUpUserCommand command = new SignUpUserCommand("Luis", "Vaca", "", "rector", "", "", "luis@uagrm.bo", "");

        assertThatThrownBy(() -> useCase.signUpUser("admin@uagrm.bo", command))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrors()).containsOnlyKeys(Role.FIELD));
        assertThat(users.inserted).isEmpty();
    }

    @Test
    void duplicateFromTheRepositoryIsPropagatedWithoutPublishing() {
        existing("admin@uagrm.bo", Role.SYSTEM_ADMIN);
        users.rejectInsert = new DomainValidationException(Map.of("correo", "Ya pertenece a otra cuenta."));

        assertThatThrownBy(() -> useCase.signUpUser("admin@uagrm.bo", professor("luis@uagrm.bo")))
                .isSameAs(users.rejectInsert);
        assertThat(bus.events).isEmpty();
    }

    @Test
    void requesterWithoutPermissionDoesNotInsert() {
        existing("docente@uagrm.bo", Role.PROFESSOR);

        assertThatThrownBy(() -> useCase.signUpUser("docente@uagrm.bo", professor("luis@uagrm.bo")))
                .isInstanceOf(UserNotAuthorizedException.class);
        assertThat(users.inserted).isEmpty();
        assertThat(bus.events).isEmpty();
    }

    static class InMemoryUsers implements UserRepository {
        final List<User> saved = new ArrayList<>();
        final List<User> inserted = new ArrayList<>();
        DomainValidationException rejectInsert;

        @Override
        public void insert(User user) {
            if (rejectInsert != null) {
                throw rejectInsert;
            }
            inserted.add(user);
            saved.add(user);
        }

        @Override
        public void update(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<User> getByEmail(String email) {
            return saved.stream().filter(u -> u.getEmailAddress().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public Optional<User> getById(UUID id) {
            return saved.stream().filter(u -> u.getId().equals(id)).findFirst();
        }
    }

    static class RecordingBus implements UserManagementEventBus {
        final List<UserSignedUpEvent> events = new ArrayList<>();

        @Override
        public void notifyUserSignedUpEvent(UserSignedUpEvent event) {
            events.add(event);
        }
    }
}
