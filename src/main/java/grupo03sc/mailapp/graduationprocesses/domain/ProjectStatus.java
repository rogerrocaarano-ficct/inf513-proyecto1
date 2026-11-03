package grupo03sc.mailapp.graduationprocesses.domain;

/**
 * Status of the current document of a graduation project, with its name in Spanish. The PROFILE_
 * statuses belong to a project profile, the others to a version. UC-004 sets PROFILE_PENDING and
 * TUTOR_REVIEW; the evaluation and the reviews of UC-006 set the others.
 */
public enum ProjectStatus {
    PROFILE_PENDING("pendiente de evaluación"),
    PROFILE_OBSERVED("observado"),
    PROFILE_REJECTED("rechazado"),
    PROFILE_APPROVED("con visto bueno"),
    TUTOR_REVIEW("en revisión del tutor"),
    TUTOR_OBSERVED("observada por el tutor"),
    TRIBUNAL_REVIEW("en revisión del tribunal"),
    TRIBUNAL_OBSERVED("observada por el tribunal"),
    APPROVED("aprobada");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
