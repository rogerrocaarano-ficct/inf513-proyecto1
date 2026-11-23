package bo.org.tecnoweb.cpms.procedures.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SemesterTest {

    @Test
    void parsesBothPeriodsOfAYear() {
        assertThat(Semester.parse("2026-1")).isEqualTo(new Semester(2026, 1));
        assertThat(Semester.parse(" 2026-2 ")).isEqualTo(new Semester(2026, 2));
    }

    @Test
    void isWrittenAsYearAndPeriod() {
        assertThat(new Semester(2026, 2)).hasToString("2026-2");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2026-3", "2026-0", "26-2", "2026", "2026/2", "2026-2-1"})
    void rejectsAnythingElse(String text) {
        assertThatThrownBy(() -> Semester.parse(text))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrors()).containsOnlyKeys(Semester.FIELD));
    }
}
