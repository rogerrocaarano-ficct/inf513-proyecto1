package bo.org.tecnoweb.cpms.procedures.application;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.users.application.UserManagementNotifier;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Exercises UC-002 decisions using doubles of the outgoing ports. */
class DegreeProcedureUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final DegreeProcedureRepository procedures = mock(DegreeProcedureRepository.class);
    private final UserManagementNotifier userNotifier = mock(UserManagementNotifier.class);
    private final DegreeProcedureNotifier notifier = mock(DegreeProcedureNotifier.class);
    private final DegreeProcedureUseCase useCase = new DegreeProcedureUseCase(users, procedures, userNotifier, notifier);
    private final String requester = "secretaria@cpms.test";
    private final OpenDegreeProcedureCommand command = new OpenDegreeProcedureCommand(
            "Ana", "Pérez", "", "1234", "123456", "ana@cpms.test", "7123456", documents());

    /** Reuses an existing account without another sign-up notice (FR-056). */
    @Test
    void linksExistingStudent() {
        allowOpening();
        var student = student();
        when(users.getByEmailForUpdate(command.email())).thenReturn(Optional.of(student));
        useCase.open(requester, command);

        verify(procedures).insert(argThat(procedure -> procedure.getStudentId().equals(student.getId())));
        verify(users, never()).insert(any());
        verifyNoInteractions(userNotifier);
        verify(notifier).notifyOpened(any(), eq("TR-001"));
    }

    /** A1 creates the account and uses the existing sign-up notice (FR-035, FR-051). */
    @Test
    void createsAnAccountAndNotifiesStudent() {
        allowOpening();
        useCase.open(requester, command);

        var order = inOrder(users, procedures);
        order.verify(users).insert(argThat(student -> student.getEmailAddress().equals(command.email())));
        order.verify(procedures).insert(any());
        verify(userNotifier).notifyNewAccount(argThat(event -> event.signedUpUser().equals(command.email())));
    }

    /** FR-033 duplicate codes become a generic account validation error. */
    @Test
    void rejectsDuplicateCodeWithoutOpening() {
        allowOpening();
        doThrow(new DomainValidationException(Map.of("código", "Duplicado."))).when(users).insert(any());

        assertThatThrownBy(() -> useCase.open(requester, command))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        error -> assertThat(error.getErrors().get("cuenta")).contains("Verifique el correo y el registro"));
        verify(procedures, never()).insert(any());
        verifyNoInteractions(userNotifier, notifier);
    }

    /** Permissions prevent other roles from opening and replacing. */
    @Test
    void deniesADirector() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary().withRole(Role.DIRECTOR)));
        assertThatThrownBy(() -> useCase.open(requester, command)).isInstanceOf(UserNotAuthorizedException.class);
        assertThatThrownBy(() -> useCase.replace(requester, new ReplaceInitialDocumentCommand("TR-001", pdf("ci.pdf"))))
                .isInstanceOf(UserNotAuthorizedException.class);
        verifyNoInteractions(procedures, notifier);
    }

    private void allowOpening() {
        when(users.getByEmail(requester)).thenReturn(Optional.of(secretary()));
        when(procedures.insert(any())).thenAnswer(call -> {
            DegreeProcedure procedure = call.getArgument(0);
            return DegreeProcedure.rehydrate(procedure.getId(), 1, procedure.getStudentId(), procedure.getOpenedBy(),
                    procedure.getOpenedAt(), null, procedure.getDocuments());
        });
    }
}
