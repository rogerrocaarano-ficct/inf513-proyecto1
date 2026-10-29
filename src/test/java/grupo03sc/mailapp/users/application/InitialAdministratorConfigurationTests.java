package grupo03sc.mailapp.users.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import grupo03sc.mailapp.audit.application.ChangeLog;
import grupo03sc.mailapp.users.domain.AccountStatus;
import grupo03sc.mailapp.users.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class InitialAdministratorConfigurationTests {

    private final UserAccountRepository accounts = mock(UserAccountRepository.class);
    private final ChangeLog changeLog = mock(ChangeLog.class);

    @Test
    void failsToStartWhenNoAdministratorExistsAndTheConfigurationIsIncomplete() {
        when(accounts.existsWithRoleAndStatus(Role.ADMINISTRATOR, AccountStatus.ACTIVE)).thenReturn(false);
        var creator = new InitialAdministratorCreator(accounts, changeLog,
                new InitialAdministratorProperties("Admin", " "));

        assertThatThrownBy(() -> creator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INITIAL_ADMIN_EMAIL")
                .hasMessageNotContaining("INITIAL_ADMIN_NAME");
        verify(accounts, never()).saveAndFlush(any());
    }
}
