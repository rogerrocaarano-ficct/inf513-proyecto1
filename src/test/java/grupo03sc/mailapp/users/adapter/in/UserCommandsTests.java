package grupo03sc.mailapp.users.adapter.in;

import static grupo03sc.mailapp.messaging.MailTestSupport.ADMIN;
import static grupo03sc.mailapp.messaging.MailTestSupport.body;
import static grupo03sc.mailapp.messaging.MailTestSupport.receivedBy;
import static grupo03sc.mailapp.messaging.MailTestSupport.sendCommand;
import static org.assertj.core.api.Assertions.assertThat;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.messaging.MailTestSupport;
import grupo03sc.mailapp.messaging.adapter.in.Pop3MailboxPoller;
import grupo03sc.mailapp.users.application.UserAccountRepository;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * UC-001 end to end: commands by email through GreenMail, replies and notices.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserCommandsTests {

    @RegisterExtension
    static GreenMailExtension greenMail = MailTestSupport.greenMail();

    @Autowired
    private Pop3MailboxPoller poller;

    @Autowired
    private UserManagementService service;

    @Autowired
    private UserAccountRepository accounts;

    private static String email(String name) {
        return name + "-" + UUID.randomUUID() + "@uagrm.edu.bo";
    }

    private UUID adminId() {
        return accounts.findByEmail(ADMIN).orElseThrow().getId();
    }

    private UserAccount account(Set<Role> roles) {
        return service.createAccount(adminId(), UserAccountData.of("Cuenta de Prueba", email("cuenta"), roles));
    }

    /** Messages received since the last command sent with {@link #run}. */
    private List<MimeMessage> since = List.of();

    private List<MimeMessage> newMailTo(String recipient) {
        return receivedBy(greenMail, recipient).stream().filter(m -> !since.contains(m)).toList();
    }

    /** The only notice (not a reply) that the recipient received after the last command. */
    private MimeMessage onlyNoticeTo(String recipient) {
        List<MimeMessage> notices = newMailTo(recipient).stream().filter(m -> !subject(m).startsWith("Re: ")).toList();
        assertThat(notices).hasSize(1);
        return notices.getFirst();
    }

    private static String subject(MimeMessage message) {
        try {
            return message.getSubject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Sends a command, polls the mailbox once and returns the body of the reply. */
    private String run(String from, String subject) throws Exception {
        since = List.of(greenMail.getReceivedMessages());
        sendCommand(from, subject);
        poller.poll();
        return body(newMailTo(from).stream().filter(m -> subject(m).equals("Re: " + subject))
                .findFirst().orElseThrow());
    }

    @Test
    void createsAProfessorRepliesAndNotifiesTheNewUser() throws Exception {
        String ana = email("ana");

        String reply = run(ADMIN, "CrearUsuario(Ana Pérez; " + ana + "; docente)");

        assertThat(reply).isEqualTo("Cuenta de usuario creada: Ana Pérez <" + ana + ">, con los roles: docente.");
        MimeMessage notice = onlyNoticeTo(ana);
        assertThat(notice.getSubject()).isEqualTo("Cuenta de usuario creada");
        assertThat(body(notice)).contains("Ana Pérez").contains("los roles: docente.");
    }

    @Test
    void createsAccountsWithRolesWrittenInAnyCase() throws Exception {
        UserAccount current = accounts.findAllByOrderByFullNameAsc().stream()
                .filter(a -> a.isActive() && a.hasRole(Role.THESIS_WORKSHOP_PROFESSOR)).findFirst().orElse(null);
        if (current != null) {
            service.deactivateAccount(adminId(), current.getEmail());
        }
        String luis = email("luis");

        String reply = run(ADMIN, "CrearUsuario(Luis Rojas; " + luis + "; Docente , Docente de Taller de Grado)");

        assertThat(reply).endsWith("con los roles: docente, docente de taller de grado.");
    }

    @Test
    void repliesWithTheReasonAndSendsNoNoticeWhenTheRequestIsRejected() throws Exception {
        String juan = email("juan");

        String reply = run(ADMIN, "CrearUsuario(Juan Soto; " + juan + "; estudiante)");

        assertThat(reply).isEqualTo("Faltan datos: registro universitario, cédula de identidad, teléfono.");
        assertThat(newMailTo(juan)).isEmpty();
        assertThat(accounts.findByEmail(juan)).isEmpty();
    }

    @Test
    void doesNotRunUserCommandsForSendersWithoutAManagerRole() throws Exception {
        UserAccount professor = account(Set.of(Role.PROFESSOR));

        assertThat(run(professor.getEmail(), "ListarUsuarios()"))
                .isEqualTo("No tiene permiso para ejecutar el comando ListarUsuarios.");
    }

    @Test
    void repliesThatAnUnknownAccountDoesNotExist() throws Exception {
        assertThat(run(ADMIN, "VerUsuario(nadie@uagrm.edu.bo)"))
                .isEqualTo("La cuenta de usuario no existe: nadie@uagrm.edu.bo.");
    }

    @Test
    void listsAndShowsAccounts() throws Exception {
        UserAccount student = service.createAccount(adminId(), new UserAccountData("Juan Soto", email("juan"),
                Set.of(Role.STUDENT), String.valueOf(System.nanoTime() % 1_000_000_000L), "1234567", "70000000"));

        assertThat(run(ADMIN, "ListarUsuarios()"))
                .contains("Juan Soto <" + student.getEmail() + "> estudiante (activa)");
        assertThat(run(ADMIN, "VerUsuario(" + student.getEmail() + ")"))
                .contains("Registro universitario: " + student.getStudentRegistrationNumber())
                .contains("Cédula de identidad: 1234567")
                .contains("Teléfono: 70000000");
    }

    @Test
    void letsTheProgramDirectorManageOnlyTheirScope() throws Exception {
        UserAccount director = account(Set.of(Role.PROGRAM_DIRECTOR, Role.PROFESSOR));
        String rosa = email("rosa");

        assertThat(run(director.getEmail(), "CrearUsuario(Rosa Vaca; " + rosa + "; secretario)"))
                .startsWith("Cuenta de usuario creada");
        assertThat(run(director.getEmail(), "CrearUsuario(Otro; " + email("otro") + "; director de carrera)"))
                .isEqualTo("Fuera de su alcance: no puede asignar ni quitar el rol director de carrera.");
        assertThat(run(director.getEmail(), "DesactivarUsuario(" + ADMIN + ")"))
                .isEqualTo("Fuera de su alcance: no puede gestionar la cuenta de usuario " + ADMIN + ".");
    }

    @Test
    void changesRolesAndNotifiesButNotForOtherData() throws Exception {
        UserAccount professor = account(Set.of(Role.PROFESSOR));

        String reply = run(ADMIN, "ModificarUsuario(" + professor.getEmail() + "; roles; docente, director de carrera)");
        assertThat(reply).contains("Roles: docente, director de carrera");
        assertThat(onlyNoticeTo(professor.getEmail()).getSubject()).isEqualTo("Roles actualizados");

        run(ADMIN, "ModificarUsuario(" + professor.getEmail() + "; telefono; 71111111)");
        assertThat(newMailTo(professor.getEmail())).filteredOn(m -> !subject(m).startsWith("Re: ")).isEmpty();
        assertThat(run(ADMIN, "ModificarUsuario(" + professor.getEmail() + "; edad; 40)"))
                .startsWith("Dato inválido: el campo edad no existe");
    }

    @Test
    void deactivatesAnAccountSoItCannotSendCommandsAndReactivatesIt() throws Exception {
        UserAccount secretary = account(Set.of(Role.PROGRAM_SECRETARY));

        assertThat(run(ADMIN, "DesactivarUsuario(" + secretary.getEmail() + ")"))
                .isEqualTo("Cuenta de usuario desactivada: " + secretary.getEmail() + ".");
        assertThat(onlyNoticeTo(secretary.getEmail()).getSubject()).isEqualTo("Cuenta de usuario desactivada");
        assertThat(run(secretary.getEmail(), "Ping()")).startsWith("El remitente no está autorizado");

        assertThat(run(ADMIN, "ReactivarUsuario(" + secretary.getEmail() + ")"))
                .isEqualTo("Cuenta de usuario reactivada: " + secretary.getEmail() + ".");
        assertThat(onlyNoticeTo(secretary.getEmail()).getSubject()).isEqualTo("Cuenta de usuario reactivada");
        assertThat(run(secretary.getEmail(), "Ping()")).isEqualTo("Pong");
    }

    @Test
    void rejectsDeactivatingTheOnlyAdministrator() throws Exception {
        assertThat(run(ADMIN, "DesactivarUsuario(" + ADMIN + ")"))
                .isEqualTo("El sistema quedaría sin un administrador activo: " + ADMIN + " es la única cuenta de administrador activa.");
    }
}
