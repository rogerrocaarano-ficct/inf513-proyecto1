package grupo03sc.mailapp.graduationprocesses.domain;

/** A file received in a command: its decoded name, its declared content type and its content. */
public record DocumentFile(String fileName, String contentType, byte[] content) {
}
