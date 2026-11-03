package grupo03sc.mailapp.users.adapter.in;

import grupo03sc.mailapp.graduationprocesses.application.ProfessorContact;
import grupo03sc.mailapp.graduationprocesses.application.ProfessorContacts;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the {@link ProfessorContacts} port of GraduationProcesses (UC-004). It reads the
 * repository only, not UserManagementService, so that no bean cycle forms with GraduationParticipation.
 */
@Component
@Transactional(readOnly = true)
class UserProfessorContacts implements ProfessorContacts {

    private final UserAccountRepository accounts;

    UserProfessorContacts(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public Optional<ProfessorContact> thesisWorkshopProfessor() {
        return accounts.findActiveWithRole(Role.THESIS_WORKSHOP_PROFESSOR).stream().findFirst()
                .map(UserProfessorContacts::toContact);
    }

    @Override
    public Optional<ProfessorContact> findById(UUID userAccountId) {
        return accounts.findById(userAccountId).filter(UserAccount::isActive).map(UserProfessorContacts::toContact);
    }

    private static ProfessorContact toContact(UserAccount account) {
        return new ProfessorContact(account.getId(), account.getFullName(), account.getEmail());
    }
}
