package grupo03sc.mailapp.users.application;

import grupo03sc.mailapp.users.domain.UserAccount;

/** The user account of a student obtained when a graduation process is opened, and whether it was created. */
public record StudentAccountResult(UserAccount account, boolean created) {
}
