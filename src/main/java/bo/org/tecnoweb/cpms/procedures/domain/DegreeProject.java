package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.AggregateRoot;
import bo.org.tecnoweb.cpms.common.DomainEvent;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.mail.domain.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Owns the initial project profile and the tutor designation (UC-003). */
public class DegreeProject implements AggregateRoot {
    private final UUID id;
    private UUID procedureId;
    private String title;
    private ProjectProfile profile;
    private UUID tutorId;
    private final List<DomainEvent> events = new ArrayList<>();

    private DegreeProject(UUID id) {
        this.id = id;
    }

    /** Starts a project with a new identity. */
    public static DegreeProject builder() {
        return new DegreeProject(UUID.randomUUID());
    }

    /** Restores a project and the evaluation recorded for its profile without publishing events. */
    public static DegreeProject rehydrate(UUID id, UUID procedureId, String title, ProjectProfile profile,
            UUID tutorId) {
        DegreeProject project = new DegreeProject(id);
        project.procedureId = procedureId;
        project.title = title;
        project.profile = profile;
        project.tutorId = tutorId;
        return project;
    }

    /** Associates the project with its graduation procedure. */
    public DegreeProject withProcedureId(UUID procedureId) {
        this.procedureId = java.util.Objects.requireNonNull(procedureId);
        return this;
    }

    /** Sets the proposed project title. */
    public DegreeProject withTitle(String title) {
        this.title = title == null ? "" : title.strip();
        return this;
    }

    /** Registers just the first profile (FR-004, FR-058). */
    public DegreeProject registerInitialProfile(File file, String student, String workshop, String reference) {
        if (profile != null) {
            throw alreadyRegistered();
        }
        java.util.Objects.requireNonNull(procedureId);
        if (title == null || title.isBlank()) {
            throw new DomainValidationException(Map.of("título", "Ingrese el título del proyecto."));
        }
        profile = ProjectProfile.pending(file);
        events.add(new DegreeProjectProfileRegisteredEvent(student, workshop, reference, title, profile, Instant.now()));
        return this;
    }

    /** Records the designation only after a positive profile evaluation (FR-003, FR-020). */
    public void assignTutor(UUID tutorId, String tutorName, String student, String reference) {
        if (profile == null || !profile.approved()) {
            throw new DomainValidationException(Map.of("perfil", "El perfil requiere el visto bueno de Taller de Grado."));
        }
        if (this.tutorId != null) {
            throw new DomainValidationException(Map.of("tutor", "El proyecto ya tiene un tutor asignado."));
        }
        this.tutorId = java.util.Objects.requireNonNull(tutorId);
        events.add(new DegreeProjectTutorAssignedEvent(student, reference, tutorName, Instant.now()));
    }

    /** Creates the duplicate initial profile error (FR-058). */
    public static DomainValidationException alreadyRegistered() {
        return new DomainValidationException(Map.of("perfil", "El trámite ya tiene un perfil de proyecto registrado."));
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

    /** Returns the project title. */
    public String getTitle() {
        return title;
    }

    /** Returns the initial profile. */
    public ProjectProfile getProfile() {
        return profile;
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
