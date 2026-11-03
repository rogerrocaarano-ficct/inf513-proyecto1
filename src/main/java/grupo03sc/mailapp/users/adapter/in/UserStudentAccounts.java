package grupo03sc.mailapp.users.adapter.in;

import grupo03sc.mailapp.graduationprocesses.application.StudentAccount;
import grupo03sc.mailapp.graduationprocesses.application.StudentAccounts;
import grupo03sc.mailapp.graduationprocesses.application.StudentData;
import grupo03sc.mailapp.users.application.StudentAccountResult;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Implements the {@link StudentAccounts} port of GraduationProcesses with the user accounts of
 * UC-001 (UC-003 step 3 and A1).
 */
@Component
class UserStudentAccounts implements StudentAccounts {

    private final UserManagementService service;
    private final UserAccountRepository accounts;

    UserStudentAccounts(UserManagementService service, UserAccountRepository accounts) {
        this.service = service;
        this.accounts = accounts;
    }

    @Override
    public StudentAccount obtain(UUID requesterId, StudentData student) {
        StudentAccountResult result = service.obtainStudentAccount(requesterId, new UserAccountData(
                student.fullName(), student.email(), Set.of(Role.STUDENT),
                student.studentRegistrationNumber(), student.idCard(), student.phone()));
        return toStudentAccount(result.account(), result.created());
    }

    @Override
    public Optional<StudentAccount> findByRegistrationNumber(String studentRegistrationNumber) {
        return service.findStudentAccount(studentRegistrationNumber).map(account -> toStudentAccount(account, false));
    }

    @Override
    public Optional<StudentAccount> findById(UUID userAccountId) {
        return accounts.findById(userAccountId).filter(account -> account.hasRole(Role.STUDENT))
                .map(account -> toStudentAccount(account, false));
    }

    private static StudentAccount toStudentAccount(UserAccount account, boolean created) {
        return new StudentAccount(account.getId(), account.getFullName(), account.getEmail(), created);
    }
}
