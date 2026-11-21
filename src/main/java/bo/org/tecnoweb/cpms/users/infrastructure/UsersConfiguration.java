package bo.org.tecnoweb.cpms.users.infrastructure;

import bo.org.tecnoweb.cpms.users.application.ListUsersQueryHandler;
import bo.org.tecnoweb.cpms.users.application.SignUpUserCommandHandler;
import bo.org.tecnoweb.cpms.users.application.UserManagementNotifier;
import bo.org.tecnoweb.cpms.users.application.UserManagementUseCase;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
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
}
