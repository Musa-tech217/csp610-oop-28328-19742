package smartlib.generics;
// Quick note: I tried to keep the generic types clear rather than overly clever.

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import smartlib.domain.Book;
import smartlib.domain.BookRepository;
import smartlib.domain.Repository;

/**
 * Demonstrates generic type erasure and a type-token style workaround.
 */
public final class ReflectionTypeTokenDemo {

    private ReflectionTypeTokenDemo() {
    }

    public static List<String> inspectRepositoryTypeArgumentsFromSubclass() {
        Type genericSuperclass = BookRepository.class.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            return Arrays.stream(parameterizedType.getActualTypeArguments())
                    .map(Type::getTypeName)
                    .toList();
        }
        return List.of();
    }

    public static List<String> inspectErasedRepositoryTypeParameters() {
        return Arrays.stream(Repository.class.getTypeParameters())
                .map(typeVariable -> typeVariable.getName())
                .toList();
    }

    public static String inspectAnonymousRepositoryTokenType() {
        TypeToken<Repository<Book, String>> token = new TypeToken<>() {
        };
        return token.type().getTypeName();
    }

    public abstract static class TypeToken<T> {

        private final Type type;

        protected TypeToken() {
            Type genericSuperclass = getClass().getGenericSuperclass();
            if (!(genericSuperclass instanceof ParameterizedType parameterizedType)) {
                throw new IllegalStateException("Type token must be created with an anonymous parameterized subclass");
            }
            this.type = parameterizedType.getActualTypeArguments()[0];
        }

        public Type type() {
            return type;
        }
    }
}
