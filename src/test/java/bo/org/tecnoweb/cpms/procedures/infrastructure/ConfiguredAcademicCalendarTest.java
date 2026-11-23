package bo.org.tecnoweb.cpms.procedures.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.procedures.domain.Semester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfiguredAcademicCalendarTest {

    @Test
    void returnsTheConfiguredSemester() {
        assertThat(new ConfiguredAcademicCalendar("2026-2").currentSemester()).isEqualTo(new Semester(2026, 2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "2026-3"})
    void doesNotStartWithAMissingOrInvalidSemester(String value) {
        assertThatThrownBy(() -> new ConfiguredAcademicCalendar(value))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cpms.academic.current-semester");
    }
}
