package bo.org.tecnoweb.cpms.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserTest {

    private static User account(Role role) {
        return User.rehydrate().withId(UUID.randomUUID()).withEmailAddress(role.getLabel() + "@uagrm.bo")
                .withFirstName("Ana").withLastName("Rojas").withRole(role);
    }

    private static final User ADMIN = account(Role.SYSTEM_ADMIN);

    /** A valid new student; tests change one field at a time. */
    private static User newStudent() {
        return User.builder()
                .withFirstName("Ana María").withLastName("Pérez").withSecondLastName("Rojas")
                .withCi("1234567").withUagrmCode("218001234").withEmailAddress("ana@uagrm.bo")
                .withPhoneNumber("71234567").withRole(Role.STUDENT);
    }

    private static DomainValidationException validationError(User user, User requester) {
        try {
            user.signUp(requester);
        } catch (DomainValidationException e) {
            return e;
        }
        throw new AssertionError("Expected a DomainValidationException");
    }

    @Nested
    class Building {

        @Test
        void builderGivesANewIdentityAndNoEvents() {
            User user = User.builder();

            assertThat(user.getId()).isNotNull();
            assertThat(user.pullEvents()).isEmpty();
        }

        @Test
        void rehydratedAccountKeepsItsDataAndRaisesNoEvents() {
            UUID id = UUID.randomUUID();
            User user = User.rehydrate().withId(id).withEmailAddress("luis@uagrm.bo").withRole(Role.PROFESSOR)
                    .withFirstName("Luis").withLastName("Vaca");

            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getRole()).isEqualTo(Role.PROFESSOR);
            assertThat(user.getFullName()).isEqualTo("Luis Vaca");
            assertThat(user.pullEvents()).isEmpty();
        }

        @Test
        void emailIsStoredInLowerCase() {
            assertThat(User.builder().withEmailAddress("  Ana@UAGRM.bo ").getEmailAddress()).isEqualTo("ana@uagrm.bo");
        }

        @Test
        void permissionsComeFromTheRole() {
            assertThat(account(Role.ADMINISTRATIVE).hasPermissionTo(RolePermission.SIGNUP_STUDENT)).isTrue();
            assertThat(account(Role.ADMINISTRATIVE).hasPermissionTo(RolePermission.SIGNUP_PROFESSOR)).isFalse();
        }
    }

    @Nested
    class SignUp {

        @Test
        void validAccountRaisesOneSignedUpEvent() {
            User student = newStudent().signUp(ADMIN);

            assertThat(student.pullEvents()).singleElement().isInstanceOfSatisfying(UserSignedUpEvent.class, e -> {
                assertThat(e.signedUpUser()).isEqualTo("ana@uagrm.bo");
                assertThat(e.signedUpUserFullName()).isEqualTo("Ana María Pérez Rojas");
                assertThat(e.signedUpUserRole()).isEqualTo("estudiante");
                assertThat(e.requesterUserEmail()).isEqualTo("administrador@uagrm.bo");
                assertThat(e.requesterUserFullName()).isEqualTo("Ana Rojas");
                assertThat(e.occurredOn()).isNotNull();
            });
            assertThat(student.pullEvents()).isEmpty();
        }

        @Test
        void requesterWithoutPermissionIsRejectedBeforeAnyDataError() {
            User invalidDirector = User.builder().withRole(Role.DIRECTOR).withFirstName("").withEmailAddress("x");

            assertThatThrownBy(() -> invalidDirector.signUp(account(Role.DIRECTOR)))
                    .isInstanceOf(UserNotAuthorizedException.class)
                    .hasMessageContaining("crear cuentas de director");
            assertThat(invalidDirector.pullEvents()).isEmpty();
        }

        @Test
        void missingRoleIsAValidationError() {
            assertThat(validationError(newStudent().withRole(null), ADMIN).getErrors()).containsOnlyKeys(Role.FIELD);
        }

        @Test
        void namesWithAccentsAndEnyeAreAccepted() {
            User user = newStudent().withFirstName("José Ñandú").withLastName("Güemes").signUp(ADMIN);

            assertThat(user.getFullName()).isEqualTo("José Ñandú Güemes Rojas");
        }

        @Test
        void namesAreNormalized() {
            User user = newStudent().withFirstName("  maría   de los   ÁNGELES ").withLastName("PÉREZ")
                    .withSecondLastName("   ").signUp(ADMIN);

            assertThat(user.getFirstName()).isEqualTo("María De Los Ángeles");
            assertThat(user.getLastName()).isEqualTo("Pérez");
            assertThat(user.getSecondLastName()).isNull();
        }

        @Test
        void everyInvalidFieldIsReportedTogether() {
            User user = newStudent().withLastName("").withPhoneNumber("7123-4567");

            assertThat(validationError(user, ADMIN).getErrors())
                    .containsOnlyKeys(User.LAST_NAME, User.PHONE)
                    .containsEntry(User.LAST_NAME, "Es obligatorio.")
                    .containsEntry(User.PHONE, "Debe tener solo dígitos, hasta 8.");
        }

        @Test
        void namesWithDigitsOrSymbolsAreRejected() {
            User user = newStudent().withFirstName("Ana2").withSecondLastName("Rojas-Vaca");

            assertThat(validationError(user, ADMIN).getErrors())
                    .containsOnlyKeys(User.FIRST_NAME, User.SECOND_LAST_NAME);
        }

        @Test
        void invalidEmailIsRejected() {
            assertThat(validationError(newStudent().withEmailAddress("ana.uagrm.bo"), ADMIN).getErrors())
                    .containsOnly(org.assertj.core.api.Assertions.entry(User.EMAIL, "No es una dirección de correo válida."));
        }

        @Test
        void ciWithComplementIsRejected() {
            assertThat(validationError(newStudent().withCi("1234567-1A"), ADMIN).getErrors())
                    .containsOnly(org.assertj.core.api.Assertions.entry(User.CI, "Debe tener solo dígitos, hasta 7."));
        }

        @Test
        void tooLongNumbersAreRejected() {
            User user = newStudent().withCi("12345678").withUagrmCode("12345678901").withPhoneNumber("712345678");

            assertThat(validationError(user, ADMIN).getErrors()).containsOnlyKeys(User.CI, User.CODE, User.PHONE);
        }

        @Test
        void studentNeedsCiCodeAndPhone() {
            User user = newStudent().withCi("").withUagrmCode(null).withPhoneNumber(" ");

            assertThat(validationError(user, ADMIN).getErrors())
                    .containsOnlyKeys(User.CI, User.CODE, User.PHONE)
                    .containsEntry(User.CI, "Es obligatorio para estudiantes.");
        }

        @Test
        void professorMayLeaveCiCodeAndPhoneEmpty() {
            User professor = newStudent().withRole(Role.PROFESSOR).withCi("").withUagrmCode("").withPhoneNumber("")
                    .signUp(account(Role.DIRECTOR));

            assertThat(professor.getCi()).isNull();
            assertThat(professor.getUagrmCode()).isNull();
            assertThat(professor.getPhoneNumber()).isNull();
        }

        @Test
        void professorWithAnInvalidOptionalFieldIsRejected() {
            User professor = newStudent().withRole(Role.PROFESSOR).withPhoneNumber("abc");

            assertThat(validationError(professor, account(Role.DIRECTOR)).getErrors()).containsOnlyKeys(User.PHONE);
        }
    }
}
