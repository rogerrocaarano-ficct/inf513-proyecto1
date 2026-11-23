package bo.org.tecnoweb.cpms.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Test accounts of the local development environment and of the deployed test environment (requirement
 * "Cuentas de prueba del entorno local"; capability {@code test-environment}).
 */
class LocalTestAccountsTest {

    private final InitialAdminAccountTest.Users users = new InitialAdminAccountTest.Users();

    @Test
    void firstStartCreatesOneAccountPerRoleAndFiveProfessorsAndThreeStudents() {
        new LocalTestAccounts(users).run(null);

        assertThat(users.inserted).hasSize(11);
        Map<Role, Long> perRole = users.inserted.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
        assertThat(perRole).containsExactlyInAnyOrderEntriesOf(Map.of(
                Role.ADMINISTRATIVE, 1L,
                Role.CAPSTONE_PROJECT_PROFESSOR, 1L,
                Role.DIRECTOR, 1L,
                Role.PROFESSOR, 5L,
                Role.STUDENT, 3L));
        assertThat(users.inserted).allSatisfy(account -> {
            assertThat(account.getEmailAddress()).endsWith("@test.local");
            assertThat(account.isActive()).isTrue();
            assertThat(account.pullEvents()).isEmpty();
        });
    }

    @Test
    void studentsHaveTheDataRequiredForTheirRole() {
        new LocalTestAccounts(users).run(null);

        assertThat(users.inserted).filteredOn(account -> account.getRole() == Role.STUDENT)
                .hasSize(3)
                .allSatisfy(student -> {
                    assertThat(student.getCi()).isNotBlank();
                    assertThat(student.getUagrmCode()).isNotBlank();
                    assertThat(student.getPhoneNumber()).isNotBlank();
                });
    }

    @Test
    void laterStartsCreateNothingAndKeepADisabledAccountDisabled() {
        LocalTestAccounts runner = new LocalTestAccounts(users);
        runner.run(null);
        User professor = users.getByEmail("docente1@test.local").orElseThrow();
        professor.withActive(false);

        runner.run(null);

        assertThat(users.inserted).hasSize(11);
        assertThat(users.updated).isZero();
        assertThat(users.getByEmail("docente1@test.local")).get().satisfies(account -> {
            assertThat(account).isSameAs(professor);
            assertThat(account.isActive()).isFalse();
        });
    }

    @Test
    void anAddressThatAlreadyHasAnAccountIsSkipped() {
        User existing = User.builder()
                .withEmailAddress("director@test.local")
                .withRole(Role.PROFESSOR)
                .withFirstName("Otro")
                .withLastName("Nombre");
        users.insert(existing);

        new LocalTestAccounts(users).run(null);

        Map<String, User> byEmail = users.inserted.stream()
                .collect(Collectors.toMap(User::getEmailAddress, Function.identity()));
        assertThat(byEmail).hasSize(11);
        assertThat(byEmail.get("director@test.local")).isSameAs(existing);
        assertThat(existing.getRole()).isEqualTo(Role.PROFESSOR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"local", "staging"})
    void runsInTheLocalAndTheTestEnvironmentProfiles(String profile) {
        contextWithProfiles(profile).run(context -> assertThat(context).hasSingleBean(LocalTestAccounts.class));
    }

    @Test
    void doesNotRunWithoutThoseProfiles() {
        contextWithProfiles().run(context -> assertThat(context).doesNotHaveBean(LocalTestAccounts.class));
    }

    private ApplicationContextRunner contextWithProfiles(String... profiles) {
        return new ApplicationContextRunner()
                .withInitializer(context -> context.getEnvironment().setActiveProfiles(profiles))
                .withBean(UserRepository.class, () -> users)
                .withUserConfiguration(LocalTestAccounts.class);
    }
}
