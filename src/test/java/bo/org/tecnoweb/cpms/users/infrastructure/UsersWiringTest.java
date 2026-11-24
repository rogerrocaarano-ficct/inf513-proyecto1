package bo.org.tecnoweb.cpms.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.application.commandhelp.ShowHelpQueryHandler;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * The application starts with the users subsystem wired: its handler joins the mail channel and the
 * configured administrator exists. Without the {@code local} profile no test account is created.
 */
@SpringBootTest(properties = "cpms.users.admin-email=admin@cpms.test")
@Import(TestcontainersConfiguration.class)
class UsersWiringTest {

    @Autowired
    private List<EmailCommandHandler> handlers;

    @Autowired
    private UserRepository users;

    @Autowired
    private ApplicationContext context;

    @Test
    void handlersAreRegisteredInTheMailChannel() {
        assertThat(handlers).extracting(EmailCommandHandler::actionKey)
                .contains("usuarios.crearCuenta", "usuarios.listar", "usuarios.desactivar", "usuarios.activar",
                        "usuarios.cambiarRol", "ayuda");
    }

    @Test
    void configuredAdministratorExistsAfterStartup() {
        assertThat(users.getByEmail("admin@cpms.test")).get()
                .satisfies(admin -> assertThat(admin.getRole()).isEqualTo(Role.SYSTEM_ADMIN));
    }

    @Test
    void noTestAccountIsCreatedWithoutTheLocalProfile() {
        assertThat(context.getBeansOfType(LocalTestAccounts.class)).isEmpty();
        assertThat(users.getByEmail("director@test.local")).isEmpty();
    }

    /**
     * Every command except the help command describes itself for the help reply (FR-066), with its own
     * action and as many arguments in its syntax as it requires.
     */
    @Test
    void everyCommandButHelpIsDocumented() {
        assertThat(handlers).filteredOn(handler -> !(handler instanceof ShowHelpQueryHandler)).isNotEmpty()
                .allSatisfy(handler -> {
                    assertThat(handler).isInstanceOf(DocumentedCommand.class);
                    var guide = ((DocumentedCommand) handler).guide();
                    assertThat(guide.action()).isEqualTo(handler.actionKey());
                    assertThat(argumentCount(guide.syntax())).as(guide.syntax()).isEqualTo(handler.requiredArgs());
                });
        assertThat(handlers).filteredOn(ShowHelpQueryHandler.class::isInstance)
                .singleElement().isNotInstanceOf(DocumentedCommand.class);
    }

    private static int argumentCount(String syntax) {
        String inside = syntax.substring(syntax.indexOf('[') + 1, syntax.lastIndexOf(']'));
        return inside.isBlank() ? 0 : inside.split(";", -1).length;
    }
}
