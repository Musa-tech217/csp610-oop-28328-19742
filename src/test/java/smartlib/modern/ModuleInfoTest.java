package smartlib.modern;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.lang.module.ModuleFinder;
import org.junit.jupiter.api.Test;

class ModuleInfoTest {

    @Test
    void moduleDescriptorCompilesAndExportsPublicApis() {
        var reference = ModuleFinder.of(Path.of("target", "classes"))
                .find("smartlib")
                .orElseThrow();
        var descriptor = reference.descriptor();

        assertEquals("smartlib", descriptor.name());
        assertTrue(descriptor.exports().stream().anyMatch(export -> export.source().equals("smartlib.modern")));
        assertTrue(descriptor.exports().stream().anyMatch(export -> export.source().equals("smartlib.domain")));
    }
}
