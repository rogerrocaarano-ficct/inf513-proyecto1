package grupo03sc.mailapp.graduationprocesses.application;

import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.email;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.fourDocuments;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.pdf;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.registration;
import static grupo03sc.mailapp.graduationprocesses.application.GraduationTestData.student;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.audit.application.ChangeLog;
import grupo03sc.mailapp.audit.application.LoggedChange;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcess;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcessStatus;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProject;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRejectionReason;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationRuleViolationException;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectDocumentKind;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectHistoryItem;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectStatus;
import grupo03sc.mailapp.messaging.application.Notifier;
import grupo03sc.mailapp.storage.application.FileStorageProperties;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** UC-004 through its service (spec graduation-projects). */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GraduationProjectServiceTests {

    @Autowired
    private GraduationProjectService service;

    @Autowired
    private GraduationProcessService processService;

    @Autowired
    private GraduationProcessRepository processes;

    @Autowired
    private GraduationProjectRepository projects;

    @Autowired
    private UserManagementService users;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private ChangeLog changeLog;

    @Autowired
    private FileStorageProperties files;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private Notifier notifier;

    private GraduationProjectFixtures fixtures;
    private OpenedProcess opened;
    private UUID studentId;

    @BeforeEach
    void openProcess() {
        fixtures = new GraduationProjectFixtures(jdbc, users, accounts);
        UUID adminId = accounts.findByEmail("admin@mailapp.test").orElseThrow().getId();
        UUID secretaryId = users.createAccount(adminId,
                UserAccountData.of("Secretaria", email("secretaria"), Set.of(Role.PROGRAM_SECRETARY))).getId();
        opened = processService.openProcess(secretaryId, student(email("juan"), registration()), fourDocuments());
        studentId = opened.student().id();
        clearInvocations(notifier);
    }

    private UUID processId() {
        return opened.process().getId();
    }

    private GraduationProject project() {
        return projects.findByGraduationProcessId(processId()).orElseThrow();
    }

    private GraduationProject presentProfile(String title) {
        return service.submitProfile(studentId, title, List.of(pdf("perfil.pdf"))).project();
    }

    /** A project with its profile approved and a tutor, ready for the first version. */
    private UserAccount readyForVersions() {
        presentProfile("Sistema de inventario");
        fixtures.setStatus(project().getId(), ProjectStatus.PROFILE_APPROVED);
        return fixtures.assignTutor(processId());
    }

    private GraduationProject deliver(String comment) {
        return service.submitVersion(studentId, comment, List.of(pdf("documento.pdf")));
    }

    private static void assertRejected(Runnable action, GraduationRejectionReason reason, String text) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(GraduationRuleViolationException.class, e -> {
            assertThat(e.getReason()).isEqualTo(reason);
            assertThat(e.getMessage()).contains(text);
        });
    }

    private Path file(String storageKey) {
        return files.root().resolve(storageKey);
    }

    private long storedProjectFiles() throws IOException {
        Path dir = files.root().resolve(GraduationProjectService.FILE_CATEGORY);
        if (!Files.exists(dir)) {
            return 0;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.count();
        }
    }

    @Test
    void theFixturesSetTheTutorTheStatusAndThePredefense() {
        presentProfile("Sistema de inventario");
        UserAccount tutor = fixtures.assignTutor(processId());
        fixtures.setStatus(project().getId(), ProjectStatus.PROFILE_APPROVED);
        fixtures.approvePredefense(processId());

        assertThat(processes.findParticipantIds(processId(), "TUTOR")).containsExactly(tutor.getId());
        assertThat(project().getStatus()).isEqualTo(ProjectStatus.PROFILE_APPROVED);
        assertThat(changeLog.history("GraduationProject", project().getId()).getLast())
                .extracting(LoggedChange::field, LoggedChange::newValue)
                .containsExactly("status", "PROFILE_APPROVED");
        GraduationProcess process = processes.findByStudentAccountIdAndStatus(studentId,
                GraduationProcessStatus.IN_PROGRESS).orElseThrow();
        assertThat(process.isPredefenseApproved()).isTrue();
    }

    @Test
    void theFirstProfileCreatesTheProjectAndNotifiesTheThesisWorkshopProfessor() {
        UserAccount professor = fixtures.thesisWorkshopProfessor();

        SubmittedProfile submitted = service.submitProfile(studentId, "Sistema de inventario",
                List.of(pdf("perfil.pdf")));

        assertThat(submitted.professorNotified()).isTrue();
        GraduationProject project = project();
        assertThat(project.getTitle()).isEqualTo("Sistema de inventario");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PROFILE_PENDING);
        assertThat(file(project.getStorageKey())).exists();
        assertThat(changeLog.history("GraduationProject", project.getId())).extracting(LoggedChange::field)
                .contains("graduationProcessId", "documentKind", "title", "storageKey", "status");
        verify(notifier).notify(eq(professor.getEmail()), eq("Perfil de proyecto para evaluar"),
                contains("Juan Soto"));
        verify(notifier).notify(anyString(), anyString(), contains("Sistema de inventario"));
    }

    @Test
    void theProfileIsRegisteredWithoutAnActiveThesisWorkshopProfessor() {
        fixtures.withoutThesisWorkshopProfessor(() -> {
            SubmittedProfile submitted = service.submitProfile(studentId, "Sistema de inventario",
                    List.of(pdf("perfil.pdf")));

            assertThat(submitted.professorNotified()).isFalse();
        });

        assertThat(project().getStatus()).isEqualTo(ProjectStatus.PROFILE_PENDING);
        verify(notifier, never()).notify(anyString(), anyString(), anyString());
    }

    @Test
    void aNewProfileAfterAnObservationReplacesTheCurrentOneAndKeepsTheFile() {
        String firstKey = presentProfile("Sistema de inventario").getStorageKey();
        fixtures.setStatus(project().getId(), ProjectStatus.PROFILE_OBSERVED);

        GraduationProject project = presentProfile("Sistema de ventas");

        assertThat(project.getTitle()).isEqualTo("Sistema de ventas");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PROFILE_PENDING);
        assertThat(project.getStorageKey()).isNotEqualTo(firstKey);
        assertThat(file(firstKey)).exists();
        assertThat(file(project.getStorageKey())).exists();
    }

    @Test
    void rejectsANewProfileWhileTheCurrentOneIsPending() {
        presentProfile("Sistema de inventario");

        assertRejected(() -> presentProfile("Otro"), GraduationRejectionReason.PROFILE_NOT_ALLOWED,
                "pendiente de evaluación");
        assertThat(project().getTitle()).isEqualTo("Sistema de inventario");
    }

    @Test
    void rejectsAProfileWithoutOneAttachment() {
        assertRejected(() -> service.submitProfile(studentId, "Sistema de inventario", List.of()),
                GraduationRejectionReason.ATTACHMENT_COUNT, "un solo adjunto");
        assertRejected(() -> service.submitProfile(studentId, "Sistema de inventario",
                List.of(pdf("a.pdf"), pdf("b.pdf"))), GraduationRejectionReason.ATTACHMENT_COUNT, "llegaron 2");
        assertThat(projects.findByGraduationProcessId(processId())).isEmpty();
    }

    @Test
    void rejectsAStudentWithoutAProcessInProgress() {
        jdbc.update("UPDATE graduation_processes SET status = 'CLOSED', closed_at = now(),"
                + " physical_copies_received_at = now() WHERE id = ?", processId());

        assertRejected(() -> presentProfile("Sistema de inventario"), GraduationRejectionReason.PROCESS_NOT_FOUND,
                "No hay un trámite de titulación en curso");
    }

    @Test
    void rejectsAVersionWithoutAProfileWithoutItsApprovalOrWithoutATutor() {
        assertRejected(() -> deliver("Primera versión"), GraduationRejectionReason.VERSION_PREREQUISITES,
                "todavía no presentó un perfil de proyecto");

        presentProfile("Sistema de inventario");
        assertRejected(() -> deliver("Primera versión"), GraduationRejectionReason.VERSION_PREREQUISITES,
                "todavía no tiene visto bueno");
    }

    @Test
    void rejectsAVersionWithoutATutor() {
        presentProfile("Sistema de inventario");
        fixtures.setStatus(project().getId(), ProjectStatus.PROFILE_APPROVED);

        assertRejected(() -> deliver("Primera versión"), GraduationRejectionReason.VERSION_PREREQUISITES,
                "todavía no tiene tutor asignado");
    }

    @Test
    void theFirstVersionIsInReviewByTheTutorWhoIsNotified() {
        UserAccount tutor = readyForVersions();
        clearInvocations(notifier);

        GraduationProject project = deliver("Primera versión");

        assertThat(project.getVersionNumber()).isEqualTo(1);
        assertThat(project.getDocumentKind()).isEqualTo(ProjectDocumentKind.PROJECT_DOCUMENT);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.TUTOR_REVIEW);
        verify(notifier).notify(eq(tutor.getEmail()), eq("Versión del documento del proyecto para revisar"),
                contains("versión 1"));
        verify(notifier).notify(anyString(), anyString(), contains("Primera versión"));
    }

    @Test
    void aCorrectedVersionAfterObservationsTakesTheNextNumber() {
        readyForVersions();
        deliver("Primera versión");
        fixtures.setStatus(project().getId(), ProjectStatus.TUTOR_OBSERVED);
        deliver("Segunda versión");
        fixtures.setStatus(project().getId(), ProjectStatus.TRIBUNAL_OBSERVED);

        GraduationProject project = deliver("Tercera versión");

        assertThat(project.getVersionNumber()).isEqualTo(3);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.TUTOR_REVIEW);
    }

    @Test
    void rejectsAVersionWhileTheCurrentOneIsInReviewOrApprovedWithoutThePredefense() {
        readyForVersions();
        deliver("Primera versión");
        assertRejected(() -> deliver("Otra"), GraduationRejectionReason.VERSION_NOT_ALLOWED, "en revisión del tutor");

        fixtures.setStatus(project().getId(), ProjectStatus.TRIBUNAL_REVIEW);
        assertRejected(() -> deliver("Otra"), GraduationRejectionReason.VERSION_NOT_ALLOWED,
                "en revisión del tribunal");

        fixtures.setStatus(project().getId(), ProjectStatus.APPROVED);
        assertRejected(() -> deliver("Otra"), GraduationRejectionReason.VERSION_NOT_ALLOWED,
                "la predefensa todavía no está aprobada");
        assertThat(project().getVersionNumber()).isEqualTo(1);
    }

    @Test
    void theVersionAfterThePredefenseIsTheFinalDefenseVersion() {
        readyForVersions();
        deliver("Primera versión");
        fixtures.setStatus(project().getId(), ProjectStatus.APPROVED);
        fixtures.approvePredefense(processId());
        clearInvocations(notifier);

        GraduationProject project = deliver("Versión final");

        assertThat(project.getVersionNumber()).isEqualTo(2);
        assertThat(project.getDocumentKind()).isEqualTo(ProjectDocumentKind.FINAL_DEFENSE_VERSION);
        verify(notifier).notify(anyString(), anyString(), contains("versión para la defensa final"));

        fixtures.setStatus(project().getId(), ProjectStatus.APPROVED);
        assertRejected(() -> deliver("Otra"), GraduationRejectionReason.VERSION_NOT_ALLOWED,
                "la versión para la defensa final ya está aprobada");
    }

    @Test
    void theNoticeIsOmittedWhenTheTutorIsDeactivated() {
        UserAccount tutor = readyForVersions();
        jdbc.update("UPDATE user_accounts SET status = 'INACTIVE' WHERE id = ?", tutor.getId());
        clearInvocations(notifier);

        deliver("Primera versión");

        assertThat(project().getVersionNumber()).isEqualTo(1);
        verify(notifier, never()).notify(anyString(), anyString(), anyString());
    }

    @Test
    void viewsTheProjectWithItsHistory() {
        presentProfile("Sistema de inventario");
        fixtures.setStatus(project().getId(), ProjectStatus.PROFILE_OBSERVED);
        presentProfile("Sistema de ventas");
        fixtures.setStatus(project().getId(), ProjectStatus.PROFILE_APPROVED);
        fixtures.assignTutor(processId());
        deliver("Primera versión");

        ProjectView view = service.viewProject(studentId).orElseThrow();

        assertThat(view.project().getTitle()).isEqualTo("Sistema de ventas");
        assertThat(view.history()).extracting(ProjectHistoryItem::documentKind, ProjectHistoryItem::title,
                ProjectHistoryItem::status)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(ProjectDocumentKind.PROJECT_PROFILE, "Sistema de inventario",
                                ProjectStatus.PROFILE_OBSERVED),
                        org.assertj.core.groups.Tuple.tuple(ProjectDocumentKind.PROJECT_PROFILE, "Sistema de ventas",
                                ProjectStatus.PROFILE_APPROVED),
                        org.assertj.core.groups.Tuple.tuple(ProjectDocumentKind.PROJECT_DOCUMENT, "Sistema de ventas",
                                ProjectStatus.TUTOR_REVIEW));
        assertThat(view.history()).allSatisfy(item -> assertThat(file(item.storageKey())).exists());
    }

    @Test
    void viewsNothingBeforeTheFirstProfile() {
        assertThat(service.viewProject(studentId)).isEmpty();
    }

    @Test
    void aRejectionAfterStoringTheFileLeavesNothing() throws IOException {
        long before = storedProjectFiles();

        // The title is longer than its column: the save fails after the file was stored.
        assertThatThrownBy(() -> presentProfile("x".repeat(301))).isNotInstanceOf(GraduationRuleViolationException.class);

        assertThat(projects.findByGraduationProcessId(processId())).isEmpty();
        assertThat(storedProjectFiles()).isEqualTo(before);
        verify(notifier, never()).notify(anyString(), anyString(), anyString());
    }
}
