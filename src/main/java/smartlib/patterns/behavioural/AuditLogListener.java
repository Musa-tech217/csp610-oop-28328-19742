package smartlib.patterns.behavioural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Captures an in-memory audit trail of every event it receives.
 */
public final class AuditLogListener implements LibraryEventListener {

    private final CopyOnWriteArrayList<LibraryEvent> auditHistory = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(LibraryEvent event) {
        auditHistory.add(event);
    }

    public List<LibraryEvent> auditHistory() {
        return List.copyOf(auditHistory);
    }
}
