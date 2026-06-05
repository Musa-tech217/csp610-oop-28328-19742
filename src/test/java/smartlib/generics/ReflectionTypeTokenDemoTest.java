package smartlib.generics;
// Quick note: this test is mainly here to pin down the expected behaviour.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReflectionTypeTokenDemoTest {

    @Test
    void repositorySubclassRetainsConcreteTypeArgumentsInMetadata() {
        List<String> typeArguments = ReflectionTypeTokenDemo.inspectRepositoryTypeArgumentsFromSubclass();

        assertEquals(List.of("smartlib.domain.Book", "java.lang.String"), typeArguments);
    }

    @Test
    void erasedRepositoryOnlyExposesTypeVariableNamesAtRuntime() {
        List<String> typeParameters = ReflectionTypeTokenDemo.inspectErasedRepositoryTypeParameters();

        assertEquals(List.of("T", "ID"), typeParameters);
    }

    @Test
    void anonymousTypeTokenPreservesParameterizedTypeMetadata() {
        String typeName = ReflectionTypeTokenDemo.inspectAnonymousRepositoryTokenType();

        assertTrue(typeName.contains("smartlib.domain.Repository"));
        assertTrue(typeName.contains("smartlib.domain.Book"));
        assertTrue(typeName.contains("java.lang.String"));
    }
}
