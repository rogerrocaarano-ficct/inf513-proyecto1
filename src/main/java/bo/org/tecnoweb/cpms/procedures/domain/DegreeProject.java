package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.AggregateRoot;
import bo.org.tecnoweb.cpms.common.DomainEvent;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Owns the versions of the project profile with their evaluation (UC-004) and the tutor designation
 * (UC-003).
 */
public class DegreeProject implements AggregateRoot {
    /** Field of the profile errors. */
    static final String PROFILE_FIELD = "perfil";

    private final UUID id;
    private UUID procedureId;
    private String title;
    private final List<ProjectProfile> profiles = new ArrayList<>();
    private UUID tutorId;
    private final List<DomainEvent> events = new ArrayList<>();

    private DegreeProject(UUID id) {
        this.id = id;
    }

    /** Starts a project with a new identity. */
    public static DegreeProject builder() {
        return new DegreeProject(UUID.randomUUID());
    }

    /**
     * Restores a project with its profile versions, oldest first, without publishing events.
     *
     * @param id          the project identity
     * @param procedureId the procedure of the project
     * @param title       the current project title
     * @param profiles    the profile versions, numbered 1, 2, … in this order
     * @param tutorId     the assigned tutor, or {@code null}
     * @return the restored project
     * @throws IllegalArgumentException if the versions are not numbered 1, 2, … in order
     */
    public static DegreeProject rehydrate(UUID id, UUID procedureId, String title, List<ProjectProfile> profiles,
            UUID tutorId) {
        for (int index = 0; index < profiles.size(); index++) {
            if (profiles.get(index).version() != index + 1) {
                throw new IllegalArgumentException("Profile versions must be numbered 1, 2, … in order");
            }
        }
        DegreeProject project = new DegreeProject(id);
        project.procedureId = procedureId;
        project.title = title;
        project.profiles.addAll(profiles);
        project.tutorId = tutorId;
        return project;
    }

    /** Associates the project with its graduation procedure. */
    public DegreeProject withProcedureId(UUID procedureId) {
        this.procedureId = Objects.requireNonNull(procedureId);
        return this;
    }

    /**
     * Basic flow of UC-004: registers a profile version, the first one or a new one after an observation
     * or a rejection (FR-004, FR-023, FR-058). It adopts the stored PDF the student sent, already
     * validated with {@link PdfDocument#require}, and makes its title the project title.
     *
     * @param file      the stored PDF of the profile
     * @param title     the proposed project title
     * @param student   the email of the student
     * @param workshop  the email of the workshop teacher who receives it (FR-057)
     * @param reference the reference of the procedure
     * @return this project
     * @throws DomainValidationException if the current profile is pending or approved, or the title is blank
     */
    public DegreeProject registerProfile(StoredFile file, String title, String student, String workshop,
            String reference) {
        Objects.requireNonNull(procedureId);
        ProjectProfile current = getProfile();
        if (current != null && current.status() == ProjectProfileStatus.PENDING) {
            throw invalid("El trámite ya tiene un perfil de proyecto pendiente de evaluación.");
        }
        if (current != null && current.status() == ProjectProfileStatus.APPROVED) {
            throw invalid("El perfil de proyecto del trámite ya fue aprobado.");
        }
        String value = title == null ? "" : title.strip();
        if (value.isBlank()) {
            throw new DomainValidationException(Map.of("título", "Ingrese el título del proyecto."));
        }
        ProjectProfile profile = ProjectProfile.pending(profiles.size() + 1, file, value);
        profiles.add(profile);
        this.title = value;
        events.add(new DegreeProjectProfileRegisteredEvent(student, workshop, reference, value, profile,
                Instant.now()));
        return this;
    }

    /**
     * A1 of UC-004: approves the pending profile (FR-005).
     *
     * @param student   the email of the student, who is notified (FR-060)
     * @param director  the email of the active director, or {@code null} if there is none (FR-061)
     * @param reference the reference of the procedure
     * @throws DomainValidationException if there is no pending profile
     */
    public void approveProfile(String student, String director, String reference) {
        Instant now = Instant.now();
        evaluate(ProjectProfileStatus.APPROVED, null, now);
        events.add(new DegreeProjectProfileApprovedEvent(student, director, reference, title, now));
    }

    /**
     * A2 of UC-004: observes the pending profile, asking for changes on the same topic (FR-022).
     *
     * @param observations the observations of the workshop teacher
     * @param student      the email of the student, who is notified (FR-060)
     * @param reference    the reference of the procedure
     * @throws DomainValidationException if there is no pending profile or the observations are blank
     */
    public void observeProfile(String observations, String student, String reference) {
        Instant now = Instant.now();
        String value = requireObservations(observations);
        evaluate(ProjectProfileStatus.OBSERVED, value, now);
        events.add(new DegreeProjectProfileObservedEvent(student, reference, title, value, now));
    }

    /**
     * A3 of UC-004: rejects the pending profile because its topic must change (FR-022).
     *
     * @param observations why the topic must change
     * @param student      the email of the student, who is notified (FR-060)
     * @param reference    the reference of the procedure
     * @throws DomainValidationException if there is no pending profile or the observations are blank
     */
    public void rejectProfile(String observations, String student, String reference) {
        Instant now = Instant.now();
        String value = requireObservations(observations);
        evaluate(ProjectProfileStatus.REJECTED, value, now);
        events.add(new DegreeProjectProfileRejectedEvent(student, reference, title, value, now));
    }

    /** Records the designation only when the current profile is approved (FR-003, FR-020). */
    public void assignTutor(UUID tutorId, String tutorName, String student, String reference) {
        ProjectProfile current = getProfile();
        if (current == null || current.status() != ProjectProfileStatus.APPROVED) {
            throw invalid("El perfil de proyecto requiere la aprobación de Taller de Grado.");
        }
        if (this.tutorId != null) {
            throw new DomainValidationException(Map.of("tutor", "El proyecto ya tiene un tutor asignado."));
        }
        this.tutorId = Objects.requireNonNull(tutorId);
        events.add(new DegreeProjectTutorAssignedEvent(student, reference, tutorName, Instant.now()));
    }

    private void evaluate(ProjectProfileStatus result, String observations, Instant now) {
        ProjectProfile current = getProfile();
        if (current == null || current.status() != ProjectProfileStatus.PENDING) {
            throw invalid("El trámite no tiene un perfil de proyecto pendiente de evaluación.");
        }
        profiles.set(profiles.size() - 1, current.evaluated(result, observations, now));
    }

    private static String requireObservations(String observations) {
        if (observations == null || observations.isBlank()) {
            throw new DomainValidationException(Map.of("observaciones",
                    "Escriba las observaciones en el cuerpo del correo."));
        }
        return observations.strip();
    }

    private static DomainValidationException invalid(String message) {
        return new DomainValidationException(Map.of(PROFILE_FIELD, message));
    }

    /** Returns the project identity. */
    @Override
    public UUID getId() {
        return id;
    }

    /** Returns the associated procedure identity. */
    public UUID getProcedureId() {
        return procedureId;
    }

    /** Returns the current project title. */
    public String getTitle() {
        return title;
    }

    /** Returns the current profile version, or {@code null} if none was registered. */
    public ProjectProfile getProfile() {
        return profiles.isEmpty() ? null : profiles.getLast();
    }

    /** Returns every profile version, oldest first (FR-023). */
    public List<ProjectProfile> getProfiles() {
        return List.copyOf(profiles);
    }

    /** Returns the assigned tutor identity, if any. */
    public UUID getTutorId() {
        return tutorId;
    }

    /** Returns and clears the pending domain events. */
    @Override
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pending = List.copyOf(events);
        events.clear();
        return pending;
    }
}
