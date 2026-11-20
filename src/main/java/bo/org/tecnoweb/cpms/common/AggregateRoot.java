package bo.org.tecnoweb.cpms.common;

import java.util.List;
import java.util.UUID;

/**
 * Root of a domain aggregate.
 *
 * <p>An aggregate root has an identity and collects the domain events raised since they were last
 * pulled, so the application layer can publish them after the changes are saved.
 */
public interface AggregateRoot {

    /**
     * Returns the identity of the aggregate.
     *
     * @return the identifier, never {@code null} once the aggregate is built
     */
    UUID getId();

    /**
     * Returns the pending domain events and clears them.
     *
     * @return the events raised since the last call, in the order they were raised; empty if none
     */
    List<DomainEvent> pullEvents();
}
