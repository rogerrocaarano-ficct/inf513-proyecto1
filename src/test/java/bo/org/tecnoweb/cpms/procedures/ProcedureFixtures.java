package bo.org.tecnoweb.cpms.procedures;

import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Shared account and attachment inputs for UC-002 tests. */
public final class ProcedureFixtures {
    private ProcedureFixtures() {}

    /** Returns an active secretary. */
    public static User secretary() {
        return User.builder().withRole(Role.ADMINISTRATIVE).withEmailAddress("secretaria@cpms.test")
                .withFirstName("Laura").withLastName("Rojas");
    }

    /** Returns an active student. */
    public static User student() {
        return User.builder().withRole(Role.STUDENT).withEmailAddress("ana@cpms.test")
                .withFirstName("Ana").withLastName("Pérez").withCi("1234")
                .withUagrmCode("123456").withPhoneNumber("7123456");
    }

    /** Returns the four PDFs required for opening. */
    public static List<File> documents() {
        return List.of(pdf("carta.pdf"), pdf("cv.pdf"), pdf("ci.pdf"), pdf("boleta.pdf"));
    }

    /** Builds a test attachment with a PDF header. */
    public static File pdf(String name) {
        return File.builder().withName(name).withData(("%PDF-1.7\n" + name).getBytes(StandardCharsets.US_ASCII));
    }
}
