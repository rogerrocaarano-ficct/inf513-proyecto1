package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.audit.application.ChangeLog;
import grupo03sc.mailapp.audit.application.FieldChange;
import grupo03sc.mailapp.graduationprocesses.domain.DocumentFile;
import grupo03sc.mailapp.graduationprocesses.domain.DocumentReplacement;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcess;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcessStatus;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRejectionReason;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRuleViolationException;
import grupo03sc.mailapp.graduationprocesses.domain.InitialDocument;
import grupo03sc.mailapp.graduationprocesses.domain.InitialDocumentType;
import grupo03sc.mailapp.messaging.application.Notifier;
import grupo03sc.mailapp.storage.application.FileStorage;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages graduation processes (UC-003). Each operation is one transaction: the student account,
 * the process, its documents and their change log entries are kept or undone together; the stored
 * files are deleted if it rolls back, and the notices are sent after commit.
 */
@Service
@Transactional
public class GraduationProcessService {

    static final String TARGET_TYPE = "GraduationProcess";
    static final String FILE_CATEGORY = "initial-documents";
    private static final String IN_PROGRESS_INDEX = "graduation_processes_student_in_progress_uk";

    private final GraduationProcessRepository processes;
    private final StudentAccounts studentAccounts;
    private final FileStorage fileStorage;
    private final ChangeLog changeLog;
    private final Notifier notifier;
    private final Clock clock;

    GraduationProcessService(GraduationProcessRepository processes, StudentAccounts studentAccounts,
            FileStorage fileStorage, ChangeLog changeLog, Notifier notifier, Clock clock) {
        this.processes = processes;
        this.studentAccounts = studentAccounts;
        this.fileStorage = fileStorage;
        this.changeLog = changeLog;
        this.notifier = notifier;
        this.clock = clock;
    }

    /**
     * Basic flow and A1: checks the initial documents, obtains or creates the student account, stores
     * the files and opens the process.
     */
    public OpenedProcess openProcess(UUID requesterId, StudentData student, List<DocumentFile> files) {
        Map<InitialDocumentType, DocumentFile> byType = InitialDocumentType.classify(files);
        byType.values().forEach(InitialDocument::check);
        StudentAccount account = studentAccounts.obtain(requesterId, student);
        if (processes.existsByStudentAccountIdAndStatus(account.id(), GraduationProcessStatus.IN_PROGRESS)) {
            throw processInProgress(account);
        }
        Instant now = Instant.now(clock);
        List<InitialDocument> documents = byType.entrySet().stream()
                .map(entry -> storeDocument(entry.getKey(), entry.getValue(), now))
                .toList();
        GraduationProcess process = save(GraduationProcess.open(account.id(), documents, now), account);
        changeLog.record(requesterId, TARGET_TYPE, process.getId(), process.describe());
        notifier.notify(account.email(), "Trámite de titulación abierto", """
                Estimado(a) %s:

                Se abrió su trámite de titulación por Proyecto de Grado con estos documentos iniciales:
                %s
                """.formatted(account.fullName(), documentList(process)));
        return new OpenedProcess(process, account);
    }

    /** A2: replaces the initial document of the type named by the only attachment. */
    public InitialDocumentType replaceInitialDocument(UUID requesterId, String studentRegistrationNumber,
            List<DocumentFile> files) {
        if (files.size() != 1) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.ATTACHMENT_COUNT,
                    "se espera un solo adjunto y llegaron " + files.size());
        }
        InProgress inProgress = processInProgress(studentRegistrationNumber);
        DocumentFile file = files.getFirst();
        InitialDocumentType type = InitialDocumentType.fromFileName(file.fileName())
                .orElseThrow(() -> new GraduationRuleViolationException(GraduationRejectionReason.UNRECOGNIZED_DOCUMENT,
                        file.fileName() + " (el nombre debe contener carta, hojadevida o cv, cedula o boleta)"));
        InitialDocument.check(file);
        String storageKey = fileStorage.store(FILE_CATEGORY, file.fileName(), file.content());
        DocumentReplacement replacement =
                inProgress.process().replaceInitialDocument(type, file, storageKey, Instant.now(clock));
        GraduationProcess process = save(inProgress.process(), inProgress.student());
        fileStorage.delete(replacement.replacedStorageKey());
        changeLog.record(requesterId, TARGET_TYPE, process.getId(), List.of(replacement.change()));
        return type;
    }

    /** A3: registers the two physical copies, closes the process and archives its record. */
    public StudentAccount closeProcess(UUID requesterId, String studentRegistrationNumber) {
        InProgress inProgress = processInProgress(studentRegistrationNumber);
        List<FieldChange> changes = inProgress.process().close(Instant.now(clock));
        GraduationProcess process = save(inProgress.process(), inProgress.student());
        changeLog.record(requesterId, TARGET_TYPE, process.getId(), changes);
        StudentAccount student = inProgress.student();
        notifier.notify(student.email(), "Trámite de titulación cerrado", """
                Estimado(a) %s:

                Su trámite de titulación por Proyecto de Grado fue cerrado y su expediente quedó archivado.
                Se registró la recepción de los dos ejemplares físicos de su proyecto.
                """.formatted(student.fullName()));
        return student;
    }

    private record InProgress(GraduationProcess process, StudentAccount student) {
    }

    /** E5: the process in progress of the student with this university registration number. */
    private InProgress processInProgress(String studentRegistrationNumber) {
        StudentAccount student = studentAccounts.findByRegistrationNumber(studentRegistrationNumber)
                .orElseThrow(() -> new GraduationRuleViolationException(GraduationRejectionReason.PROCESS_NOT_FOUND,
                        "el registro universitario " + studentRegistrationNumber + " no es de un estudiante"));
        GraduationProcess process = processes
                .findByStudentAccountIdAndStatus(student.id(), GraduationProcessStatus.IN_PROGRESS)
                .orElseThrow(() -> new GraduationRuleViolationException(GraduationRejectionReason.PROCESS_NOT_FOUND,
                        student.fullName() + " (" + studentRegistrationNumber + ")"));
        return new InProgress(process, student);
    }

    private InitialDocument storeDocument(InitialDocumentType type, DocumentFile file, Instant now) {
        String storageKey = fileStorage.store(FILE_CATEGORY, file.fileName(), file.content());
        return InitialDocument.of(type, file, storageKey, now);
    }

    /** Saves and flushes, so that the partial unique index also rejects a second process in progress. */
    private GraduationProcess save(GraduationProcess process, StudentAccount student) {
        try {
            return processes.saveAndFlush(process);
        } catch (DataIntegrityViolationException e) {
            if (String.valueOf(e.getMostSpecificCause().getMessage()).contains(IN_PROGRESS_INDEX)) {
                throw processInProgress(student);
            }
            throw e;
        }
    }

    private static GraduationRuleViolationException processInProgress(StudentAccount student) {
        return new GraduationRuleViolationException(GraduationRejectionReason.PROCESS_IN_PROGRESS, student.fullName());
    }

    private static String documentList(GraduationProcess process) {
        return process.getInitialDocuments().stream()
                .sorted(Comparator.comparing(InitialDocument::getType))
                .map(document -> "- " + capitalize(document.getType().label()) + ": " + document.getFileName())
                .collect(Collectors.joining("\n"));
    }

    private static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
