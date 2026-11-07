package grupo03sc.mailapp.graduationprocesses.domain;

/** Kind of the current document of a graduation project (UC-004), with its name in Spanish. */
public enum ProjectDocumentKind {
    PROJECT_PROFILE("perfil de proyecto"),
    PROJECT_DOCUMENT("documento del proyecto"),
    FINAL_DEFENSE_VERSION("versión para la defensa final");

    private final String label;

    ProjectDocumentKind(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
