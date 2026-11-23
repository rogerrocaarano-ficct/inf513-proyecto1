package bo.org.tecnoweb.cpms.procedures.domain;

/** State of a version of the project profile (UC-004, FR-005, FR-022). */
public enum ProjectProfileStatus {
    /** Submitted and not evaluated yet by the workshop teacher. */
    PENDING,

    /** Approved by the workshop teacher (FR-005). */
    APPROVED,

    /** Observed: changes are needed on the same topic (FR-022). */
    OBSERVED,

    /** Rejected: the topic must change (FR-022). */
    REJECTED;

    /**
     * Tells whether the student may submit a new version after a profile in this state (FR-058).
     *
     * @return {@code true} if the profile was observed or rejected
     */
    public boolean allowsNewVersion() {
        return this == OBSERVED || this == REJECTED;
    }
}
