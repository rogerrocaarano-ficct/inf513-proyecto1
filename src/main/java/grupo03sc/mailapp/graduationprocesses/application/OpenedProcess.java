package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcess;

/** A graduation process just opened and the user account of its student. */
public record OpenedProcess(GraduationProcess process, StudentAccount student) {
}
