package grupo03sc.mailapp.graduationprocesses.application;

/** The data of a student received when a graduation process is opened. */
public record StudentData(String fullName, String email, String studentRegistrationNumber, String idCard,
        String phone) {
}
