package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.audit.application.ChangeLog;
import grupo03sc.mailapp.audit.application.FieldChange;
import grupo03sc.mailapp.graduationprocesses.domain.DocumentFile;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcess;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcessStatus;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProject;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRejectionReason;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRuleViolationException;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectDocumentKind;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectHistory;
import grupo03sc.mailapp.messaging.application.Notifier;
import grupo03sc.mailapp.storage.application.FileStorage;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages the graduation project of a student (UC-004). Each operation is one transaction: the
 * project and its change log entries are kept or undone together; the stored file is deleted if it
 * rolls back, and the notices are sent after commit. The files of earlier documents are never deleted.
 */
@Service
@Transactional
public class GraduationProjectService {

    static final String TARGET_TYPE = "GraduationProject";
    static final String FILE_CATEGORY = "graduation-projects";
    private static final String TUTOR = "TUTOR";
    private static final Logger log = LoggerFactory.getLogger(GraduationProjectService.class);

    private final GraduationProjectRepository projects;
    private final GraduationProcessRepository processes;
    private final StudentAccounts studentAccounts;
    private final ProfessorContacts professorContacts;
    private final FileStorage fileStorage;
    private final ChangeLog changeLog;
    private final Notifier notifier;
    private final Clock clock;

    GraduationProjectService(GraduationProjectRepository projects, GraduationProcessRepository processes,
            StudentAccounts studentAccounts, ProfessorContacts professorContacts, FileStorage fileStorage,
            ChangeLog changeLog, Notifier notifier, Clock clock) {
        this.projects = projects;
        this.processes = processes;
        this.studentAccounts = studentAccounts;
        this.professorContacts = professorContacts;
        this.fileStorage = fileStorage;
        this.changeLog = changeLog;
        this.notifier = notifier;
        this.clock = clock;
    }

    /**
     * Basic flow and A1: presents the first profile, which creates the project, or a new one after an
     * observation or a rejection, and notifies the thesis workshop professor.
     */
    public SubmittedProfile submitProfile(UUID studentId, String title, List<DocumentFile> files) {
        GraduationProcess process = processInProgress(studentId);
        Optional<GraduationProject> existing = projects.findByGraduationProcessId(process.getId());
        existing.ifPresent(GraduationProject::checkProfileAllowed);
        DocumentFile file = singleFile(files);
        GraduationProject.checkFile(file);
        Instant now = Instant.now(clock);
        String storageKey = fileStorage.store(FILE_CATEGORY, file.fileName(), file.content());
        GraduationProject project;
        List<FieldChange> changes;
        if (existing.isPresent()) {
            project = existing.get();
            changes = project.submitProfile(title, file, storageKey, now);
        } else {
            project = GraduationProject.createWithProfile(process.getId(), title, file, storageKey, now);
            changes = null;
        }
        project = projects.saveAndFlush(project);
        changeLog.record(studentId, TARGET_TYPE, project.getId(), changes == null ? project.describe() : changes);
        return new SubmittedProfile(project, notifyThesisWorkshopProfessor(studentId, project));
    }

    /** A2 and A3: delivers a new version of the project document and notifies the tutor. */
    public GraduationProject submitVersion(UUID studentId, String generalComment, List<DocumentFile> files) {
        GraduationProcess process = processInProgress(studentId);
        GraduationProject project = projects.findByGraduationProcessId(process.getId())
                .orElseThrow(() -> new GraduationRuleViolationException(GraduationRejectionReason.VERSION_PREREQUISITES,
                        "todavía no presentó un perfil de proyecto"));
        boolean predefenseApproved = process.isPredefenseApproved();
        project.checkVersionAllowed(predefenseApproved);
        UUID tutorId = processes.findParticipantIds(process.getId(), TUTOR).stream().findFirst()
                .orElseThrow(() -> new GraduationRuleViolationException(GraduationRejectionReason.VERSION_PREREQUISITES,
                        "todavía no tiene tutor asignado"));
        DocumentFile file = singleFile(files);
        GraduationProject.checkFile(file);
        String storageKey = fileStorage.store(FILE_CATEGORY, file.fileName(), file.content());
        List<FieldChange> changes = project.submitVersion(generalComment, file, storageKey, predefenseApproved,
                Instant.now(clock));
        project = projects.saveAndFlush(project);
        changeLog.record(studentId, TARGET_TYPE, project.getId(), changes);
        notifyTutor(studentId, tutorId, project);
        return project;
    }

    /** A4: the project of the student with its history, or empty if no profile was presented yet. */
    @Transactional(readOnly = true)
    public Optional<ProjectView> viewProject(UUID studentId) {
        GraduationProcess process = processInProgress(studentId);
        return projects.findByGraduationProcessId(process.getId())
                .map(project -> new ProjectView(project,
                        ProjectHistory.from(changeLog.history(TARGET_TYPE, project.getId()))));
    }

    /** The precondition of UC-004: the graduation process in progress of the student. */
    private GraduationProcess processInProgress(UUID studentId) {
        return processes.findByStudentAccountIdAndStatus(studentId, GraduationProcessStatus.IN_PROGRESS)
                .orElseThrow(() -> new GraduationRuleViolationException(GraduationRejectionReason.PROCESS_NOT_FOUND, null));
    }

    /** E4: the command takes exactly one attachment. */
    private static DocumentFile singleFile(List<DocumentFile> files) {
        if (files.size() != 1) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.ATTACHMENT_COUNT,
                    "se espera un solo adjunto y llegaron " + files.size());
        }
        return files.getFirst();
    }

    /** Step 5: true if there is an active thesis workshop professor to notify. */
    private boolean notifyThesisWorkshopProfessor(UUID studentId, GraduationProject project) {
        Optional<ProfessorContact> professor = professorContacts.thesisWorkshopProfessor();
        if (professor.isEmpty()) {
            log.warn("No active thesis workshop professor to notify of the profile of project {}", project.getId());
            return false;
        }
        notifier.notify(professor.get().email(), "Perfil de proyecto para evaluar", """
                Estimado(a) %s:

                %s presentó un perfil de proyecto para evaluar.
                Título del proyecto: %s
                """.formatted(professor.get().fullName(), studentName(studentId), project.getTitle()));
        return true;
    }

    /** A2.5: the notice is omitted if the tutor's account is deactivated (the process is pending reassignment). */
    private void notifyTutor(UUID studentId, UUID tutorId, GraduationProject project) {
        Optional<ProfessorContact> tutor = professorContacts.findById(tutorId);
        if (tutor.isEmpty()) {
            log.warn("The tutor {} of project {} is not active; the notice is omitted", tutorId, project.getId());
            return;
        }
        String kind = project.getDocumentKind() == ProjectDocumentKind.FINAL_DEFENSE_VERSION
                ? "\nEs la versión para la defensa final." : "";
        notifier.notify(tutor.get().email(), "Versión del documento del proyecto para revisar", """
                Estimado(a) %s:

                %s entregó la versión %d del documento del proyecto para revisar.%s
                Título del proyecto: %s
                Comentario general: %s
                """.formatted(tutor.get().fullName(), studentName(studentId), project.getVersionNumber(), kind,
                project.getTitle(), project.getGeneralComment()));
    }

    private String studentName(UUID studentId) {
        return studentAccounts.findById(studentId)
                .map(student -> "El estudiante " + student.fullName() + " (" + student.email() + ")")
                .orElse("Un estudiante");
    }
}
