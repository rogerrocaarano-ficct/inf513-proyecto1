package bo.org.tecnoweb.cpms.procedures;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.mail.application.InboxPollerService;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
        "cpms.users.admin-email=admin@procedures.test",
        "cpms.mail.pop3.port=3110",
        "cpms.mail.pop3.username=sistema",
        "cpms.mail.pop3.password=secret",
        "spring.mail.port=3025"})
@Import(TestcontainersConfiguration.class)
class DegreeProcedureByEmailIntegrationTest {
    private static final String SYSTEM = "sistema@procedures.test";
    private static final String ADMIN = "admin@procedures.test";
    private static final String SECRETARY = "secretaria@procedures.test";
    private static final String STUDENT = "ana@procedures.test";
    private static final String OPEN = "tramites.abrirTramite[Ana; Pérez; ; 1234; 123456; " + STUDENT + "; 7123456]";
    private static final String[] DOCUMENTS = {"carta.pdf", "cv.pdf", "ci.pdf", "boleta.pdf"};

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_POP3);

    @Autowired
    private InboxPollerService poller;

    @Autowired
    private JdbcTemplate jdbc;

    private GreenMailUser system;

    @BeforeEach
    void setup() throws Exception {
        jdbc.update("DELETE FROM initial_document");
        jdbc.update("DELETE FROM degree_procedure");
        jdbc.update("DELETE FROM received_email");
        jdbc.update("DELETE FROM user_account WHERE email_address <> ?", ADMIN);
        system = greenMail.setUser(SYSTEM, "sistema", "secret");
        command(ADMIN, "usuarios.crearCuenta[Laura; Rojas; ; ; ; " + SECRETARY + "; ; administrativo]");
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void opensWithNewStudentAndReplacesOnlySelectedDocument() throws Exception {
        command(SECRETARY, OPEN, DOCUMENTS);

        long reference = jdbc.queryForObject("SELECT reference FROM degree_procedure", Long.class);
        String formatted = "TR-" + String.format(Locale.ROOT, "%03d", reference);
        assertThat(subjectsTo(SECRETARY)).containsExactly("Trámite abierto: " + formatted);
        assertThat(subjectsTo(STUDENT)).containsExactlyInAnyOrder(
                "Su cuenta en el sistema de trámites de titulación", "Apertura de trámite: " + formatted);

        command(SECRETARY, "tramites.reemplazarDocumento[" + formatted + "]", "carta_corregida.pdf");
        assertThat(filenames()).containsExactlyInAnyOrder("carta_corregida.pdf", "cv.pdf", "ci.pdf", "boleta.pdf");
        assertThat(jdbc.queryForObject("SELECT content FROM initial_document WHERE document_type = 'LETTER'", byte[].class))
                .containsExactly("%PDF-1.7\ncarta_corregida.pdf".getBytes(StandardCharsets.US_ASCII));
        assertThat(subjectsTo(SECRETARY)).contains("Documento inicial reemplazado: " + formatted);

        jdbc.update("UPDATE degree_procedure SET closed_at = now() WHERE reference = ?", reference);
        command(SECRETARY, "tramites.reemplazarDocumento[" + formatted + "]", "carta_tardia.pdf");
        assertThat(filenames()).contains("carta_corregida.pdf").doesNotContain("carta_tardia.pdf");
    }

    @Test
    void linksExistingStudentAndRejectsSecondOpening() throws Exception {
        command(SECRETARY, "usuarios.crearCuenta[Ana; Pérez; ; 1234; 123456; " + STUDENT + "; 7123456; estudiante]");
        greenMail.purgeEmailFromAllMailboxes();

        command(SECRETARY, OPEN, DOCUMENTS);
        command(SECRETARY, OPEN, DOCUMENTS);

        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM degree_procedure JOIN user_account ON student_id = id
                WHERE email_address = ?
                """, Integer.class, STUDENT)).isEqualTo(1);
        assertThat(subjectsTo(STUDENT)).singleElement()
                .satisfies(subject -> assertThat(subject).startsWith("Apertura de trámite: TR-"));
    }

    @Test
    void rejectsUnauthorizedSenderAndDuplicateDocumentWithoutCreatingStudent() throws Exception {
        command(ADMIN, OPEN, DOCUMENTS);
        command(SECRETARY, OPEN, "carta.pdf", "cv.pdf", "ci.pdf", "ci_copia.pdf");

        assertThat(filenames()).isEmpty();
        assertThat(jdbc.queryForList("SELECT id FROM user_account WHERE email_address = ?", STUDENT)).isEmpty();
        assertThat(subjectsTo(STUDENT)).isEmpty();
    }

    @Test
    void rejectsIncompatibleIdentifiersAndDisabledAccount() throws Exception {
        command(SECRETARY, "usuarios.crearCuenta[Ana; Pérez; ; 1234; 123456; " + STUDENT + "; 7123456; estudiante]");
        jdbc.update("UPDATE user_account SET active = false WHERE email_address = ?", STUDENT);
        greenMail.purgeEmailFromAllMailboxes();

        command(SECRETARY, OPEN.replace(STUDENT, "otra@procedures.test"), DOCUMENTS);
        command(SECRETARY, OPEN, DOCUMENTS);

        assertThat(jdbc.queryForList("SELECT reference FROM degree_procedure")).isEmpty();
        assertThat(jdbc.queryForList("SELECT id FROM user_account WHERE email_address = ?", "otra@procedures.test"))
                .isEmpty();
        assertThat(subjectsTo(SECRETARY)).hasSize(2);
        assertThat(subjectsTo(STUDENT)).isEmpty();
    }

    private List<String> filenames() {
        return jdbc.queryForList("SELECT filename FROM initial_document", String.class);
    }

    private List<String> subjectsTo(String address) throws Exception {
        List<String> subjects = new ArrayList<>();
        for (MimeMessage message : greenMail.getReceivedMessages()) {
            for (var recipient : message.getRecipients(Message.RecipientType.TO)) {
                if (address.equals(recipient.toString())) {
                    subjects.add(message.getSubject());
                }
            }
        }
        return subjects;
    }

    private void command(String from, String subject, String... filenames) throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(SYSTEM));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        MimeMultipart parts = new MimeMultipart();
        MimeBodyPart body = new MimeBodyPart();
        body.setText("Prueba UC-002");
        parts.addBodyPart(body);
        for (String filename : filenames) {
            MimeBodyPart attachment = new MimeBodyPart();
            byte[] bytes = ("%PDF-1.7\n" + filename).getBytes(StandardCharsets.US_ASCII);
            attachment.setDataHandler(new DataHandler(new ByteArrayDataSource(bytes, "application/pdf")));
            attachment.setFileName(filename);
            attachment.setHeader("Content-Transfer-Encoding", "base64");
            parts.addBodyPart(attachment);
        }
        message.setContent(parts);
        system.deliver(message);
        poller.pollAndProcess();
    }
}
