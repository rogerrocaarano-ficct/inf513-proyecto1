package bo.org.tecnoweb.cpms.procedures.application.degreeprocedure;

import bo.org.tecnoweb.cpms.procedures.domain.Semester;

/**
 * Port that tells the current academic semester, used to build the reference of a graduation
 * procedure when it is opened (UC-002). The management of the academic calendar is a use case still
 * to come; until then its adapter reads the semester from the configuration.
 */
public interface AcademicCalendar {

    /**
     * Returns the semester in course.
     *
     * @return the current semester
     */
    Semester currentSemester();
}
