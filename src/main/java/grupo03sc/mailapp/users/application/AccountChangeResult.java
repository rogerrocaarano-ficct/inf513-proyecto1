package grupo03sc.mailapp.users.application;

import grupo03sc.mailapp.users.domain.UserAccount;
import java.util.List;

/**
 * A changed user account and the graduation processes left pending reassignment by the change.
 */
public record AccountChangeResult(UserAccount account, List<AffectedProcess> affectedProcesses) {

    public AccountChangeResult {
        affectedProcesses = List.copyOf(affectedProcesses);
    }
}
