package grupo03sc.mailapp.messaging.application;

/**
 * The body of the reply that Messaging sends to the sender of a command.
 */
public record CommandReply(String body) {
}
