package grupo03sc.mailapp.graduationprocesses.adapter.in;

import static grupo03sc.mailapp.messaging.MailTestSupport.ADMIN;
import static grupo03sc.mailapp.messaging.MailTestSupport.attachment;
import static grupo03sc.mailapp.messaging.MailTestSupport.attachmentWithDisposition;
import static grupo03sc.mailapp.messaging.MailTestSupport.body;
import static grupo03sc.mailapp.messaging.MailTestSupport.inline;
import static grupo03sc.mailapp.messaging.MailTestSupport.receivedBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.messaging.MailTestSupport;
import grupo03sc.mailapp.messaging.adapter.in.Pop3MailboxPoller;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
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
 * UC-003 end to end: the graduation process commands by email through GreenMail, replies and notices.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GraduationCommandsTests {

    @RegisterExtension
    static GreenMailExtension greenMail = MailTestSupport.greenMail();

    @Autowired
    private Pop3MailboxPoller poller;

    @Autowired
    private UserManagementService users;

    @Autowired
    private UserAccountRepository accounts;

    @Autowired
    private JdbcTemplate jdbc;

    private String secretary;

    /** Messages received before the last command. */
    private List<MimeMessage> since = List.of();

    @BeforeEach
    void createSecretary() {
        secretary = email("secretaria");
        UUID adminId = accounts.findByEmail(ADMIN).orElseThrow().getId();
        users.createAccount(adminId, UserAccountData.of("Secretaria", secretary, Set.of(Role.PROGRAM_SECRETARY)));
    }

    private static String email(String name) {
        return name + "-" + UUID.randomUUID() + "@uagrm.edu.bo";
    }

    private static String registration() {
        return String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000L));
    }

    private static MimeBodyPart pdf(String fileName) throws Exception {
        return attachment(fileName, "application/pdf", ("%PDF " + fileName).getBytes(StandardCharsets.UTF_8));
    }

    private static MimeBodyPart[] fourDocuments() throws Exception {
        return new MimeBodyPart[] {pdf("Carta_Soto.pdf"), pdf("CV.pdf"),
                attachmentWithDisposition("attachment; filename=\"=?UTF-8?Q?C=C3=A9dula.pdf?=\"", "application/pdf",
                        "%PDF cedula".getBytes(StandardCharsets.UTF_8)),
                pdf("boleta-2026.pdf")};
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

    private List<String> noticeSubjectsTo(String recipient) {
        return newMailTo(recipient).stream().map(GraduationCommandsTests::subject)
                .filter(s -> !s.startsWith("Re: ")).toList();
    }

    /** Sends a command with the parts, polls the mailbox once and returns the body of the reply. */
    private String run(String from, String subject, MimeBodyPart... parts) throws Exception {
        since = List.of(greenMail.getReceivedMessages());
        MailTestSupport.sendWithParts(from, subject, parts);
        poller.poll();
        return body(newMailTo(from).stream().filter(m -> subject(m).equals("Re: " + subject))
                .findFirst().orElseThrow());
    }

    private String open(String student, String registration, MimeBodyPart... parts) throws Exception {
        return run(secretary, "AbrirTramite(Juan Soto; " + student + "; " + registration + "; 1234567; 70000000)",
                parts);
    }

    @Test
    void opensAProcessWithAnEncodedNameAndAnInlineImage() throws Exception {
        String student = email("juan");
        String registration = registration();
        MimeBodyPart[] documents = fourDocuments();

        String reply = open(student, registration, documents[0], documents[1], documents[2], documents[3],
                inline("logo.png", "image/png", new byte[] {1}));

        assertThat(reply).isEqualTo("Trámite de titulación abierto: Juan Soto (" + registration + ")."
                + " Se creó la cuenta de usuario del estudiante.");
        assertThat(noticeSubjectsTo(student))
                .containsExactlyInAnyOrder("Cuenta de usuario creada", "Trámite de titulación abierto");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM initial_documents d JOIN graduation_processes g"
                + " ON g.id = d.graduation_process_id JOIN user_accounts a ON a.id = g.student_account_id"
                + " WHERE a.email = ?", Integer.class, student)).isEqualTo(4);
    }

    @Test
    void rejectsMissingDataAndUnrecognizedAttachments() throws Exception {
        assertThat(run(secretary, "AbrirTramite(Juan Soto; " + email("juan") + "; " + registration() + ")",
                fourDocuments())).isEqualTo("Faltan datos: cédula de identidad, teléfono.");

        String student = email("juan");
        MimeBodyPart[] documents = fourDocuments();
        assertThat(open(student, registration(), documents[0], documents[1], documents[2], pdf("foto.png")))
                .startsWith("Adjunto no reconocido: foto.png");
        assertThat(accounts.findByEmail(student)).isEmpty();
        assertThat(noticeSubjectsTo(student)).isEmpty();
    }

    @Test
    void onlyTheSecretaryRunsTheCommands() throws Exception {
        assertThat(run(ADMIN, "CerrarTramite(" + registration() + ")")).contains("permiso");
    }

    @Test
    void replacesADocumentAndClosesTheProcess() throws Exception {
        String student = email("juan");
        String registration = registration();
        open(student, registration, fourDocuments());

        assertThat(run(secretary, "ReemplazarDocumento(" + registration + ")", pdf("Boleta-nueva.pdf")))
                .isEqualTo("Documento inicial reemplazado: boleta de inscripción.");
        assertThat(run(secretary, "CerrarTramite(" + registration + ")"))
                .isEqualTo("El trámite de titulación todavía no tiene la calificación final: está en la etapa de apertura.");

        jdbc.update("UPDATE graduation_processes SET stage = 'FINAL_GRADE_RECORDED' WHERE student_account_id ="
                + " (SELECT id FROM user_accounts WHERE email = ?)", student);
        assertThat(run(secretary, "CerrarTramite(" + registration + ")"))
                .isEqualTo("Trámite de titulación cerrado: Juan Soto (" + registration + ").");
        assertThat(noticeSubjectsTo(student)).containsExactly("Trámite de titulación cerrado");
    }
}
