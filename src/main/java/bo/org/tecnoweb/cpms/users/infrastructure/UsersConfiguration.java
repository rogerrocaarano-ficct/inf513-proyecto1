package bo.org.tecnoweb.cpms.users.infrastructure;

import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.application.commandhelp.CommandHelpNotifier;
import bo.org.tecnoweb.cpms.users.application.commandhelp.CommandHelpUseCase;
import bo.org.tecnoweb.cpms.users.application.commandhelp.ShowHelpQueryHandler;
import bo.org.tecnoweb.cpms.users.application.usermanagement.ChangeUserRoleCommandHandler;
import bo.org.tecnoweb.cpms.users.application.usermanagement.DisableUserCommandHandler;
import bo.org.tecnoweb.cpms.users.application.usermanagement.EnableUserCommandHandler;
import bo.org.tecnoweb.cpms.users.application.usermanagement.ListUsersQueryHandler;
import bo.org.tecnoweb.cpms.users.application.usermanagement.SignUpUserCommandHandler;
import bo.org.tecnoweb.cpms.users.application.usermanagement.UserManagementNotifier;
import bo.org.tecnoweb.cpms.users.application.usermanagement.UserManagementUseCase;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring of the users subsystem. The command handlers declared here join the mail channel
 * automatically.
 */
@Configuration(proxyBeanMethods = false)
class UsersConfiguration {

    /**
     * Creates the use case UC-001.
     *
     * @param users    JPA adapter of the accounts
     * @param notifier email implementation of the notifier
     * @return the use case
     */
    @Bean
    UserManagementUseCase userManagementUseCase(UserRepository users, UserManagementNotifier notifier) {
        return new UserManagementUseCase(users, notifier);
    }

    /**
     * Creates the handler of {@code usuarios.crearCuenta}.
     *
     * @param useCase the use case it runs
     * @return the handler
     */
    @Bean
    SignUpUserCommandHandler signUpUserCommandHandler(UserManagementUseCase useCase) {
        return new SignUpUserCommandHandler(useCase);
    }

    /**
     * Creates the handler of {@code usuarios.listar}.
     *
     * @param useCase the use case it runs
     * @return the handler
     */
    @Bean
    ListUsersQueryHandler listUsersQueryHandler(UserManagementUseCase useCase) {
        return new ListUsersQueryHandler(useCase);
    }

    /**
     * Creates the handler of {@code usuarios.desactivar}.
     *
     * @param useCase the use case it runs
     * @return the handler
     */
    @Bean
    DisableUserCommandHandler disableUserCommandHandler(UserManagementUseCase useCase) {
        return new DisableUserCommandHandler(useCase);
    }

    /**
     * Creates the handler of {@code usuarios.activar}.
     *
     * @param useCase the use case it runs
     * @return the handler
     */
    @Bean
    EnableUserCommandHandler enableUserCommandHandler(UserManagementUseCase useCase) {
        return new EnableUserCommandHandler(useCase);
    }

    /**
     * Creates the handler of {@code usuarios.cambiarRol} (UC-001 A1, FR-031).
     *
     * @param useCase the use case it runs
     * @return the handler
     */
    @Bean
    ChangeUserRoleCommandHandler changeUserRoleCommandHandler(UserManagementUseCase useCase) {
        return new ChangeUserRoleCommandHandler(useCase);
    }

    /**
     * Creates the help command (FR-066).
     *
     * @param users    JPA adapter of the accounts
     * @param commands every command handler that describes its command, from every subsystem
     * @param notifier email implementation of the notifier
     * @return the use case
     */
    @Bean
    CommandHelpUseCase commandHelpUseCase(UserRepository users, List<DocumentedCommand> commands,
            CommandHelpNotifier notifier) {
        return new CommandHelpUseCase(users, commands, notifier);
    }

    /**
     * Creates the handler of {@code ayuda}.
     *
     * @param useCase the use case it runs
     * @return the handler
     */
    @Bean
    ShowHelpQueryHandler showHelpQueryHandler(CommandHelpUseCase useCase) {
        return new ShowHelpQueryHandler(useCase);
    }
}
