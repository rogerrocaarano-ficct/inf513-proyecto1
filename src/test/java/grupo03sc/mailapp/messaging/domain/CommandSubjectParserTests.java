package grupo03sc.mailapp.messaging.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class CommandSubjectParserTests {

    @Test
    void readsTheCommandAndItsArguments() {
        assertThat(CommandSubjectParser.parse("CrearUsuario(Ana Pérez; ana@uagrm.edu.bo; docente)"))
                .contains(new ParsedSubject("CrearUsuario", List.of("Ana Pérez", "ana@uagrm.edu.bo", "docente")));
    }

    @Test
    void readsACommandWithoutArguments() {
        assertThat(CommandSubjectParser.parse("ping()")).contains(new ParsedSubject("ping", List.of()));
        assertThat(CommandSubjectParser.parse("  Ping (  )  ")).contains(new ParsedSubject("Ping", List.of()));
    }

    @Test
    void trimsArgumentsAndKeepsEmptyOnes() {
        assertThat(CommandSubjectParser.parse("Modificar(  a ;; c  )"))
                .contains(new ParsedSubject("Modificar", List.of("a", "", "c")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hola", "Ping(", "Ping)", "(a; b)", "1Ping()", ""})
    void rejectsSubjectsWithoutTheCommandForm(String subject) {
        assertThat(CommandSubjectParser.parse(subject)).isEmpty();
    }

    @Test
    void rejectsAMissingSubject() {
        assertThat(CommandSubjectParser.parse(null)).isEmpty();
    }
}
