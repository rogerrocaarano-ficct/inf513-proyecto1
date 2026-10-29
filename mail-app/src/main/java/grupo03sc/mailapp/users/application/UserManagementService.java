package grupo03sc.mailapp.users.application;

import grupo03sc.mailapp.audit.application.ChangeLog;
import grupo03sc.mailapp.audit.application.FieldChange;
import grupo03sc.mailapp.graduationprocesses.application.GraduationParticipation;
import grupo03sc.mailapp.graduationprocesses.application.GraduationProcessSummary;
import grupo03sc.mailapp.graduationprocesses.application.ParticipantKind;
import grupo03sc.mailapp.messaging.application.Notifier;
import grupo03sc.mailapp.users.domain.AccountField;
import grupo03sc.mailapp.users.domain.AccountStatus;
import grupo03sc.mailapp.users.domain.RejectionReason;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import grupo03sc.mailapp.users.domain.UserRuleViolationException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages user accounts and their roles (UC-001). Each operation is one transaction: the change,
 * its change log entries and the processes left pending reassignment are kept or undone together,
 * and the notice to the user is sent after commit.
 */
@Service
@Transactional
public class UserManagementService {

    static final String TARGET_TYPE = "UserAccount";

    private final UserAccountRepository accounts;
    private final ChangeLog changeLog;
    private final Notifier notifier;
    private final GraduationParticipation graduationParticipation;

    UserManagementService(UserAccountRepository accounts, ChangeLog changeLog, Notifier notifier,
            GraduationParticipation graduationParticipation) {
        this.accounts = accounts;
        this.changeLog = changeLog;
        this.notifier = notifier;
        this.graduationParticipation = graduationParticipation;
    }

    /** Basic flow and A1: creates an active account, records it and notifies the new user. */
    public UserAccount createAccount(UUID requesterId, UserAccountData data) {
        UserAccount requester = requester(requesterId);
        UserAccount account = UserAccount.create(data);
        checkCanAssign(requester, account.getRoles());
        if (accounts.existsByEmail(account.getEmail())) {
            throw duplicateEmail(account.getEmail());
        }
        String registration = account.getStudentRegistrationNumber();
        if (registration != null && accounts.existsByStudentRegistrationNumber(registration)) {
            throw duplicateRegistration(registration);
        }
        Role.validateCombination(account.getRoles());
        if (account.hasRole(Role.THESIS_WORKSHOP_PROFESSOR)
                && accounts.existsWithRoleAndStatus(Role.THESIS_WORKSHOP_PROFESSOR, AccountStatus.ACTIVE)) {
            throw secondThesisWorkshopProfessor();
        }
        UserAccount saved = save(account);
        changeLog.record(requesterId, TARGET_TYPE, saved.getId(), saved.describe());
        notifier.notify(saved.getEmail(), "Cuenta de usuario creada", """
                Estimado(a) %s:

                Se creó su cuenta de usuario en el sistema de titulación con los roles: %s.
                Puede enviar sus comandos por correo desde esta dirección.
                """.formatted(saved.getFullName(), Role.labels(saved.getRoles())));
        return saved;
    }

    /** A2: the accounts within the scope of the requester, by name. */
    @Transactional(readOnly = true)
    public List<UserAccount> listAccounts(UUID requesterId) {
        UserAccount requester = requester(requesterId);
        return accounts.findAllByOrderByFullNameAsc().stream()
                .filter(account -> Role.canManage(requester.getRoles(), account.getRoles()))
                .toList();
    }

    /** A2: one account within the scope of the requester. */
    @Transactional(readOnly = true)
    public UserAccount findAccount(UUID requesterId, String email) {
        return managedAccount(requester(requesterId), email);
    }

    /** A3: changes one field other than the roles. Changing data sends no notice. */
    public AccountChangeResult updateField(UUID requesterId, String email, AccountField field, String value) {
        if (field == AccountField.ROLES) {
            throw new IllegalArgumentException("Roles are changed with updateRoles");
        }
        UserAccount account = managedAccount(requester(requesterId), email);
        if (field == AccountField.EMAIL && value != null && !value.isBlank()) {
            String newEmail = UserAccount.normalizeEmail(value);
            if (accounts.existsByEmailAndIdNot(newEmail, account.getId())) {
                throw duplicateEmail(newEmail);
            }
        }
        if (field == AccountField.STUDENT_REGISTRATION_NUMBER && value != null && !value.isBlank()
                && accounts.existsByStudentRegistrationNumberAndIdNot(value.strip(), account.getId())) {
            throw duplicateRegistration(value.strip());
        }
        FieldChange change = account.changeField(field, value);
        save(account);
        changeLog.record(requesterId, TARGET_TYPE, account.getId(), List.of(change));
        return new AccountChangeResult(account, List.of());
    }

    /** A3: replaces the roles, leaves pending the processes of the removed roles and notifies the user. */
    public AccountChangeResult updateRoles(UUID requesterId, String email, Set<Role> newRoles) {
        UserAccount requester = requester(requesterId);
        UserAccount account = managedAccount(requester, email);
        checkCanAssign(requester, newRoles);
        Role.validateCombination(newRoles);
        if (account.isActive() && newRoles.contains(Role.THESIS_WORKSHOP_PROFESSOR)
                && accounts.existsOtherActiveWithRole(Role.THESIS_WORKSHOP_PROFESSOR, account.getId())) {
            throw secondThesisWorkshopProfessor();
        }
        if (account.isActive() && account.hasRole(Role.ADMINISTRATOR) && !newRoles.contains(Role.ADMINISTRATOR)) {
            checkNotLastAdministrator(account);
        }
        Set<Role> oldRoles = account.getRoles();
        if (oldRoles.equals(newRoles)) {
            return new AccountChangeResult(account, List.of());
        }
        Set<ParticipantKind> lostKinds = EnumSet.noneOf(ParticipantKind.class);
        lostKinds.addAll(Role.participantKinds(oldRoles));
        lostKinds.removeAll(Role.participantKinds(newRoles));
        List<GraduationProcessSummary> affected = lostKinds.isEmpty()
                ? List.of()
                : graduationParticipation.markPendingReassignment(account.getId(), lostKinds);
        FieldChange change = account.changeRoles(newRoles);
        save(account);
        changeLog.record(requesterId, TARGET_TYPE, account.getId(), List.of(change));
        notifier.notify(account.getEmail(), "Roles actualizados", """
                Estimado(a) %s:

                Sus roles en el sistema de titulación ahora son: %s.
                """.formatted(account.getFullName(), Role.labels(account.getRoles())));
        return new AccountChangeResult(account, affected);
    }

    /** A4: deactivates the account, leaves its processes pending and notifies the user. */
    public AccountChangeResult deactivateAccount(UUID requesterId, String email) {
        UserAccount account = managedAccount(requester(requesterId), email);
        if (account.isActive() && account.hasRole(Role.ADMINISTRATOR)) {
            checkNotLastAdministrator(account);
        }
        FieldChange change = account.deactivate();
        List<GraduationProcessSummary> affected =
                graduationParticipation.markPendingReassignment(account.getId(), EnumSet.allOf(ParticipantKind.class));
        save(account);
        changeLog.record(requesterId, TARGET_TYPE, account.getId(), List.of(change));
        notifier.notify(account.getEmail(), "Cuenta de usuario desactivada", """
                Estimado(a) %s:

                Su cuenta de usuario en el sistema de titulación fue desactivada. Ya no puede enviar comandos.
                """.formatted(account.getFullName()));
        return new AccountChangeResult(account, affected);
    }

    /** A5: reactivates the account and notifies the user. */
    public UserAccount reactivateAccount(UUID requesterId, String email) {
        UserAccount account = managedAccount(requester(requesterId), email);
        if (!account.isActive() && account.hasRole(Role.THESIS_WORKSHOP_PROFESSOR)
                && accounts.existsOtherActiveWithRole(Role.THESIS_WORKSHOP_PROFESSOR, account.getId())) {
            throw secondThesisWorkshopProfessor();
        }
        FieldChange change = account.reactivate();
        save(account);
        changeLog.record(requesterId, TARGET_TYPE, account.getId(), List.of(change));
        notifier.notify(account.getEmail(), "Cuenta de usuario reactivada", """
                Estimado(a) %s:

                Su cuenta de usuario en el sistema de titulación fue reactivada con los roles: %s.
                """.formatted(account.getFullName(), Role.labels(account.getRoles())));
        return account;
    }

    private UserAccount requester(UUID requesterId) {
        return accounts.findById(requesterId)
                .orElseThrow(() -> new IllegalStateException("Unknown requester " + requesterId));
    }

    /** The account with this email, if it exists and the requester may manage it (E1). */
    private UserAccount managedAccount(UserAccount requester, String email) {
        String normalized = email == null ? "" : UserAccount.normalizeEmail(email);
        UserAccount account = accounts.findByEmail(normalized)
                .orElseThrow(() -> new UserRuleViolationException(RejectionReason.ACCOUNT_NOT_FOUND, normalized));
        if (!Role.canManage(requester.getRoles(), account.getRoles())) {
            throw new UserRuleViolationException(RejectionReason.OUT_OF_SCOPE,
                    "no puede gestionar la cuenta de usuario " + account.getEmail());
        }
        return account;
    }

    /** Rejects roles that the requester may not assign or remove (E1). */
    private static void checkCanAssign(UserAccount requester, Set<Role> roles) {
        Set<Role> outside = EnumSet.noneOf(Role.class);
        for (Role role : roles) {
            if (!Role.canManage(requester.getRoles(), Set.of(role))) {
                outside.add(role);
            }
        }
        if (!outside.isEmpty()) {
            throw new UserRuleViolationException(RejectionReason.OUT_OF_SCOPE,
                    "no puede asignar ni quitar " + (outside.size() == 1 ? "el rol " : "los roles ") + Role.labels(outside));
        }
    }

    private void checkNotLastAdministrator(UserAccount account) {
        if (!accounts.existsOtherActiveWithRole(Role.ADMINISTRATOR, account.getId())) {
            throw new UserRuleViolationException(RejectionReason.LAST_ADMINISTRATOR,
                    account.getEmail() + " es la única cuenta de administrador activa");
        }
    }

    /** Saves and flushes, so that the unique indexes also reject a duplicate (D5). */
    private UserAccount save(UserAccount account) {
        try {
            return accounts.saveAndFlush(account);
        } catch (DataIntegrityViolationException e) {
            String message = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase(Locale.ROOT);
            if (message.contains("user_accounts_email_uk")) {
                throw duplicateEmail(account.getEmail());
            }
            if (message.contains("user_accounts_registration_uk")) {
                throw duplicateRegistration(account.getStudentRegistrationNumber());
            }
            throw e;
        }
    }

    private static UserRuleViolationException duplicateEmail(String email) {
        return new UserRuleViolationException(RejectionReason.DUPLICATE_EMAIL,
                email + " ya pertenece a otra cuenta de usuario");
    }

    private static UserRuleViolationException duplicateRegistration(String number) {
        return new UserRuleViolationException(RejectionReason.DUPLICATE_REGISTRATION_NUMBER,
                number + " ya pertenece a otra cuenta de usuario");
    }

    private static UserRuleViolationException secondThesisWorkshopProfessor() {
        return new UserRuleViolationException(RejectionReason.SECOND_THESIS_WORKSHOP_PROFESSOR,
                "solo puede haber una cuenta de usuario activa con el rol " + Role.THESIS_WORKSHOP_PROFESSOR.label());
    }
}
