package grupo03sc.mailapp.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.graduationprocesses.application.GraduationParticipation;
import grupo03sc.mailapp.graduationprocesses.application.GraduationProcessSummary;
import grupo03sc.mailapp.graduationprocesses.application.ParticipantKind;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * FR-034 with a double of GraduationParticipation, since graduation processes do not exist yet (D6).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AffectedProcessesTests {

    @MockitoBean
    private GraduationParticipation participation;

    @Autowired
    private UserManagementService service;

    @Autowired
    private UserAccountRepository accounts;

    private final GraduationProcessSummary process = new GraduationProcessSummary(UUID.randomUUID(), "Juan Soto");

    private UUID admin() {
        return accounts.findByEmail("admin@mailapp.test").orElseThrow().getId();
    }

    private UserAccount professor() {
        return service.createAccount(admin(), UserAccountData.of("Tutor", "tutor-" + UUID.randomUUID() + "@uagrm.edu.bo",
                Set.of(Role.PROFESSOR)));
    }

    @Test
    void leavesTheProcessesOfADeactivatedAccountPendingAndReportsThem() {
        UserAccount tutor = professor();
        when(participation.markPendingReassignment(eq(tutor.getId()), any())).thenReturn(List.of(process));

        AccountChangeResult result = service.deactivateAccount(admin(), tutor.getEmail());

        assertThat(result.affectedProcesses()).containsExactly(process);
        verify(participation).markPendingReassignment(tutor.getId(), EnumSet.allOf(ParticipantKind.class));
    }

    @Test
    void leavesPendingOnlyTheParticipationOfTheRemovedRoles() {
        UserAccount tutor = professor();
        when(participation.markPendingReassignment(eq(tutor.getId()), any())).thenReturn(List.of(process));

        AccountChangeResult result = service.updateRoles(admin(), tutor.getEmail(), Set.of(Role.PROGRAM_SECRETARY));

        assertThat(result.affectedProcesses()).containsExactly(process);
        verify(participation).markPendingReassignment(tutor.getId(), EnumSet.of(ParticipantKind.TUTOR, ParticipantKind.TRIBUNAL_MEMBER));
    }

    @Test
    void doesNotLookForProcessesWhenNoParticipatingRoleIsRemoved() {
        UserAccount tutor = professor();

        service.updateRoles(admin(), tutor.getEmail(), Set.of(Role.PROFESSOR, Role.PROGRAM_DIRECTOR));

        verify(participation, never()).markPendingReassignment(any(), any());
    }
}
