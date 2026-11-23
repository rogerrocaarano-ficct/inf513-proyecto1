package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.application.degreeprocedure.AcademicCalendar;
import bo.org.tecnoweb.cpms.procedures.domain.Semester;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@link AcademicCalendar} that reads the current semester from {@code cpms.academic.current-semester}
 * (UC-002). It stands in for the management of the academic calendar until that use case exists.
 */
@Component
class ConfiguredAcademicCalendar implements AcademicCalendar {
    private final Semester current;

    /**
     * Reads and checks the configured semester, so that the application does not start with a missing
     * or invalid value.
     *
     * @param semester the configured semester, for example {@code 2026-2}
     * @throws IllegalStateException if the value is missing or invalid
     */
    ConfiguredAcademicCalendar(@Value("${cpms.academic.current-semester:}") String semester) {
        try {
            this.current = Semester.parse(semester);
        } catch (DomainValidationException invalid) {
            throw new IllegalStateException("cpms.academic.current-semester is missing or invalid (\"" + semester
                    + "\"): set CPMS_CURRENT_SEMESTER to <year>-<period>, for example 2026-2", invalid);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Semester currentSemester() {
        return current;
    }
}
