package grupo03sc.mailapp.graduationprocesses.application;

import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.email;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.fourDocuments;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.pdf;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.registration;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.student;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.audit.application.ChangeLogEntryRepository;
import grupo03sc.mailapp.audit.domain.ChangeLogEntry;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcess;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcessStatus;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRejectionReason;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRuleViolationException;
import grupo03sc.mailapp.graduationprocesses.domain.InitialDocument;
import grupo03sc.mailapp.graduationprocesses.domain.InitialDocumentType;
import grupo03sc.mailapp.messaging.application.CommandRejectedException;
import grupo03sc.mailapp.storage.application.FileStorageProperties;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccountData;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** UC-003 through its service (spec graduation-processes). */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GraduationProcessServiceTests {

    @Autowired
    private GraduationProcessService service;

    @Autowired
    private GraduationProcessRepository processes;

    @Autowired
    private UserManagementService users;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private ChangeLogEntryRepository changeLog;

    @Autowired
    private FileStorageProperties files;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transaction;

    private UUID secretaryId;

    @BeforeEach
    void createSecretary() {
        UUID adminId = accounts.findByEmail("admin@mailapp.test").orElseThrow().getId();
        secretaryId = users.createAccount(adminId,
                UserAccountData.of("Secretaria", email("secretaria"), Set.of(Role.PROGRAM_SECRETARY))).getId();
    }

    private Path file(String storageKey) {
        return files.root().resolve(storageKey);
    }

    private List<String> fields(UUID processId) {
        return changeLog.findByTargetTypeAndTargetIdOrderByChangedAt("GraduationProcess", processId).stream()
                .map(ChangeLogEntry::getField).toList();
    }

    private GraduationProcess reload(OpenedProcess opened) {
        return processes.findByStudentAccountIdAndStatus(opened.student().id(), opened.process().getStatus())
                .orElseThrow();
    }

    @Test
    void opensTheProcessWithTheFourDocumentsAndANewStudentAccount() {
        String email = email("juan");

        OpenedProcess opened = service.openProcess(secretaryId, student(email, registration()), fourDocuments());

        assertThat(opened.student().created()).isTrue();
        assertThat(accounts.findByEmail(email)).isPresent();
        GraduationProcess process = reload(opened);
        assertThat(process.getStatus()).isEqualTo(GraduationProcessStatus.IN_PROGRESS);
        assertThat(process.getInitialDocuments()).extracting(InitialDocument::getType)
                .containsExactlyInAnyOrder(InitialDocumentType.values());
        assertThat(process.getInitialDocuments())
                .allSatisfy(document -> assertThat(file(document.getStorageKey())).exists());
        assertThat(fields(process.getId())).contains("status", "stage", "studentAccountId",
                "initialDocuments.ID_CARD_COPY");
    }

    @Test
    void opensTheProcessOnTheExistingStudentAccount() {
        String email = email("juan");
        String registration = registration();
        UUID existing = service.openProcess(secretaryId, student(email, registration), fourDocuments()).student().id();
        jdbc.update("UPDATE graduation_processes SET status = 'CLOSED', closed_at = now(),"
                + " physical_copies_received_at = now() WHERE student_account_id = ?", existing);

        OpenedProcess opened = service.openProcess(secretaryId, student(email, registration), fourDocuments());

        assertThat(opened.student().id()).isEqualTo(existing);
        assertThat(opened.student().created()).isFalse();
    }

    @Test
    void rejectsASecondProcessInProgress() {
        String email = email("juan");
        String registration = registration();
        service.openProcess(secretaryId, student(email, registration), fourDocuments());

        assertThatThrownBy(() -> service.openProcess(secretaryId, student(email, registration), fourDocuments()))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.PROCESS_IN_PROGRESS));
    }

    @Test
    void rejectsAnIncompleteOpeningWithoutCreatingTheAccount() {
        String email = email("juan");

        assertThatThrownBy(() -> service.openProcess(secretaryId, student(email, registration()),
                List.of(pdf("carta.pdf"), pdf("cv.pdf"), pdf("cedula.pdf"))))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.MISSING_DOCUMENT));
        assertThat(accounts.findByEmail(email)).isEmpty();
    }

    @Test
    void aRejectionAfterOpeningLeavesNoAccountProcessFileOrChangeLog() throws Exception {
        String email = email("juan");
        long filesBefore = countFiles();

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            service.openProcess(secretaryId, student(email, registration()), fourDocuments());
            throw new CommandRejectedException("rechazo simulado");
        })).isInstanceOf(CommandRejectedException.class);

        assertThat(accounts.findByEmail(email)).isEmpty();
        assertThat(countFiles()).isEqualTo(filesBefore);
    }

    @Test
    void replacesAnInitialDocumentAndDeletesTheOldFile() {
        String registration = registration();
        OpenedProcess opened = service.openProcess(secretaryId, student(email("juan"), registration), fourDocuments());
        String oldKey = keyOf(reload(opened), InitialDocumentType.ENROLLMENT_SLIP);

        InitialDocumentType type = service.replaceInitialDocument(secretaryId, registration,
                List.of(pdf("Boleta-nueva.pdf")));

        assertThat(type).isEqualTo(InitialDocumentType.ENROLLMENT_SLIP);
        GraduationProcess process = reload(opened);
        String newKey = keyOf(process, InitialDocumentType.ENROLLMENT_SLIP);
        assertThat(newKey).isNotEqualTo(oldKey);
        assertThat(file(newKey)).exists();
        assertThat(file(oldKey)).doesNotExist();
        assertThat(fields(process.getId())).contains("initialDocuments.ENROLLMENT_SLIP");
    }

    @Test
    void rejectsAReplacementWithoutAProcessInProgressOrWithTwoAttachments() {
        String registration = registration();
        service.openProcess(secretaryId, student(email("juan"), registration), fourDocuments());

        assertThatThrownBy(() -> service.replaceInitialDocument(secretaryId, registration(), List.of(pdf("boleta.pdf"))))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.PROCESS_NOT_FOUND));
        assertThatThrownBy(() -> service.replaceInitialDocument(secretaryId, registration,
                List.of(pdf("boleta.pdf"), pdf("carta.pdf"))))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.ATTACHMENT_COUNT));
    }

    @Test
    void closesOnlyAfterTheFinalGrade() {
        String registration = registration();
        OpenedProcess opened = service.openProcess(secretaryId, student(email("juan"), registration), fourDocuments());

        assertThatThrownBy(() -> service.closeProcess(secretaryId, registration))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.NO_FINAL_GRADE));

        jdbc.update("UPDATE graduation_processes SET stage = 'FINAL_GRADE_RECORDED' WHERE id = ?",
                opened.process().getId());
        StudentAccount student = service.closeProcess(secretaryId, registration);

        assertThat(student.id()).isEqualTo(opened.student().id());
        assertThat(jdbc.queryForObject("SELECT status FROM graduation_processes WHERE id = ?", String.class,
                opened.process().getId())).isEqualTo("CLOSED");
        assertThat(fields(opened.process().getId())).contains("closedAt", "physicalCopiesReceivedAt");
    }

    private static String keyOf(GraduationProcess process, InitialDocumentType type) {
        return process.getInitialDocuments().stream().filter(d -> d.getType() == type).findFirst().orElseThrow()
                .getStorageKey();
    }

    private long countFiles() throws Exception {
        Path root = files.root().resolve(GraduationProcessService.FILE_CATEGORY);
        if (!Files.exists(root)) {
            return 0;
        }
        try (var stream = Files.list(root)) {
            return stream.count();
        }
    }
}
