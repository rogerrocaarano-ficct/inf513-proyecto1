package bo.org.tecnoweb.cpms.users;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.mail.application.InboxPollerService;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Help command end to end (FR-066): {@code ayuda[]} by email (GreenMail), accounts in PostgreSQL, the
 * commands of the sender's role or the rejection by email.
 */
@SpringBootTest(properties = {
        "cpms.users.admin-email=admin@cpms.test",
        "cpms.mail.pop3.port=3110",
        "cpms.mail.pop3.username=sistema",
        "cpms.mail.pop3.password=secret",
        "spring.mail.port=3025"})
@Import(TestcontainersConfiguration.class)
class HelpByEmailIntegrationTest {

    private static final String SYSTEM = "sistema@cpms.test";
    private static final String ADMIN = "admin@cpms.test";

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_POP3);

    @Autowired
    private InboxPollerService service;

    @Autowired
    private JdbcTemplate jdbc;

    private GreenMailUser system;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM received_email");
        jdbc.update("DELETE FROM user_account WHERE email_address <> ?", ADMIN);
        system = greenMail.setUser(SYSTEM, "sistema", "secret");
        account("secretaria@uagrm.bo", "ADMINISTRATIVE");
        account("luis@uagrm.bo", "PROFESSOR");
        account("ana@uagrm.bo", "STUDENT");
    }

    private void account(String email, String role) {
        jdbc.update("INSERT INTO user_account (id, email_address, role, first_name, last_name) "
                + "VALUES (gen_random_uuid(), ?, ?, 'Nombre', 'Apellido')", email, role);
    }

    private void command(String from, String subject) throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(SYSTEM));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setText("", StandardCharsets.UTF_8.name());
        system.deliver(message);
        service.pollAndProcess();
    }

    private List<MimeMessage> mailTo(String address) {
        return Arrays.stream(greenMail.getReceivedMessages()).filter(m -> {
            try {
                Address[] to = m.getRecipients(Message.RecipientType.TO);
                return to != null && Arrays.stream(to).anyMatch(a -> a.toString().equalsIgnoreCase(address));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }).toList();
    }

    /** Decoded text body (the raw body is quoted-printable). */
    private static String body(MimeMessage message) {
        try {
            return String.valueOf(message.getContent());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void studentGetsOnlyItsCommands() throws Exception {
        command("Ana@uagrm.bo", "ayuda[]");

        assertThat(mailTo("ana@uagrm.bo")).singleElement().satisfies(reply -> {
            assertThat(reply.getSubject()).isEqualTo("Comandos que puede usar");
            assertThat(body(reply).replace("\r\n", "\n").strip()).isEqualTo("""
                    Comandos que puede usar:

                    ayuda[]
                      Ver los comandos que puede usar.

                    proyectos.registrarPerfil[titulo]
                      Presentar el perfil del proyecto o una versión nueva.
                      Adjuntos: 1 PDF con el perfil.
                    """.strip());
        });
    }

    @Test
    void administrativeSeesWhatItMayDoWithPartialCommands() throws Exception {
        command("secretaria@uagrm.bo", "AYUDA[]");

        assertThat(mailTo("secretaria@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("tramites.abrirTramite[registro]")
                .contains("tramites.reemplazarDocumento[referencia]")
                .contains("Puede: crear cuentas de estudiante.")
                .contains("Puede: listar cuentas de estudiante.")
                .doesNotContain("usuarios.desactivar")
                .doesNotContain("usuarios.activar")
                .doesNotContain("proyectos."));
    }

    @Test
    void professorIsToldItHasNoCommands() throws Exception {
        command("luis@uagrm.bo", "ayuda[]");

        assertThat(mailTo("luis@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .startsWith("Por ahora su cuenta no tiene comandos disponibles.")
                .contains("ayuda[]")
                .doesNotContain("usuarios.")
                .doesNotContain("proyectos.")
                .doesNotContain("tramites."));
    }

    @Test
    void unknownSenderIsToldItIsNotRegistered() throws Exception {
        command("extrano@gmail.com", "ayuda[]");

        assertThat(mailTo("extrano@gmail.com")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("no está registrada en el sistema")
                .doesNotContain("Comandos que puede usar"));
    }

    @Test
    void unknownCommandPointsToTheHelpCommandThatAnswers() throws Exception {
        command("ana@uagrm.bo", "usuarios.borrarTodo[]");
        assertThat(mailTo("ana@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("no existe. Envíe ayuda[] para ver los comandos que puede usar.")
                .doesNotContain("usuarios.listar"));

        command("ana@uagrm.bo", "ayuda[]");

        assertThat(mailTo("ana@uagrm.bo")).hasSize(2)
                .anySatisfy(reply -> assertThat(reply.getSubject()).isEqualTo("Comandos que puede usar"));
    }

    @Test
    void helpWithArgumentsIsRejected() throws Exception {
        command("ana@uagrm.bo", "ayuda[usuarios.listar]");

        assertThat(mailTo("ana@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("requiere 0 argumentos y se recibieron 1")
                .doesNotContain("Comandos que puede usar"));
    }
}
