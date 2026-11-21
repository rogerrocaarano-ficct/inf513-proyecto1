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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserManagementUseCaseTest {

    private final InMemoryUsers users = new InMemoryUsers();
    private final RecordingNotifier notifier = new RecordingNotifier();
    private final UserManagementUseCase useCase = new UserManagementUseCase(users, notifier);

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
        assertThat(notifier.events).singleElement().satisfies(e -> {
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
        assertThat(notifier.events).isEmpty();
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
        assertThat(notifier.events).isEmpty();
    }

    @Test
    void requesterWithoutPermissionDoesNotInsert() {
        existing("docente@uagrm.bo", Role.PROFESSOR);

        assertThatThrownBy(() -> useCase.signUpUser("docente@uagrm.bo", professor("luis@uagrm.bo")))
                .isInstanceOf(UserNotAuthorizedException.class);
        assertThat(users.inserted).isEmpty();
        assertThat(notifier.events).isEmpty();
    }

    @Test
    void listsTheAccountsOfTheRoleToTheRequester() {
        existing("Secretaria@uagrm.bo", Role.ADMINISTRATIVE);
        users.saved.add(User.rehydrate().withId(UUID.randomUUID()).withEmailAddress("ana@uagrm.bo")
                .withRole(Role.STUDENT).withFirstName("Ana María").withLastName("Pérez").withSecondLastName("Rojas")
                .withCi("1234567").withUagrmCode("218001234").withPhoneNumber("71234567"));
        users.saved.add(User.rehydrate().withId(UUID.randomUUID()).withEmailAddress("beto@uagrm.bo")
                .withRole(Role.STUDENT).withFirstName("Beto").withLastName("Arce"));

        useCase.listUsers("secretaria@UAGRM.bo", new ListUsersQuery("Estudiante"));

        assertThat(notifier.lists).singleElement().satisfies(result -> {
            assertThat(result.role()).isEqualTo("estudiante");
            assertThat(result.requesterUserEmail()).isEqualTo("secretaria@uagrm.bo");
            assertThat(result.users()).containsExactly(
                    new ListUsersQueryItem("Ana María Pérez Rojas", "71234567", "218001234", "ana@uagrm.bo",
                            "estudiante", "1234567"),
                    new ListUsersQueryItem("Beto Arce", null, null, "beto@uagrm.bo", "estudiante", null));
        });
    }

    @Test
    void roleWithoutAccountsSendsAnEmptyList() {
        existing("admin@uagrm.bo", Role.SYSTEM_ADMIN);

        useCase.listUsers("admin@uagrm.bo", new ListUsersQuery("docente_taller"));

        assertThat(notifier.lists).singleElement()
                .satisfies(result -> assertThat(result.users()).isEmpty());
    }

    @Test
    void listSenderWithoutAccountIsNotRegistered() {
        assertThatThrownBy(() -> useCase.listUsers("extrano@gmail.com", new ListUsersQuery("estudiante")))
                .isInstanceOf(UserNotAuthorizedException.class)
                .hasMessageContaining("no está registrada");
        assertThat(notifier.lists).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "rector"})
    void emptyOrUnknownRoleToListIsAValidationError(String role) {
        existing("admin@uagrm.bo", Role.SYSTEM_ADMIN);

        assertThatThrownBy(() -> useCase.listUsers("admin@uagrm.bo", new ListUsersQuery(role)))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrors()).containsOnlyKeys(Role.FIELD));
        assertThat(notifier.lists).isEmpty();
    }

    @Test
    void roleOutsideTheRequesterScopeIsRejectedWithoutReadingAccounts() {
        existing("director@uagrm.bo", Role.DIRECTOR);

        assertThatThrownBy(() -> useCase.listUsers("director@uagrm.bo", new ListUsersQuery("director")))
                .isInstanceOf(UserNotAuthorizedException.class)
                .hasMessageContaining("listar cuentas de director");
        assertThat(users.readByRole).isZero();
        assertThat(notifier.lists).isEmpty();
    }

    static class InMemoryUsers implements UserRepository {
        final List<User> saved = new ArrayList<>();
        final List<User> inserted = new ArrayList<>();
        DomainValidationException rejectInsert;
        int readByRole;

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

        /** Returns the accounts of the role in insertion order: the order is the adapter's job. */
        @Override
        public List<User> getByRole(Role role) {
            readByRole++;
            return saved.stream().filter(u -> u.getRole() == role).toList();
        }
    }

    static class RecordingNotifier implements UserManagementNotifier {
        final List<UserSignedUpEvent> events = new ArrayList<>();
        final List<ListUsersQueryResult> lists = new ArrayList<>();

        @Override
        public void notifyUserSignedUp(UserSignedUpEvent event) {
            events.add(event);
        }

        @Override
        public void sendUserList(ListUsersQueryResult queryResult) {
            lists.add(queryResult);
        }
    }
}
