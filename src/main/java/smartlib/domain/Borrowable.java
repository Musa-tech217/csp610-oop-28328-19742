package smartlib.domain;
// Quick note: I kept the domain code straightforward so the rules are easy to follow.

/**
 * Marks a domain object that can participate in borrowing.
 */
public interface Borrowable {

    boolean isAvailableForBorrow();
}
