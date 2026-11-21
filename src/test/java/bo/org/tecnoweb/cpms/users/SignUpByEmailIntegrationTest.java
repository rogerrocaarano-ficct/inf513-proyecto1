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
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * UC-001 basic flow end to end: commands by email (GreenMail), accounts in PostgreSQL, replies and
 * notices by email.
 */
@SpringBootTest(properties = {
        "cpms.users.admin-email=admin@cpms.test",
        "cpms.mail.pop3.port=3110",
        "cpms.mail.pop3.username=sistema",
        "cpms.mail.pop3.password=secret",
        "spring.mail.port=3025"})
@Import(TestcontainersConfiguration.class)
class SignUpByEmailIntegrationTest {

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

    private static String signUp(String names, String email, String role, String code) {
        return "usuarios.crearCuenta[" + names + "; Pérez; ; 1234567; " + code + "; " + email + "; 71234567; "
                + role + "]";
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

    private List<Map<String, Object>> account(String email) {
        return jdbc.queryForList("SELECT role, first_name, last_name, uagrm_code FROM user_account WHERE email_address = ?",
                email);
    }

    @Test
    void administratorCreatesADirectorWhoCreatesAProfessorAndAStudent() throws Exception {
        command(ADMIN, signUp("carla", "director@uagrm.bo", "Director", "100"));
        command("Director@UAGRM.bo", signUp("luis", "luis@uagrm.bo", "docente", "200"));
        command("director@uagrm.bo", signUp("ana maría", "ana@uagrm.bo", "estudiante", "218001234"));

        assertThat(account("director@uagrm.bo")).singleElement().satisfies(row -> {
            assertThat(row).containsEntry("role", "DIRECTOR").containsEntry("first_name", "Carla");
        });
        assertThat(account("luis@uagrm.bo")).singleElement()
                .satisfies(row -> assertThat(row).containsEntry("role", "PROFESSOR"));
        assertThat(account("ana@uagrm.bo")).singleElement()
                .satisfies(row -> assertThat(row).containsEntry("role", "STUDENT")
                        .containsEntry("first_name", "Ana María").containsEntry("uagrm_code", "218001234"));

        assertThat(mailTo(ADMIN)).singleElement().satisfies(reply -> {
            assertThat(reply.getSubject()).isEqualTo("Cuenta creada: Carla Pérez");
            assertThat(body(reply)).contains("director@uagrm.bo").contains("rol director");
        });
        assertThat(mailTo("director@uagrm.bo")).hasSize(3)
                .anySatisfy(notice -> assertThat(body(notice)).contains("con rol director"))
                .anySatisfy(reply -> assertThat(reply.getSubject()).isEqualTo("Cuenta creada: Luis Pérez"))
                .anySatisfy(reply -> assertThat(reply.getSubject()).isEqualTo("Cuenta creada: Ana María Pérez"));
        assertThat(mailTo("luis@uagrm.bo")).singleElement().satisfies(notice -> {
            assertThat(notice.getSubject()).isEqualTo("Su cuenta en el sistema de trámites de titulación");
            assertThat(body(notice)).contains("Carla Pérez creó su cuenta").contains("rol docente");
        });
        assertThat(mailTo("ana@uagrm.bo")).singleElement();
    }

    @Test
    void professorCannotCreateAccounts() throws Exception {
        command(ADMIN, signUp("luis", "luis@uagrm.bo", "docente", "200"));
        greenMail.purgeEmailFromAllMailboxes();

        command("luis@uagrm.bo", signUp("ana", "ana@uagrm.bo", "estudiante", "218001234"));

        assertThat(account("ana@uagrm.bo")).isEmpty();
        assertThat(mailTo("luis@uagrm.bo")).singleElement()
                .satisfies(reply -> assertThat(body(reply)).contains("no tiene permiso para crear cuentas de estudiante"));
        assertThat(mailTo("ana@uagrm.bo")).isEmpty();
    }

    @Test
    void unknownSenderIsToldItIsNotRegistered() throws Exception {
        command("extrano@gmail.com", signUp("ana", "ana@uagrm.bo", "estudiante", "218001234"));

        assertThat(account("ana@uagrm.bo")).isEmpty();
        assertThat(mailTo("extrano@gmail.com")).singleElement()
                .satisfies(reply -> assertThat(body(reply)).contains("no está registrada en el sistema"));
        assertThat(mailTo("ana@uagrm.bo")).isEmpty();
    }

    @Test
    void invalidDataIsAnsweredFieldByFieldWithoutNoticeToTheNewAccount() throws Exception {
        command(ADMIN, "usuarios.crearCuenta[Ana; ; ; 1234567-1A; 218001234; ana@uagrm.bo; 7123-4567; estudiante]");

        assertThat(account("ana@uagrm.bo")).isEmpty();
        assertThat(mailTo(ADMIN)).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("- primer apellido: Es obligatorio.")
                .contains("- CI: Debe tener solo dígitos, hasta 7.")
                .contains("- teléfono: Debe tener solo dígitos, hasta 8."));
        assertThat(mailTo("ana@uagrm.bo")).isEmpty();
    }

    @Test
    void repeatedEmailIsRejected() throws Exception {
        command(ADMIN, signUp("ana", "ana@uagrm.bo", "estudiante", "218001234"));
        greenMail.purgeEmailFromAllMailboxes();

        command(ADMIN, signUp("otra", "ANA@uagrm.bo", "estudiante", "218009999"));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_account WHERE email_address = 'ana@uagrm.bo'",
                Integer.class)).isEqualTo(1);
        assertThat(mailTo(ADMIN)).singleElement()
                .satisfies(reply -> assertThat(body(reply)).contains("- correo: Ya pertenece a otra cuenta."));
        assertThat(mailTo("ana@uagrm.bo")).isEmpty();
    }
}
