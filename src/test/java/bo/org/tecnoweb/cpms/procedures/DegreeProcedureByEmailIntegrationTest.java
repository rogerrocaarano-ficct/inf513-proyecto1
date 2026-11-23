package bo.org.tecnoweb.cpms.procedures;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.mail.application.InboxPollerService;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** Exercises UC-002 and UC-003 through the real email channel. */
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
    private static final String WORKSHOP = "taller@procedures.test";
    private static final String DIRECTOR = "direccion@procedures.test";
    private static final String TUTOR = "docente@procedures.test";
    private static final String PROFILE = "proyectos.registrarPerfil[Redes comunitarias]";
    private static final String OPEN = "tramites.abrirTramite[Ana; Pérez; ; 1234; 123456; " + STUDENT + "; 7123456]";
    private static final String[] DOCUMENTS = {"carta.pdf", "cv.pdf", "ci.pdf", "boleta.pdf"};

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_POP3);

    @Autowired
    private InboxPollerService poller;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StoredFileService storedFiles;

    private GreenMailUser system;

    @BeforeEach
    void setup() throws Exception {
        jdbc.update("DELETE FROM degree_project");
        jdbc.update("DELETE FROM initial_document");
        jdbc.update("DELETE FROM degree_procedure");
        jdbc.update("DELETE FROM received_email");
        jdbc.update("DELETE FROM stored_file");
        jdbc.update("DELETE FROM user_account WHERE email_address <> ?", ADMIN);
        system = greenMail.setUser(SYSTEM, "sistema", "secret");
        command(ADMIN, "usuarios.crearCuenta[Laura; Rojas; ; ; ; " + SECRETARY + "; ; administrativo]");
        greenMail.purgeEmailFromAllMailboxes();
    }

    /** Registers one profile, then assigns a tutor only after FR-005 is satisfied. */
    @Test
    void registersProfileAndAssignsTutorAfterApproval() throws Exception {
        command(ADMIN, "usuarios.crearCuenta[Carla; Rojas; ; ; ; " + DIRECTOR + "; ; director]");
        command(DIRECTOR, "usuarios.crearCuenta[Luis; Rojas; ; ; ; " + TUTOR + "; ; docente]");
        command(SECRETARY, OPEN, DOCUMENTS);
        long reference = jdbc.queryForObject("SELECT reference FROM degree_procedure", Long.class);
        String label = DegreeProcedure.referenceLabel(reference);
        String assign = "proyectos.asignarTutor[" + label + "; " + TUTOR + "]";
        greenMail.purgeEmailFromAllMailboxes();

        command(STUDENT, PROFILE, "perfil.pdf");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM degree_project", Integer.class)).isZero();
        assertThat(subjectsTo(STUDENT)).hasSize(1);

        command(DIRECTOR, "usuarios.crearCuenta[Rosa; Rojas; ; ; ; " + WORKSHOP + "; ; docente_taller]");
        greenMail.purgeEmailFromAllMailboxes();
        command(STUDENT, PROFILE, "perfil.pdf");
        command(STUDENT, PROFILE, "perfil.pdf");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM degree_project", Integer.class)).isEqualTo(1);
        assertThat(subjectsTo(WORKSHOP)).containsExactly("Perfil de proyecto: " + label);
        var notice = java.util.Arrays.stream(greenMail.getReceivedMessages())
                .filter(message -> hasSubject(message, "Perfil de proyecto: " + label)).findFirst().orElseThrow();
        var parts = (MimeMultipart) notice.getContent();
        assertThat(parts.getBodyPart(1).getFileName()).isEqualTo("perfil.pdf");
        assertThat(parts.getBodyPart(1).getContentType()).startsWith("application/pdf");
        // The test PDF is plain ASCII, so SMTP may send it as 7bit text and turn "\n" into "\r\n".
        assertThat(new String(parts.getBodyPart(1).getInputStream().readAllBytes(), StandardCharsets.US_ASCII))
                .isEqualToIgnoringNewLines(new String(ProcedureFixtures.pdfContent("perfil.pdf"),
                        StandardCharsets.US_ASCII));

        command(DIRECTOR, assign);
        assertThat(jdbc.queryForList("SELECT tutor_id FROM degree_project WHERE tutor_id IS NOT NULL")).isEmpty();
        jdbc.update("UPDATE degree_project SET profile_approved = true");
        command(DIRECTOR, assign);
        command(DIRECTOR, assign);
        assertThat(jdbc.queryForObject("""
                SELECT email_address FROM degree_project JOIN user_account ON tutor_id = user_account.id
                """, String.class)).isEqualTo(TUTOR);
        assertThat(subjectsTo(STUDENT).stream().filter(subject -> subject.equals("Tutor asignado: " + label)))
                .hasSize(1);
    }

    private boolean hasSubject(MimeMessage message, String subject) {
        try {
            return subject.equals(message.getSubject());
        } catch (jakarta.mail.MessagingException caught) {
            throw new AssertionError(caught);
        }
    }

    @Test
    void opensWithNewStudentAndReplacesOnlySelectedDocument() throws Exception {
        command(SECRETARY, OPEN, DOCUMENTS);

        long reference = jdbc.queryForObject("SELECT reference FROM degree_procedure", Long.class);
        String formatted = "TR-" + String.format(Locale.ROOT, "%03d", reference);
        assertThat(subjectsTo(SECRETARY)).containsExactly("Trámite abierto: " + formatted);
        assertThat(subjectsTo(STUDENT)).containsExactlyInAnyOrder(
                "Su cuenta en el sistema de trámites de titulación", "Apertura de trámite: " + formatted);

        // The documents adopt the stored files of the attachments: one file on disk per attachment.
        assertThat(jdbc.queryForObject("SELECT count(*) FROM stored_file", Integer.class)).isEqualTo(4);
        assertThat(jdbc.queryForList("""
                SELECT d.stored_file_id FROM initial_document d
                JOIN received_email_attachment a ON a.stored_file_id = d.stored_file_id
                """)).hasSize(4);
        UUID original = letter();

        command(SECRETARY, "tramites.reemplazarDocumento[" + formatted + "]", "carta_corregida.pdf");
        assertThat(filenames()).containsExactlyInAnyOrder("carta_corregida.pdf", "cv.pdf", "ci.pdf", "boleta.pdf");
        UUID corrected = letter();
        assertThat(storedFiles.readContent(corrected))
                .containsExactly("%PDF-1.7\ncarta_corregida.pdf".getBytes(StandardCharsets.US_ASCII));
        assertThat(storedFiles.getHistory(corrected)).extracting(StoredFile::getId)
                .containsExactly(original, corrected);
        assertThat(storedFiles.readContent(original))
                .containsExactly("%PDF-1.7\ncarta.pdf".getBytes(StandardCharsets.US_ASCII));
        assertThat(subjectsTo(SECRETARY)).contains("Documento inicial reemplazado: " + formatted);

        jdbc.update("UPDATE degree_procedure SET closed_at = now() WHERE reference = ?", reference);
        command(SECRETARY, "tramites.reemplazarDocumento[" + formatted + "]", "carta_tardia.pdf");
        assertThat(filenames()).contains("carta_corregida.pdf").doesNotContain("carta_tardia.pdf");
        assertThat(storedFiles.getHistory(corrected)).extracting(StoredFile::getId)
                .containsExactly(original, corrected);
    }

    private UUID letter() {
        return jdbc.queryForObject("SELECT stored_file_id FROM initial_document WHERE document_type = 'LETTER'",
                UUID.class);
    }

    @Test
    void linksExistingStudentAndRejectsSecondOpening() throws Exception {
        command(SECRETARY, "usuarios.crearCuenta[Ana; Pérez; ; 1234; 123456; " + STUDENT + "; 7123456; estudiante]");
        greenMail.purgeEmailFromAllMailboxes();

        command(SECRETARY, OPEN, DOCUMENTS);
        command(SECRETARY, OPEN, DOCUMENTS);

        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM degree_procedure JOIN user_account ON student_id = user_account.id
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
        return jdbc.queryForList("""
                SELECT original_name FROM initial_document JOIN stored_file ON stored_file.id = stored_file_id
                """, String.class);
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
