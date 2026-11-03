package grupo03sc.mailapp.graduationprocesses.adapter.in;

import static grupo03sc.mailapp.messaging.MailTestSupport.ADMIN;
import static grupo03sc.mailapp.messaging.MailTestSupport.attachment;
import static grupo03sc.mailapp.messaging.MailTestSupport.body;
import static grupo03sc.mailapp.messaging.MailTestSupport.receivedBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.graduationprocesses.application.GraduationProcessService;
import grupo03sc.mailapp.graduationprocesses.application.GraduationProjectFixtures;
import grupo03sc.mailapp.graduationprocesses.application.GraduationProjectRepository;
import grupo03sc.mailapp.graduationprocesses.application.GraduationTestData;
import grupo03sc.mailapp.graduationprocesses.application.OpenedProcess;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProject;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectStatus;
import grupo03sc.mailapp.messaging.MailTestSupport;
import grupo03sc.mailapp.messaging.adapter.in.Pop3MailboxPoller;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * UC-004 end to end: the graduation project commands by email through GreenMail, replies and notices
 * (spec graduation-projects).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GraduationProjectCommandsTests {

    @RegisterExtension
    static GreenMailExtension greenMail = MailTestSupport.greenMail();

    @Autowired
    private Pop3MailboxPoller poller;

    @Autowired
    private GraduationProcessService processService;

    @Autowired
    private GraduationProjectRepository projects;

    @Autowired
    private UserManagementService users;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private JdbcTemplate jdbc;

    private GraduationProjectFixtures fixtures;
    private OpenedProcess opened;
    private String student;

    /** Messages received before the last command. */
    private List<MimeMessage> since = List.of();

    @BeforeEach
    void openProcess() {
        fixtures = new GraduationProjectFixtures(jdbc, users, accounts);
        UUID adminId = accounts.findByEmail(ADMIN).orElseThrow().getId();
        UUID secretaryId = users.createAccount(adminId, UserAccountData.of("Secretaria",
                GraduationTestData.email("secretaria"), Set.of(Role.PROGRAM_SECRETARY))).getId();
        student = GraduationTestData.email("juan");
        opened = processService.openProcess(secretaryId,
                GraduationTestData.student(student, GraduationTestData.registration()),
                GraduationTestData.fourDocuments());
    }

    private static MimeBodyPart pdf(String fileName) throws Exception {
        return attachment(fileName, "application/pdf", ("%PDF " + fileName).getBytes(StandardCharsets.UTF_8));
    }

    private static String subject(MimeMessage message) {
        try {
            return message.getSubject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private List<MimeMessage> newMailTo(String recipient) {
        return receivedBy(greenMail, recipient).stream().filter(m -> !since.contains(m)).toList();
    }

    private List<MimeMessage> noticesTo(String recipient) {
        return newMailTo(recipient).stream().filter(m -> !subject(m).startsWith("Re: ")).toList();
    }

    /** Sends a command with the parts, polls the mailbox once and returns the body of the reply. */
    private String run(String from, String subject, MimeBodyPart... parts) throws Exception {
        since = List.of(greenMail.getReceivedMessages());
        MailTestSupport.sendWithParts(from, subject, parts);
        poller.poll();
        return body(newMailTo(from).stream().filter(m -> subject(m).equals("Re: " + subject))
                .findFirst().orElseThrow());
    }

    private GraduationProject project() {
        return projects.findByGraduationProcessId(opened.process().getId()).orElseThrow();
    }

    @Test
    void onlyAStudentCanRunTheCommands() throws Exception {
        String professor = GraduationTestData.email("docente");
        users.createAccount(accounts.findByEmail(ADMIN).orElseThrow().getId(),
                UserAccountData.of("Docente", professor, Set.of(Role.PROFESSOR)));

        assertThat(run(professor, "ConsultarProyecto()")).contains("No tiene permiso");
    }

    @Test
    void rejectsMissingDataAndExtraArguments() throws Exception {
        assertThat(run(student, "PresentarPerfil()", pdf("perfil.pdf")))
                .contains("Faltan datos").contains("título del proyecto");
        assertThat(run(student, "EntregarVersion()", pdf("documento.pdf")))
                .contains("Faltan datos").contains("comentario general");
        assertThat(run(student, "ConsultarProyecto(algo)")).contains("Dato inválido");
        assertThat(projects.findByGraduationProcessId(opened.process().getId())).isEmpty();
    }

    @Test
    void presentsAProfileAndNotifiesTheThesisWorkshopProfessor() throws Exception {
        UserAccount professor = fixtures.thesisWorkshopProfessor();

        String reply = run(student, "PresentarPerfil(Sistema de inventario)", pdf("perfil.pdf"));

        assertThat(reply).isEqualTo(
                "Perfil de proyecto presentado: Sistema de inventario. Se avisó al docente de Taller de Grado.");
        assertThat(noticesTo(professor.getEmail())).singleElement().satisfies(notice -> {
            assertThat(subject(notice)).isEqualTo("Perfil de proyecto para evaluar");
            assertThat(body(notice)).contains("Juan Soto").contains("Sistema de inventario");
        });
        assertThat(project().getStatus()).isEqualTo(ProjectStatus.PROFILE_PENDING);
    }

    @Test
    void deliversAVersionAndNotifiesTheTutor() throws Exception {
        run(student, "PresentarPerfil(Sistema de inventario)", pdf("perfil.pdf"));
        fixtures.setStatus(project().getId(), ProjectStatus.PROFILE_APPROVED);
        UserAccount tutor = fixtures.assignTutor(opened.process().getId());

        String reply = run(student, "EntregarVersion(Primera versión)", pdf("documento.pdf"));

        assertThat(reply).isEqualTo("Versión 1 del documento del proyecto entregada.");
        assertThat(noticesTo(tutor.getEmail())).singleElement().satisfies(notice -> {
            assertThat(subject(notice)).isEqualTo("Versión del documento del proyecto para revisar");
            assertThat(body(notice)).contains("versión 1").contains("Primera versión");
        });
    }

    @Test
    void viewsTheProjectWithItsHistory() throws Exception {
        assertThat(run(student, "ConsultarProyecto()")).isEqualTo("Todavía no presentó un perfil de proyecto.");

        run(student, "PresentarPerfil(Sistema de inventario)", pdf("perfil.pdf"));
        fixtures.setStatus(project().getId(), ProjectStatus.PROFILE_OBSERVED);
        run(student, "PresentarPerfil(Sistema de ventas)", pdf("perfil2.pdf"));

        String reply = run(student, "ConsultarProyecto()");

        assertThat(reply).contains("Proyecto de grado: Sistema de ventas")
                .contains("Documento vigente:").contains("perfil de proyecto: pendiente de evaluación")
                .contains("Historial").contains("perfil de proyecto: observado");
    }

    @Test
    void rejectsAWrongAttachment() throws Exception {
        assertThat(run(student, "PresentarPerfil(Sistema de inventario)"))
                .contains("Cantidad de adjuntos incorrecta");
        assertThat(run(student, "PresentarPerfil(Sistema de inventario)",
                attachment("perfil.docx", "application/octet-stream", new byte[] {1})))
                .contains("Documento no válido").contains("debe ser un PDF");
    }

    @Test
    void acceptsAPdfOfExactly25Megabytes() throws Exception {
        String reply = run(student, "PresentarPerfil(Sistema de inventario)",
                attachment("perfil.pdf", "application/pdf", new byte[26_214_400]));

        assertThat(reply).startsWith("Perfil de proyecto presentado: Sistema de inventario.");
    }

    @Test
    void rejectsAPdfOverTheMaximum() throws Exception {
        String reply = run(student, "PresentarPerfil(Sistema de inventario)",
                attachment("perfil.pdf", "application/pdf", new byte[26_214_401]));

        assertThat(reply).contains("Documento no válido").contains("25 MB");
        assertThat(projects.findByGraduationProcessId(opened.process().getId())).isEmpty();
    }
}
