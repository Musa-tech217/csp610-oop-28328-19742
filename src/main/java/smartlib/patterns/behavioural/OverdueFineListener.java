package smartlib.patterns.behavioural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import smartlib.domain.Fine;
import smartlib.domain.FineCalculator;
import smartlib.domain.Loan;
import smartlib.domain.Member;
import smartlib.domain.Repository;

/**
 * Reacts to returned-book events by imposing overdue fines when needed.
 */
public final class OverdueFineListener implements LibraryEventListener {

    private final FineCalculator fineCalculator;
    private final Repository<Fine, String> fineRepository;
    private final Repository<Member, String> memberRepository;
    private final LibraryEventBus eventBus;

    public OverdueFineListener(
            FineCalculator fineCalculator,
            Repository<Fine, String> fineRepository,
            Repository<Member, String> memberRepository,
            LibraryEventBus eventBus) {
        this.fineCalculator = Objects.requireNonNull(fineCalculator, "fineCalculator must not be null");
        this.fineRepository = Objects.requireNonNull(fineRepository, "fineRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus must not be null");
    }

    @Override
    public void onEvent(LibraryEvent event) {
        if (event.type() != EventType.BOOK_RETURNED) {
            return;
        }
        Loan loan = event.payload("loan", Loan.class);
        Member member = event.payload("member", Member.class);
        LocalDate returnedOn = event.payload("returnedOn", LocalDate.class);

        Fine fine = fineCalculator.calculate(loan, member, returnedOn);
        if (!fine.hasAmount()) {
            return;
        }

        fineRepository.save(fine);
        Member updatedMember = member.addFine(fine.amount());
        memberRepository.save(updatedMember);
        eventBus.publish(LibraryEvent.of(
                EventType.FINE_IMPOSED,
                Map.of("fine", fine, "member", updatedMember, "loan", loan)));
    }
}
