package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.graduationprocesses.domain.DocumentFile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/** Data shared by the graduation process tests. */
public final class GraduationTestData {

    private GraduationTestData() {
    }

    public static String email(String name) {
        return name + "-" + UUID.randomUUID() + "@uagrm.edu.bo";
    }

    public static String registration() {
        return String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000L));
    }

    public static StudentData student(String email, String registration) {
        return new StudentData("Juan Soto", email, registration, "1234567", "70000000");
    }

    public static DocumentFile pdf(String fileName) {
        return new DocumentFile(fileName, "application/pdf", ("%PDF " + fileName).getBytes(StandardCharsets.UTF_8));
    }

    public static List<DocumentFile> fourDocuments() {
        return List.of(pdf("carta.pdf"), pdf("CV.pdf"), pdf("Cédula.pdf"), pdf("boleta.pdf"));
    }
}
