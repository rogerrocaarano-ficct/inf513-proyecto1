package bo.org.tecnoweb.cpms.users.application;

import java.util.List;

/**
 * Result of {@code usuarios.listar[role]} (UC-001 A3), sent only to the requester.
 *
 * @param users              the accounts of the role, ordered by first surname, second surname and
 *                           names; empty if there are none
 * @param role               label of the listed role, e.g. {@code docente}
 * @param requesterUserEmail email address of the requester, who receives the list
 */
public record ListUsersQueryResult(List<ListUsersQueryItem> users, String role, String requesterUserEmail) {

    /**
     * Creates the result with an unmodifiable copy of the list.
     */
    public ListUsersQueryResult {
        users = List.copyOf(users);
    }
}
