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
 * UC-001 A3 end to end: {@code usuarios.listar} by email (GreenMail), accounts in PostgreSQL, the
 * list or the rejection by email.
 */
@SpringBootTest(properties = {
        "cpms.users.admin-email=admin@cpms.test",
        "cpms.mail.pop3.port=3110",
        "cpms.mail.pop3.username=sistema",
        "cpms.mail.pop3.password=secret",
        "spring.mail.port=3025"})
@Import(TestcontainersConfiguration.class)
class ListUsersByEmailIntegrationTest {

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
        account("secretaria@uagrm.bo", "ADMINISTRATIVE", "Rosa", "Flores", null, null, null, null);
        account("director@uagrm.bo", "DIRECTOR", "Carla", "Suarez", "Vaca", null, "100", null);
        account("luis@uagrm.bo", "PROFESSOR", "Luis", "Rojas", null, null, null, null);
        account("ana@uagrm.bo", "STUDENT", "Ana María", "Perez", "Rojas", "1234567", "218001234", "71234567");
        account("beto@uagrm.bo", "STUDENT", "Beto", "Arce", null, "7654321", "218005678", "70000000");
    }

    private void account(String email, String role, String firstName, String lastName, String secondLastName,
            String ci, String code, String phone) {
        jdbc.update("INSERT INTO user_account (id, email_address, role, first_name, last_name, second_last_name, "
                + "ci, uagrm_code, phone_number) VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?, ?, ?)",
                email, role, firstName, lastName, secondLastName, ci, code, phone);
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
    void administrativeListsStudentsInOrderAndTheListedAccountsGetNoMail() throws Exception {
        command("Secretaria@uagrm.bo", "usuarios.listar[Estudiante]");

        assertThat(mailTo("secretaria@uagrm.bo")).singleElement().satisfies(reply -> {
            assertThat(reply.getSubject()).isEqualTo("Cuentas de usuario: estudiante (2)");
            assertThat(body(reply))
                    .contains("1. Beto Arce")
                    .contains("2. Ana María Perez Rojas")
                    .contains("Correo: ana@uagrm.bo")
                    .contains("CI: 1234567 | Código: 218001234 | Teléfono: 71234567")
                    .doesNotContain("luis@uagrm.bo");
        });
        assertThat(mailTo("ana@uagrm.bo")).isEmpty();
        assertThat(mailTo("beto@uagrm.bo")).isEmpty();
    }

    @Test
    void directorListsProfessorsButNotDirectors() throws Exception {
        command("director@uagrm.bo", "usuarios.listar[docente]");
        command("director@uagrm.bo", "usuarios.listar[director]");

        assertThat(mailTo("director@uagrm.bo")).hasSize(2)
                .anySatisfy(reply -> {
                    assertThat(reply.getSubject()).isEqualTo("Cuentas de usuario: docente (1)");
                    assertThat(body(reply)).contains("1. Luis Rojas").contains("CI: - | Código: - | Teléfono: -");
                })
                .anySatisfy(reply -> assertThat(body(reply))
                        .contains("no tiene permiso para listar cuentas de director"));
    }

    @Test
    void administrativeCannotListProfessors() throws Exception {
        command("secretaria@uagrm.bo", "usuarios.listar[docente]");

        assertThat(mailTo("secretaria@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("no tiene permiso para listar cuentas de docente")
                .doesNotContain("luis@uagrm.bo"));
    }

    @Test
    void professorCannotList() throws Exception {
        command("luis@uagrm.bo", "usuarios.listar[estudiante]");

        assertThat(mailTo("luis@uagrm.bo")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("no tiene permiso para listar cuentas de estudiante")
                .doesNotContain("ana@uagrm.bo"));
    }

    @Test
    void unknownSenderIsToldItIsNotRegistered() throws Exception {
        command("extrano@gmail.com", "usuarios.listar[estudiante]");

        assertThat(mailTo("extrano@gmail.com")).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("no está registrada en el sistema")
                .doesNotContain("ana@uagrm.bo"));
    }

    @Test
    void missingRoleIsRejected() throws Exception {
        command(ADMIN, "usuarios.listar[]");

        assertThat(mailTo(ADMIN)).singleElement()
                .satisfies(reply -> assertThat(body(reply)).contains("requiere 1 argumento y se recibieron 0"));
    }

    @Test
    void unknownRoleIsRejectedWithTheAcceptedRoles() throws Exception {
        command(ADMIN, "usuarios.listar[rector]");

        assertThat(mailTo(ADMIN)).singleElement().satisfies(reply -> assertThat(body(reply))
                .contains("\"rector\" no es un rol válido")
                .contains("estudiante, administrativo, docente, docente_taller, director, administrador"));
    }

    @Test
    void roleWithoutAccountsIsANoticeNotAnError() throws Exception {
        command(ADMIN, "usuarios.listar[docente_taller]");

        assertThat(mailTo(ADMIN)).singleElement().satisfies(reply -> {
            assertThat(reply.getSubject()).isEqualTo("Cuentas de usuario: docente_taller (0)");
            assertThat(body(reply)).contains("No hay cuentas con rol docente_taller.");
        });
    }
}
