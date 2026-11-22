package bo.org.tecnoweb.cpms.users.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class JpaUserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private SpringDataUserAccountRepository springData;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        springData.deleteAll();
    }

    private static User account(String email, String code) {
        return User.builder().withEmailAddress(email).withRole(Role.STUDENT).withFirstName("Ana María")
                .withLastName("Pérez").withSecondLastName("Rojas").withCi("1234567").withUagrmCode(code)
                .withPhoneNumber("71234567");
    }

    @Test
    void insertsAndReadsAnAccount() {
        User ana = account("ana@uagrm.bo", "218001234");

        repository.insert(ana);

        User read = repository.getById(ana.getId()).orElseThrow();
        assertThat(read.getEmailAddress()).isEqualTo("ana@uagrm.bo");
        assertThat(read.getRole()).isEqualTo(Role.STUDENT);
        assertThat(read.getFullName()).isEqualTo("Ana María Pérez Rojas");
        assertThat(read.getCi()).isEqualTo("1234567");
        assertThat(read.getUagrmCode()).isEqualTo("218001234");
        assertThat(read.getPhoneNumber()).isEqualTo("71234567");
        assertThat(read.pullEvents()).isEmpty();
    }

    private static User named(String email, Role role, String firstName, String lastName, String secondLastName) {
        return User.builder().withEmailAddress(email).withRole(role).withFirstName(firstName)
                .withLastName(lastName).withSecondLastName(secondLastName);
    }

    @Test
    void getByRoleReturnsOnlyThatRoleOrderedBySurnamesAndNames() {
        repository.insert(named("carla@uagrm.bo", Role.PROFESSOR, "Carla", "Suarez", "Vaca"));
        repository.insert(named("luis@uagrm.bo", Role.PROFESSOR, "Luis", "Perez", "Rojas"));
        repository.insert(named("ana@uagrm.bo", Role.PROFESSOR, "Ana", "Perez", "Rojas"));
        repository.insert(named("beto@uagrm.bo", Role.PROFESSOR, "Beto", "Perez", null));
        repository.insert(named("dir@uagrm.bo", Role.DIRECTOR, "Diego", "Arce", null));

        assertThat(repository.getByRole(Role.PROFESSOR)).extracting(User::getEmailAddress)
                .containsExactly("beto@uagrm.bo", "ana@uagrm.bo", "luis@uagrm.bo", "carla@uagrm.bo");
    }

    @Test
    void getByRoleWithoutAccountsIsEmpty() {
        repository.insert(named("dir@uagrm.bo", Role.DIRECTOR, "Diego", "Arce", null));

        assertThat(repository.getByRole(Role.CAPSTONE_PROJECT_PROFESSOR)).isEmpty();
    }

    @Test
    void getByEmailIgnoresCase() {
        repository.insert(account("ana@uagrm.bo", "218001234"));

        assertThat(repository.getByEmail(" Ana@UAGRM.bo ")).isPresent();
        assertThat(repository.getByEmail("otra@uagrm.bo")).isEmpty();
    }

    @Test
    void repeatedEmailAndCodeAreRejectedWithOneErrorEach() {
        repository.insert(account("ana@uagrm.bo", "218001234"));

        assertThatThrownBy(() -> repository.insert(account("ANA@uagrm.bo", "218001234")))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsOnlyKeys(User.EMAIL, User.CODE)
                        .containsEntry(User.EMAIL, "Ya pertenece a otra cuenta."));
        assertThat(springData.count()).isEqualTo(1);
    }

    @Test
    void repeatedCodeAloneIsRejected() {
        repository.insert(account("ana@uagrm.bo", "218001234"));

        assertThatThrownBy(() -> repository.insert(account("luis@uagrm.bo", "218001234")))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrors()).containsOnlyKeys(User.CODE));
    }

    @Test
    void severalAccountsWithoutCodeAreAllowed() {
        repository.insert(account("luis@uagrm.bo", null).withRole(Role.PROFESSOR));
        repository.insert(account("carla@uagrm.bo", null).withRole(Role.PROFESSOR));

        assertThat(springData.count()).isEqualTo(2);
    }

    @Test
    void updateKeepsItsOwnEmailAndCodeButRejectsAnotherAccounts() {
        User ana = account("ana@uagrm.bo", "218001234");
        repository.insert(ana);
        repository.insert(account("luis@uagrm.bo", "218009999"));

        repository.update(ana.withPhoneNumber("77777777"));
        assertThat(repository.getById(ana.getId()).orElseThrow().getPhoneNumber()).isEqualTo("77777777");

        assertThatThrownBy(() -> repository.update(ana.withEmailAddress("luis@uagrm.bo")))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrors()).containsOnlyKeys(User.EMAIL));
    }

    @Test
    void updateStoresTheStatusAndReadsReturnIt() {
        User ana = account("ana@uagrm.bo", "218001234");
        repository.insert(ana);
        assertThat(repository.getById(ana.getId()).orElseThrow().isActive()).isTrue();

        repository.update(ana.withActive(false));

        assertThat(repository.getByEmail("ana@uagrm.bo").orElseThrow().isActive()).isFalse();
        assertThat(repository.getByRole(Role.STUDENT)).singleElement()
                .satisfies(read -> assertThat(read.isActive()).isFalse());
    }

    @Test
    void rowWithoutStatusIsActive() {
        jdbc.update("INSERT INTO user_account (id, email_address, role, first_name, last_name) "
                + "VALUES (gen_random_uuid(), 'old@uagrm.bo', 'PROFESSOR', 'Luis', 'Rojas')");

        assertThat(repository.getByEmail("old@uagrm.bo").orElseThrow().isActive()).isTrue();
    }
}
