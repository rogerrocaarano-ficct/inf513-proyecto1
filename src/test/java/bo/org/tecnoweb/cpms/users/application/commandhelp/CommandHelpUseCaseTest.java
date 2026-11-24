package bo.org.tecnoweb.cpms.users.application.commandhelp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.application.usermanagement.ChangeUserRoleCommandHandler;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Help command (FR-066): which commands each role sees and how. */
class CommandHelpUseCaseTest {

    private final InMemoryUsers users = new InMemoryUsers();
    private final List<ShowHelpQueryResult> sent = new ArrayList<>();
    private final CommandHelpUseCase useCase = new CommandHelpUseCase(users, List.of(
            command("usuarios.crearCuenta", "usuarios.crearCuenta[rol]", "",
                    CommandGuide.forEveryRole(RolePermission::toSignUp)),
            command("usuarios.listar", "usuarios.listar[rol]", "", CommandGuide.forEveryRole(RolePermission::toList)),
            command("usuarios.desactivar", "usuarios.desactivar[correo]", "",
                    CommandGuide.forEveryRole(RolePermission::toChangeStatus)),
            command("tramites.abrirTramite", "tramites.abrirTramite[registro]", "Adjuntos: 4 PDF.",
                    Set.of(RolePermission.OPEN_DEGREE_PROCEDURE)),
            command("proyectos.registrarPerfil", "proyectos.registrarPerfil[titulo]", "Adjuntos: 1 PDF.",
                    Set.of(RolePermission.REGISTER_PROJECT_PROFILE)),
            command("proyectos.aprobarPerfil", "proyectos.aprobarPerfil[referencia]", "",
                    Set.of(RolePermission.EVALUATE_PROJECT_PROFILE)),
            command("proyectos.asignarTutor", "proyectos.asignarTutor[referencia; correoDocente]", "",
                    Set.of(RolePermission.ASSIGN_TUTOR))),
            sent::add);

    private static DocumentedCommand command(String action, String syntax, String notes,
            Set<RolePermission> permissions) {
        CommandGuide guide = new CommandGuide(action, syntax, "Para " + action + ".", notes, permissions);
        return () -> guide;
    }

    private void existing(String email, Role role, boolean active) {
        users.saved.add(User.rehydrate().withId(UUID.randomUUID()).withEmailAddress(email).withRole(role)
                .withFirstName("Carla").withLastName("Suárez").withActive(active));
    }

    private ShowHelpQueryResult helpFor(Role role) {
        existing("cuenta@uagrm.bo", role, true);
        useCase.showHelp("Cuenta@UAGRM.bo");
        assertThat(sent).hasSize(1);
        return sent.getFirst();
    }

    private static List<String> syntaxes(ShowHelpQueryResult result) {
        return result.commands().stream().map(ShowHelpQueryItem::syntax).toList();
    }

    @Test
    void studentSeesOnlyItsCommandAndHelp() {
        ShowHelpQueryResult result = helpFor(Role.STUDENT);

        assertThat(result.requesterEmail()).isEqualTo("cuenta@uagrm.bo");
        assertThat(syntaxes(result)).containsExactly("ayuda[]", "proyectos.registrarPerfil[titulo]");
        assertThat(result.commands().get(1)).satisfies(item -> {
            assertThat(item.purpose()).isEqualTo("Para proyectos.registrarPerfil.");
            assertThat(item.notes()).isEqualTo("Adjuntos: 1 PDF.");
            assertThat(item.allowedActions()).isEmpty();
        });
        assertThat(result.onlyHelp()).isFalse();
    }

    @Test
    void directorSeesItsCommandsOrderedByName() {
        ShowHelpQueryResult result = helpFor(Role.DIRECTOR);

        assertThat(syntaxes(result)).containsExactly("ayuda[]", "proyectos.asignarTutor[referencia; correoDocente]",
                "usuarios.crearCuenta[rol]", "usuarios.desactivar[correo]", "usuarios.listar[rol]");
    }

    @Test
    void administrativeSeesWhatItMayDoWithPartialCommands() {
        ShowHelpQueryResult result = helpFor(Role.ADMINISTRATIVE);

        assertThat(syntaxes(result)).containsExactly("ayuda[]", "tramites.abrirTramite[registro]",
                "usuarios.crearCuenta[rol]", "usuarios.listar[rol]");
        assertThat(result.commands().get(1).allowedActions()).isEmpty();
        assertThat(result.commands().get(2).allowedActions()).containsExactly("crear cuentas de estudiante");
        assertThat(result.commands().get(3).allowedActions()).containsExactly("listar cuentas de estudiante");
    }

    @Test
    void administratorWithEveryPermissionOfACommandGetsNoClarification() {
        ShowHelpQueryResult result = helpFor(Role.SYSTEM_ADMIN);

        assertThat(syntaxes(result)).containsExactly("ayuda[]", "usuarios.crearCuenta[rol]",
                "usuarios.desactivar[correo]", "usuarios.listar[rol]");
        assertThat(result.commands()).allSatisfy(item -> assertThat(item.allowedActions()).isEmpty());
    }

    @Test
    void directorGetsTheLabelsOfThePartialPermissions() {
        ShowHelpQueryResult result = helpFor(Role.DIRECTOR);

        assertThat(result.commands().get(2).allowedActions()).containsExactly("crear cuentas de estudiante",
                "crear cuentas de administrativo", "crear cuentas de docente",
                "crear cuentas de docente de Taller de Grado");
    }

    /** The real guide of {@code usuarios.cambiarRol} (UC-001 A1): who sees it and with which actions. */
    @Test
    void changeRoleCommandIsShownOnlyToWhoMayUseIt() {
        List<ShowHelpQueryResult> replies = new ArrayList<>();
        CommandHelpUseCase help = new CommandHelpUseCase(users, List.of(() -> ChangeUserRoleCommandHandler.GUIDE),
                replies::add);
        existing("admin@uagrm.bo", Role.SYSTEM_ADMIN, true);
        existing("director@uagrm.bo", Role.DIRECTOR, true);
        existing("secretaria@uagrm.bo", Role.ADMINISTRATIVE, true);

        help.showHelp("admin@uagrm.bo");
        help.showHelp("director@uagrm.bo");
        help.showHelp("secretaria@uagrm.bo");

        assertThat(syntaxes(replies.get(0))).containsExactly("ayuda[]", "usuarios.cambiarRol[correo; rol]");
        assertThat(replies.get(0).commands().get(1).allowedActions()).isEmpty();
        assertThat(replies.get(1).commands().get(1).allowedActions()).containsExactly(
                "cambiar el rol de cuentas de estudiante", "cambiar el rol de cuentas de administrativo",
                "cambiar el rol de cuentas de docente", "cambiar el rol de cuentas de docente de Taller de Grado");
        assertThat(syntaxes(replies.get(2))).containsExactly("ayuda[]");
    }

    @Test
    void professorOnlyHasHelp() {
        ShowHelpQueryResult result = helpFor(Role.PROFESSOR);

        assertThat(syntaxes(result)).containsExactly("ayuda[]");
        assertThat(result.commands().getFirst().purpose()).isEqualTo("Ver los comandos que puede usar.");
        assertThat(result.onlyHelp()).isTrue();
    }

    @Test
    void senderWithoutAccountIsNotRegistered() {
        assertThatThrownBy(() -> useCase.showHelp("extrano@gmail.com"))
                .isInstanceOf(UserNotAuthorizedException.class)
                .hasMessageContaining("no está registrada");
        assertThat(sent).isEmpty();
    }

    @Test
    void disabledAccountIsRejected() {
        existing("luis@uagrm.bo", Role.STUDENT, false);

        assertThatThrownBy(() -> useCase.showHelp("luis@uagrm.bo"))
                .isInstanceOf(UserNotAuthorizedException.class)
                .hasMessageContaining("desactivada");
        assertThat(sent).isEmpty();
    }

    @Test
    void helpChangesNoData() {
        existing("cuenta@uagrm.bo", Role.STUDENT, true);

        useCase.showHelp("cuenta@uagrm.bo");
        useCase.showHelp("cuenta@uagrm.bo");

        assertThat(sent).hasSize(2);
        assertThat(sent.get(0)).isEqualTo(sent.get(1));
        assertThat(users.writes).isZero();
    }

    /** Accounts kept in memory; only reading by email is expected. */
    static class InMemoryUsers implements UserRepository {
        final List<User> saved = new ArrayList<>();
        int writes;

        @Override
        public void insert(User user) {
            writes++;
        }

        @Override
        public void update(User user) {
            writes++;
        }

        @Override
        public Optional<User> getByEmail(String email) {
            return saved.stream().filter(u -> u.getEmailAddress().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public Optional<User> getByUagrmCodeForUpdate(String code) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<User> getById(UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<User> getByRole(Role role) {
            throw new UnsupportedOperationException();
        }
    }
}
