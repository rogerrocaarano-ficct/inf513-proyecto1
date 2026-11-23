package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Academic semester in which a graduation procedure is opened, written {@code <year>-<period>}, for
 * example {@code 2026-2} (UC-002). It is the first part of the procedure reference.
 *
 * @param year   the year, four digits
 * @param period the period of the year, 1 or 2
 */
public record Semester(int year, int period) {
    /** Field of the validation error. */
    public static final String FIELD = "semestre";

    private static final Pattern FORMAT = Pattern.compile("(\\d{4})-([12])");

    /**
     * Checks the year and the period.
     *
     * @throws DomainValidationException if the year does not have four digits or the period is not 1
     *                                   or 2
     */
    public Semester {
        if (year < 1000 || year > 9999 || (period != 1 && period != 2)) {
            throw invalid(year + "-" + period);
        }
    }

    /**
     * Reads a semester written {@code <year>-<period>}.
     *
     * @param text the semester, for example {@code 2026-2}
     * @return the semester
     * @throws DomainValidationException if the text is empty or does not have that form
     */
    public static Semester parse(String text) {
        Matcher matcher = FORMAT.matcher(text == null ? "" : text.strip());
        if (!matcher.matches()) {
            throw invalid(text);
        }
        return new Semester(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }

    /**
     * Returns the semester written {@code <year>-<period>}.
     *
     * @return for example {@code 2026-2}
     */
    @Override
    public String toString() {
        return year + "-" + period;
    }

    private static DomainValidationException invalid(String text) {
        return new DomainValidationException(Map.of(FIELD,
                "El semestre \"" + (text == null ? "" : text) + "\" no es válido; use año y periodo, por ejemplo 2026-2."));
    }
}
