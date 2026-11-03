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
 * FR-034 with a double of GraduationParticipation, so that a professor can take part in processes
 * before UC-005 designates them.
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

    private final UUID processId = UUID.randomUUID();

    private UUID admin() {
        return accounts.findByEmail("admin@mailapp.test").orElseThrow().getId();
    }

    private UserAccount professor() {
        return service.createAccount(admin(), UserAccountData.of("Tutor", "tutor-" + UUID.randomUUID() + "@uagrm.edu.bo",
                Set.of(Role.PROFESSOR)));
    }

    /** A process of a student account named Juan Soto, as GraduationProcesses reports it (D7). */
    private GraduationProcessSummary process() {
        UserAccount student = service.createAccount(admin(), new UserAccountData("Juan Soto",
                "juan-" + UUID.randomUUID() + "@uagrm.edu.bo", Set.of(Role.STUDENT),
                String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000L)), "1", "2"));
        return new GraduationProcessSummary(processId, student.getId());
    }

    @Test
    void leavesTheProcessesOfADeactivatedAccountPendingAndReportsThem() {
        UserAccount tutor = professor();
        when(participation.markPendingReassignment(eq(tutor.getId()), any())).thenReturn(List.of(process()));

        AccountChangeResult result = service.deactivateAccount(admin(), tutor.getEmail());

        assertThat(result.affectedProcesses()).containsExactly(new AffectedProcess(processId, "Juan Soto"));
        verify(participation).markPendingReassignment(tutor.getId(), EnumSet.allOf(ParticipantKind.class));
    }

    @Test
    void leavesPendingOnlyTheParticipationOfTheRemovedRoles() {
        UserAccount tutor = professor();
        when(participation.markPendingReassignment(eq(tutor.getId()), any())).thenReturn(List.of(process()));

        AccountChangeResult result = service.updateRoles(admin(), tutor.getEmail(), Set.of(Role.PROGRAM_SECRETARY));

        assertThat(result.affectedProcesses()).containsExactly(new AffectedProcess(processId, "Juan Soto"));
        verify(participation).markPendingReassignment(tutor.getId(), EnumSet.of(ParticipantKind.TUTOR, ParticipantKind.TRIBUNAL_MEMBER));
    }

    @Test
    void doesNotLookForProcessesWhenNoParticipatingRoleIsRemoved() {
        UserAccount tutor = professor();

        service.updateRoles(admin(), tutor.getEmail(), Set.of(Role.PROFESSOR, Role.PROGRAM_DIRECTOR));

        verify(participation, never()).markPendingReassignment(any(), any());
    }
}
