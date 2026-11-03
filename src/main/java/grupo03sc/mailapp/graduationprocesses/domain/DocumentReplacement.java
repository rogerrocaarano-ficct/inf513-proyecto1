package grupo03sc.mailapp.graduationprocesses.domain;

import grupo03sc.mailapp.audit.application.FieldChange;

/** The result of replacing an initial document: the change and the key of the replaced file. */
public record DocumentReplacement(FieldChange change, String replacedStorageKey) {
}
