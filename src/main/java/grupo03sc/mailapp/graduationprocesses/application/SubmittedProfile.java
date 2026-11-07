package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.graduationprocesses.domain.GraduationProject;

/** A project profile just presented, and whether a thesis workshop professor was notified (UC-004 step 5). */
public record SubmittedProfile(GraduationProject project, boolean professorNotified) {
}
