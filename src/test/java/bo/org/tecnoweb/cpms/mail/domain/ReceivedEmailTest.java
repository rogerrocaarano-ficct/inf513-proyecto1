package bo.org.tecnoweb.cpms.mail.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
        void newEmailIsReceived() {
            assertThat(ReceivedEmail.builder().getState()).isEqualTo(ReceivedEmailState.RECEIVED);
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
            ReceivedEmail received = ReceivedEmail.builder();
            ReceivedEmail validated = ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED);

            received.markAsDiscarded();
            validated.markAsDiscarded();

            assertThat(received.getState()).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(validated.getState()).isEqualTo(ReceivedEmailState.DISCARDED);
        }
    }
}
