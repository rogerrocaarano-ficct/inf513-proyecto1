package grupo03sc.mailapp.audit.application;

/**
 * A changed field: its name, the old value (null on creation) and the new value.
 */
public record FieldChange(String field, String oldValue, String newValue) {

    public static FieldChange created(String field, Object value) {
        return new FieldChange(field, null, value == null ? null : value.toString());
    }
}
