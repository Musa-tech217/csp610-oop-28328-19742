package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

import java.util.List;

/**
 * Generates text-based reports for a domain type.
 *
 * @param <T> report source type
 */
public interface ReportGenerator<T> {

    String generateReport(List<T> items);
}
