package grupo03sc.mailapp.graduationprocesses.domain;

/**
 * Stage of a graduation process, with its name in Spanish. UC-003 uses the opening and the final
 * grade (registered by UC-006); the other use cases add their stages.
 */
public enum GraduationStage {
    OPENED("apertura"),
    FINAL_GRADE_RECORDED("calificación final registrada");

    private final String label;

    GraduationStage(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
