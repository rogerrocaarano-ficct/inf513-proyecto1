package grupo03sc.mailapp.graduationprocesses.domain;

/**
 * Stage of a graduation process, with its name in Spanish. The constants are declared in the order of
 * the process: {@link GraduationProcess#isPredefenseApproved()} compares them, so a new stage goes in
 * its place, not at the end. UC-003 uses the opening and the final grade, UC-004 reads the approved
 * pre-defense; UC-006 registers both.
 */
public enum GraduationStage {
    OPENED("apertura"),
    PREDEFENSE_APPROVED("predefensa aprobada"),
    FINAL_GRADE_RECORDED("calificación final registrada");

    private final String label;

    GraduationStage(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
