package bo.org.tecnoweb.cpms.mail.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ReceivedEmailTest {

    private static ReceivedEmail withSubject(String subject) {
        return ReceivedEmail.builder().withSubject(subject);
    }

    @Nested
    class SubjectSyntax {

        @Test
        void actionIsCaseInsensitive() {
            assertThat(withSubject("Usuarios.Listar[]").parseAction()).isEqualTo("usuarios.listar");
        }

        @Test
        void textBeforeTheCommandIsIgnored() {
            assertThat(withSubject("RE: Fwd: usuarios.listar[]").parseAction()).isEqualTo("usuarios.listar");
        }

        @Test
        void spacesAroundTheCommandAreIgnored() {
            assertThat(withSubject("   usuarios.listar   [a]  ").parseAction()).isEqualTo("usuarios.listar");
        }

        @ParameterizedTest
        @ValueSource(strings = {"hola", "usuarios.listar[] gracias", "", "[a; b]"})
        void subjectWithoutTheFormIsRejected(String subject) {
            assertThatThrownBy(() -> withSubject(subject).parseAction())
                    .isInstanceOfSatisfying(DomainValidationException.class, e ->
                            assertThat(e.getErrors()).containsEntry(ReceivedEmail.SUBJECT_FIELD, ReceivedEmail.SYNTAX_HELP));
        }

        @Test
        void missingSubjectIsRejected() {
            assertThatThrownBy(() -> withSubject(null).parseAction())
                    .isInstanceOf(DomainValidationException.class);
        }
    }

    @Nested
    class Arguments {

        @Test
        void argumentsAreTrimmedAndKeepCaseAndInnerSpaces() {
            assertThat(withSubject("cmd[ Ana María ;   Pérez;ana@uagrm.bo ]").parseArgs())
                    .containsExactly("Ana María", "Pérez", "ana@uagrm.bo");
        }

        @Test
        void emptyBracketsHaveNoArguments() {
            assertThat(withSubject("cmd[]").parseArgs()).isEmpty();
            assertThat(withSubject("cmd[   ]").parseArgs()).isEmpty();
        }

        @Test
        void emptyArgumentIsKept() {
            assertThat(withSubject("cmd[a;;b]").parseArgs()).containsExactly("a", "", "b");
        }

        @Test
        void singleArgument() {
            assertThat(withSubject("cmd[ uno ]").parseArgs()).containsExactly("uno");
        }
    }

    @Nested
    class States {

        @Test
        void newEmailIsFetched() {
            assertThat(ReceivedEmail.builder().getState()).isEqualTo(ReceivedEmailState.FETCHED);
        }

        @Test
        void validatedEmailCanBeProcessed() {
            ReceivedEmail email = ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED);

            email.markAsProcessed();

            assertThat(email.getState()).isEqualTo(ReceivedEmailState.PROCESSED);
        }

        @Test
        void processingAgainHasNoEffect() {
            ReceivedEmail email = ReceivedEmail.builder().withState(ReceivedEmailState.PROCESSED);

            email.markAsProcessed();

            assertThat(email.getState()).isEqualTo(ReceivedEmailState.PROCESSED);
        }

        @Test
        void receivedEmailCannotBeProcessedWithoutValidation() {
            assertThatThrownBy(() -> ReceivedEmail.builder().markAsProcessed())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void discardedEmailCannotBeProcessed() {
            ReceivedEmail email = ReceivedEmail.builder();
            email.markAsDiscarded();

            assertThatThrownBy(email::markAsProcessed).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void finalEmailCannotBeDiscarded() {
            ReceivedEmail processed = ReceivedEmail.builder().withState(ReceivedEmailState.PROCESSED);

            assertThatThrownBy(processed::markAsDiscarded).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void receivedOrValidatedEmailCanBeDiscarded() {
            ReceivedEmail received = ReceivedEmail.builder().withState(ReceivedEmailState.RECEIVED);
            ReceivedEmail validated = ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED);

            received.markAsDiscarded();
            validated.markAsDiscarded();

            assertThat(received.getState()).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(validated.getState()).isEqualTo(ReceivedEmailState.DISCARDED);
        }
    }


    @Nested
    class Attachments {

        private Attachment.Content read(String name, byte... data) {
            return new Attachment.Content(name, "application/pdf", data);
        }

        @Test
        void emptyAttachmentIsFoundInMessageOrder() {
            ReceivedEmail email = ReceivedEmail.builder().withAttachments(List.of(read("carta.pdf", (byte) 1),
                    read("boleta.pdf"), read("cv.pdf")));

            assertThat(email.findEmptyAttachment()).map(Attachment.Content::name).contains("boleta.pdf");
        }

        @Test
        void storedAttachmentsAreNotEmpty() {
            ReceivedEmail email = ReceivedEmail.rehydrate().withId(UUID.randomUUID())
                    .withState(ReceivedEmailState.RECEIVED)
                    .withAttachments(List.of(new Attachment.Stored(UUID.randomUUID())));

            assertThat(email.findEmptyAttachment()).isEmpty();
        }

        @Test
        void storingTheAttachmentsKeepsTheirReferencesInOrderAndReceivesTheEmail() {
            UUID first = UUID.randomUUID();
            UUID second = UUID.randomUUID();
            Map<String, UUID> ids = Map.of("carta.pdf", first, "cv.pdf", second);
            List<Attachment.Content> stored = new ArrayList<>();
            ReceivedEmail email = ReceivedEmail.builder().withAttachments(List.of(read("carta.pdf", (byte) 1),
                    read("cv.pdf", (byte) 2)));

            email.storeAttachments(content -> {
                stored.add(content);
                return ids.get(content.name());
            });

            assertThat(stored).extracting(Attachment.Content::name).containsExactly("carta.pdf", "cv.pdf");
            assertThat(email.getState()).isEqualTo(ReceivedEmailState.RECEIVED);
            assertThat(email.getAttachments()).containsExactly(new Attachment.Stored(first),
                    new Attachment.Stored(second));
            assertThat(email.getAttachmentIds()).containsExactly(first, second);
        }

        @Test
        void anEmailWithoutAttachmentsIsReceivedWithNoReferences() {
            ReceivedEmail email = ReceivedEmail.builder();

            email.storeAttachments(content -> UUID.randomUUID());

            assertThat(email.getState()).isEqualTo(ReceivedEmailState.RECEIVED);
            assertThat(email.getAttachmentIds()).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(value = ReceivedEmailState.class, names = "FETCHED", mode = EnumSource.Mode.EXCLUDE)
        void attachmentsCanOnlyBeStoredOnce(ReceivedEmailState state) {
            ReceivedEmail email = ReceivedEmail.builder().withState(state);

            assertThatThrownBy(() -> email.storeAttachments(content -> UUID.randomUUID()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void discardingAFetchedEmailDropsItsAttachments() {
            ReceivedEmail email = ReceivedEmail.builder().withAttachments(List.of(read("boleta.pdf")));

            email.markAsDiscarded();

            assertThat(email.getState()).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(email.getAttachments()).isEmpty();
            assertThat(email.getAttachmentIds()).isEmpty();
        }

        @Test
        void discardingAStoredEmailKeepsItsReferences() {
            UUID id = UUID.randomUUID();
            ReceivedEmail email = ReceivedEmail.builder().withAttachments(List.of(read("carta.pdf", (byte) 1)));
            email.storeAttachments(content -> id);

            email.markAsDiscarded();

            assertThat(email.getAttachmentIds()).containsExactly(id);
        }

        @Test
        void attachmentIdsRequireTheAttachmentsToBeStored() {
            ReceivedEmail email = ReceivedEmail.builder().withAttachments(List.of(read("a.pdf", (byte) 1)));

            assertThatThrownBy(email::getAttachmentIds).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(ReceivedEmail.builder()::getAttachmentIds).isInstanceOf(IllegalStateException.class);
        }
    }
}
