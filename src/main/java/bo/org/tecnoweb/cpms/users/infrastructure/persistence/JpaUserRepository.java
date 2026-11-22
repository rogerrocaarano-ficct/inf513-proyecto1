package bo.org.tecnoweb.cpms.users.infrastructure.persistence;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter of {@link UserRepository}.
 *
 * <p>Before saving, it checks that the email address and the code do not belong to another account
 * (FR-033); the {@code UNIQUE} constraints of the table back that check.
 */
@Repository
class JpaUserRepository implements UserRepository {

    private static final String TAKEN = "Ya pertenece a otra cuenta.";

    /** Order of account lists: first surname, second surname (missing ones first) and names. */
    private static final Sort BY_NAME = Sort.by(
            Sort.Order.asc("lastName"),
            Sort.Order.asc("secondLastName").nullsFirst(),
            Sort.Order.asc("firstName"));

    private final SpringDataUserAccountRepository accounts;

    /**
     * Creates the adapter.
     *
     * @param accounts Spring Data repository of the entities
     */
    JpaUserRepository(SpringDataUserAccountRepository accounts) {
        this.accounts = accounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void insert(User user) {
        checkUnique(user);
        UserAccountEntity entity = new UserAccountEntity(user.getId());
        copyFields(user, entity);
        accounts.save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void update(User user) {
        checkUnique(user);
        UserAccountEntity entity = accounts.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("No account with id " + user.getId()));
        copyFields(user, entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return accounts.findByEmailAddress(email.strip().toLowerCase(Locale.ROOT)).map(JpaUserRepository::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getById(UUID id) {
        return accounts.findById(id).map(JpaUserRepository::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getByRole(Role role) {
        return accounts.findByRole(role, BY_NAME).stream().map(JpaUserRepository::toDomain).toList();
    }

    /**
     * Rejects the account if its email address or its code belong to another account.
     *
     * @param user the account about to be saved
     * @throws DomainValidationException with one error per repeated field
     */
    private void checkUnique(User user) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (accounts.existsByEmailAddressAndIdNot(user.getEmailAddress(), user.getId())) {
            errors.put(User.EMAIL, TAKEN);
        }
        if (user.getUagrmCode() != null && accounts.existsByUagrmCodeAndIdNot(user.getUagrmCode(), user.getId())) {
            errors.put(User.CODE, TAKEN);
        }
        if (!errors.isEmpty()) {
            throw new DomainValidationException(errors);
        }
    }

    private static void copyFields(User user, UserAccountEntity entity) {
        entity.setEmailAddress(user.getEmailAddress());
        entity.setRole(user.getRole());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setSecondLastName(user.getSecondLastName());
        entity.setCi(user.getCi());
        entity.setUagrmCode(user.getUagrmCode());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setActive(user.isActive());
    }

    private static User toDomain(UserAccountEntity entity) {
        return User.rehydrate()
                .withId(entity.getId())
                .withEmailAddress(entity.getEmailAddress())
                .withRole(entity.getRole())
                .withFirstName(entity.getFirstName())
                .withLastName(entity.getLastName())
                .withSecondLastName(entity.getSecondLastName())
                .withCi(entity.getCi())
                .withUagrmCode(entity.getUagrmCode())
                .withPhoneNumber(entity.getPhoneNumber())
                .withActive(entity.isActive());
    }
}
