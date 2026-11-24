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
 * UC-001 A1, role only, end to end: {@code usuarios.cambiarRol} by email (GreenMail), accounts in
 * PostgreSQL, the replies and notices by email.
 */
@SpringBootTest(properties = {
        "cpms.users.admin-email=admin@cpms.test",
        "cpms.mail.pop3.port=3110",
        "cpms.mail.pop3.username=sistema",
        "cpms.mail.pop3.password=secret",
        "spring.mail.port=3025"})
@Import(TestcontainersConfiguration.class)
class ChangeUserRoleByEmailIntegrationTest {

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
        jdbc.update("UPDATE user_account SET active = TRUE, role = 'SYSTEM_ADMIN' WHERE email_address = ?", ADMIN);
        system = greenMail.setUser(SYSTEM, "sistema", "secret");
        account("director@uagrm.bo", "DIRECTOR", "Carla", "Suarez");
        account("secretaria@uagrm.bo", "ADMINISTRATIVE", "Rosa", "Flores");
        account("luis@uagrm.bo", "PROFESSOR", "Luis", "Rojas");
        account("ana@uagrm.bo", "STUDENT", "Ana", "Perez");
    }

    private void account(String email, String role, String firstName, String lastName) {
        jdbc.update("INSERT INTO user_account (id, email_address, role, first_name, last_name) "
                + "VALUES (gen_random_uuid(), ?, ?, ?, ?)", email, role, firstName, lastName);
    }

    private String role(String email) {
        return jdbc.queryForObject("SELECT role FROM user_account WHERE email_address = ?", String.class, email);
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
    void administratorMakesAProfessorDirectorWhoCanThenActAsDirector() throws Exception {
        command(ADMIN, "usuarios.cambiarRol[Luis@UAGRM.bo; Director]");

        assertThat(role("luis@uagrm.bo")).isEqualTo("DIRECTOR");
        assertThat(mailTo(ADMIN)).singleElement().satisfies(reply -> {
            assertThat(subject(reply)).isEqualTo("Rol cambiado: Luis Rojas");
            assertThat(body(reply)).contains("(luis@uagrm.bo) de docente a director.");
        });
        assertThat(mailTo("luis@uagrm.bo")).singleElement().satisfies(notice -> {
            assertThat(subject(notice)).isEqualTo("Su rol en el sistema de trámites de titulación cambió");
            assertThat(body(notice)).contains("cambió el rol de su cuenta de docente a director.");
        });

        command("luis@uagrm.bo", "usuarios.listar[docente]");
        assertThat(mailTo("luis@uagrm.bo")).anySatisfy(
                reply -> assertThat(subject(reply)).isEqualTo("Cuentas de usuario: docente (0)"));
    }

    @Test
    void directorMayNotGiveTheDirectorRole() throws Exception {
        command("director@uagrm.bo", "usuarios.cambiarRol[luis@uagrm.bo; director]");

        assertThat(role("luis@uagrm.bo")).isEqualTo("PROFESSOR");
        assertThat(mailTo("director@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("no tiene permiso para cambiar el rol de cuentas de director"));
        assertThat(mailTo("luis@uagrm.bo")).isEmpty();
    }

    @Test
    void administrativeMayNotChangeARole() throws Exception {
        command("secretaria@uagrm.bo", "usuarios.cambiarRol[ana@uagrm.bo; docente]");

        assertThat(role("ana@uagrm.bo")).isEqualTo("STUDENT");
        assertThat(mailTo("secretaria@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("no tiene permiso para cambiar el rol de cuentas de estudiante"));
    }

    @Test
    void sameRoleIsRejectedWithoutNotice() throws Exception {
        command("director@uagrm.bo", "usuarios.cambiarRol[luis@uagrm.bo; docente]");

        assertThat(mailTo("director@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("La cuenta luis@uagrm.bo ya tiene el rol docente."));
        assertThat(mailTo("luis@uagrm.bo")).isEmpty();
    }

    @Test
    void administratorMayNotChangeItsOwnRole() throws Exception {
        account("admin2@cpms.test", "SYSTEM_ADMIN", "Diego", "Arce");

        command(ADMIN, "usuarios.cambiarRol[admin@cpms.test; docente]");

        assertThat(role(ADMIN)).isEqualTo("SYSTEM_ADMIN");
        assertThat(mailTo(ADMIN)).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("No puede cambiar el rol de su propia cuenta."));
    }

    @Test
    void professorWithoutStudentDataMayNotBecomeAStudent() throws Exception {
        command("director@uagrm.bo", "usuarios.cambiarRol[luis@uagrm.bo; estudiante]");

        assertThat(role("luis@uagrm.bo")).isEqualTo("PROFESSOR");
        assertThat(mailTo("director@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("No se pudo ejecutar el comando \"usuarios.cambiarrol\":")
                .contains("- CI: Es obligatorio para estudiantes.")
                .contains("- código: Es obligatorio para estudiantes.")
                .contains("- teléfono: Es obligatorio para estudiantes."));
        assertThat(mailTo("luis@uagrm.bo")).isEmpty();
    }
}
