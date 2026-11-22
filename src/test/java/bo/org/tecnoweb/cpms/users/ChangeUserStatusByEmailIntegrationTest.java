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
 * UC-001 A2 end to end: {@code usuarios.desactivar} and {@code usuarios.activar} by email (GreenMail),
 * accounts in PostgreSQL, the replies and notices by email.
 */
@SpringBootTest(properties = {
        "cpms.users.admin-email=admin@cpms.test",
        "cpms.mail.pop3.port=3110",
        "cpms.mail.pop3.username=sistema",
        "cpms.mail.pop3.password=secret",
        "spring.mail.port=3025"})
@Import(TestcontainersConfiguration.class)
class ChangeUserStatusByEmailIntegrationTest {

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
        jdbc.update("UPDATE user_account SET active = TRUE WHERE email_address = ?", ADMIN);
        system = greenMail.setUser(SYSTEM, "sistema", "secret");
        account("director@uagrm.bo", "DIRECTOR", "Carla", "Suarez");
        account("otra.directora@uagrm.bo", "DIRECTOR", "Rosa", "Vaca");
        account("secretaria@uagrm.bo", "ADMINISTRATIVE", "Rosa", "Flores");
        account("luis@uagrm.bo", "PROFESSOR", "Luis", "Rojas");
        account("ana@uagrm.bo", "STUDENT", "Ana", "Perez");
    }

    private void account(String email, String role, String firstName, String lastName) {
        jdbc.update("INSERT INTO user_account (id, email_address, role, first_name, last_name) "
                + "VALUES (gen_random_uuid(), ?, ?, ?, ?)", email, role, firstName, lastName);
    }

    private boolean active(String email) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT active FROM user_account WHERE email_address = ?", Boolean.class, email));
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

    private static String body(MimeMessage message) {
        try {
            return String.valueOf(message.getContent());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String subject(MimeMessage message) {
        try {
            return message.getSubject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void directorDisablesAndEnablesAProfessorAndTheProfessorIsBlockedMeanwhile() throws Exception {
        command("director@uagrm.bo", "usuarios.desactivar[Luis@UAGRM.bo]");

        assertThat(active("luis@uagrm.bo")).isFalse();
        assertThat(mailTo("director@uagrm.bo")).singleElement()
                .satisfies(reply -> assertThat(subject(reply)).isEqualTo("Cuenta desactivada: Luis Rojas"));
        assertThat(mailTo("luis@uagrm.bo")).singleElement().satisfies(notice -> {
            assertThat(subject(notice)).isEqualTo("Su cuenta en el sistema de trámites de titulación fue desactivada");
            assertThat(body(notice)).contains("Carla Suarez desactivó su cuenta");
        });

        command("luis@uagrm.bo", "usuarios.listar[estudiante]");
        assertThat(mailTo("luis@uagrm.bo")).hasSize(2)
                .anySatisfy(reply -> assertThat(body(reply))
                        .contains("La cuenta luis@uagrm.bo está desactivada. Comuníquese con la Dirección de Carrera."));

        command("director@uagrm.bo", "usuarios.activar[luis@uagrm.bo]");
        assertThat(active("luis@uagrm.bo")).isTrue();
        assertThat(mailTo("director@uagrm.bo")).anySatisfy(
                reply -> assertThat(subject(reply)).isEqualTo("Cuenta activada: Luis Rojas"));
        assertThat(mailTo("luis@uagrm.bo")).anySatisfy(notice -> assertThat(subject(notice))
                .isEqualTo("Su cuenta en el sistema de trámites de titulación fue activada"));
    }

    @Test
    void directorMayNotDisableAnotherDirector() throws Exception {
        command("director@uagrm.bo", "usuarios.desactivar[otra.directora@uagrm.bo]");

        assertThat(active("otra.directora@uagrm.bo")).isTrue();
        assertThat(mailTo("director@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("no tiene permiso para cambiar el estado de cuentas de director"));
        assertThat(mailTo("otra.directora@uagrm.bo")).isEmpty();
    }

    @Test
    void administrativeMayNotDisableAStudent() throws Exception {
        command("secretaria@uagrm.bo", "usuarios.desactivar[ana@uagrm.bo]");

        assertThat(active("ana@uagrm.bo")).isTrue();
        assertThat(mailTo("secretaria@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("no tiene permiso para cambiar el estado de cuentas de estudiante"));
    }

    @Test
    void unknownAccountIsRejected() throws Exception {
        command("director@uagrm.bo", "usuarios.desactivar[nadie@uagrm.bo]");

        assertThat(mailTo("director@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("No existe una cuenta con el correo nadie@uagrm.bo."));
    }

    @Test
    void disablingTwiceIsRejectedWithoutNoticeTheSecondTime() throws Exception {
        command("director@uagrm.bo", "usuarios.desactivar[ana@uagrm.bo]");
        command("director@uagrm.bo", "usuarios.desactivar[ana@uagrm.bo]");

        assertThat(mailTo("director@uagrm.bo")).hasSize(2).anySatisfy(reply -> assertThat(body(reply))
                .contains("La cuenta ana@uagrm.bo ya está desactivada."));
        assertThat(mailTo("ana@uagrm.bo")).hasSize(1);
    }

    @Test
    void onlyActiveAdministratorMayNotDisableItself() throws Exception {
        command(ADMIN, "usuarios.desactivar[admin@cpms.test]");

        assertThat(active(ADMIN)).isTrue();
        assertThat(mailTo(ADMIN)).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("No se puede desactivar la única cuenta de administrador activa."));
    }

    @Test
    void administratorMayNotDisableItselfEvenWithAnotherAdministrator() throws Exception {
        account("admin2@cpms.test", "SYSTEM_ADMIN", "Diego", "Arce");

        command(ADMIN, "usuarios.desactivar[admin@cpms.test]");

        assertThat(active(ADMIN)).isTrue();
        assertThat(mailTo(ADMIN)).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("No puede desactivar su propia cuenta."));
    }

    @Test
    void listShowsTheStatusOfEachAccount() throws Exception {
        command("director@uagrm.bo", "usuarios.desactivar[luis@uagrm.bo]");
        account("beto@uagrm.bo", "PROFESSOR", "Beto", "Arce");

        command("director@uagrm.bo", "usuarios.listar[docente]");

        assertThat(mailTo("director@uagrm.bo")).anySatisfy(reply -> {
            assertThat(subject(reply)).isEqualTo("Cuentas de usuario: docente (2)");
            assertThat(body(reply))
                    .contains("1. Beto Arce")
                    .contains("2. Luis Rojas")
                    .containsSubsequence("Beto Arce", "Estado: activa", "Luis Rojas", "Estado: desactivada");
        });
    }
}
