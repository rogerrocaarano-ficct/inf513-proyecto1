package bo.org.tecnoweb.cpms.common;

import java.time.Instant;

/**
 * Something that happened in the domain and that other parts of the system may react to.
 */
public interface DomainEvent {

    /**
     * Returns the moment the event happened.
     *
     * @return the instant the event happened
     */
    Instant occurredOn();
}
