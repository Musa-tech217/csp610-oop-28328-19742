package smartlib.patterns.behavioural;
// Quick note: this file is here to show the pattern idea without mixing in too much else.

@FunctionalInterface
public interface LibraryEventListener {

    void onEvent(LibraryEvent event);
}
