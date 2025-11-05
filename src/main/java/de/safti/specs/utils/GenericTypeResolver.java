package de.safti.specs.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class to resolve generic type arguments of a superclass or interface
 * based on a starting subtype.
 */
public class GenericTypeResolver {

    /**
     * Cache for resolved type arguments.
     * Key: A record combining the starting type and the target class.
     * Value: The array of resolved generic type arguments.
     */
    private static final Map<CacheKey, Type[]> CACHE = new ConcurrentHashMap<>();

    /**
     * A record to be used as a key in the cache.
     *
     * @param startType   The starting Type (Class or ParameterizedType).
     * @param targetClass The target superclass/interface to resolve.
     */
    private record CacheKey(Type startType, Class<?> targetClass) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(startType, cacheKey.startType) &&
                    Objects.equals(targetClass, cacheKey.targetClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startType, targetClass);
        }
    }

    /**
     * Finds the concrete type arguments for a generic target class/interface,
     * starting from a given type.
     *
     * @param type   The starting type, e.g., {@code FooBar.class} or a
     * {@link ParameterizedType}.
     * @param target The target generic class or interface, e.g., {@code Foo.class}.
     * @return An array of {@code Type} objects representing the resolved
     * type arguments. This array may contain {@link Class},
     * {@link ParameterizedType}, etc. If a type argument cannot be
     * resolved (e.g., it's still a variable), the corresponding
     * array element will be {@code null}.
     * Returns an empty array if the target is not a supertype of the
     * given type.
     */
    public static Type[] findGenericTypeArguments(Type type, Class<?> target) {
        if (type == null || target == null) {
            return new Type[0];
        }

        CacheKey key = new CacheKey(type, target);
        Type[] cachedResult = CACHE.get(key);
        if (cachedResult != null) {
            return cachedResult;
        }

        Type[] result = findRecursive(type, target, Collections.emptyMap());

        if (result == null) {
            // Target not found in hierarchy
            result = new Type[0];
        }

        CACHE.put(key, result);
        return result;
    }

    /**
     * Recursively searches the type hierarchy to find and resolve the target's
     * type arguments.
     *
     * @param currentType The current type (class or interface) being inspected.
     * @param target      The target class/interface we are looking for.
     * @param context     A map of TypeVariable -> Resolved Type from the parent.
     * @return An array of resolved types, or {@code null} if the target is
     * not found in this branch of the hierarchy.
     */
    private static Type[] findRecursive(Type currentType, Class<?> target, Map<TypeVariable<?>, Type> context) {
        // Stop conditions
        if (currentType == null || currentType.equals(Object.class)) {
            return null; // Reached top, target not found
        }

        // 1. Get the raw class and build the context for this level
        Class<?> currentClass;
        Map<TypeVariable<?>, Type> nextContext = new HashMap<>(context);

        if (currentType instanceof ParameterizedType pt) {
            currentClass = (Class<?>) pt.getRawType();
            Type[] actualArgs = pt.getActualTypeArguments();
            TypeVariable<?>[] typeParams = currentClass.getTypeParameters();

            for (int i = 0; i < actualArgs.length; i++) {
                Type arg = actualArgs[i];
                // Resolve the argument if it's a variable from the parent context
                while (arg instanceof TypeVariable<?> tv) {
                    Type resolved = context.get(tv);
                    if (resolved == null || resolved.equals(arg)) {
                        break; // Unresolved or self-referential
                    }
                    arg = resolved;
                }
                nextContext.put(typeParams[i], arg);
            }
        } else if (currentType instanceof Class<?> c) {
            currentClass = c;
            // Context remains the same as the parent
        } else {
            return null; // Not a type we can inspect (e.g., WildcardType)
        }

        // 2. Check if this is our target
        if (currentClass.equals(target)) {
            // Target found! Resolve its type parameters using the current context.
            TypeVariable<?>[] targetParams = target.getTypeParameters();
            Type[] resolvedArgs = new Type[targetParams.length];

            for (int i = 0; i < targetParams.length; i++) {
                resolvedArgs[i] = resolveType(targetParams[i], nextContext);
            }
            return resolvedArgs;
        }

        // 3. Recurse: Search superclass first
        Type genericSuper = currentClass.getGenericSuperclass();
        Type[] fromSuper = findRecursive(genericSuper, target, nextContext);
        if (fromSuper != null) {
            return fromSuper;
        }

        // 4. Recurse: Search interfaces
        for (Type iface : currentClass.getGenericInterfaces()) {
            Type[] fromIface = findRecursive(iface, target, nextContext);
            if (fromIface != null) {
                return fromIface;
            }
        }

        // Target not found in this hierarchy branch
        return null;
    }

    /**
     * Resolves a {@link Type} (which may be a {@link TypeVariable}) into a
     * more concrete {@link Type} (like {@link Class} or {@link ParameterizedType})
     * using the given context.
     *
     * @param type    The type to resolve.
     * @param context The map of type variables to their resolved types.
     * @return The resolved {@code Type}, or {@code null} if it represents an
     * unresolved type variable.
     */
    private static Type resolveType(Type type, Map<TypeVariable<?>, Type> context) {
        Type current = type;

        // Keep resolving as long as it's a type variable found in the context
        while (current instanceof TypeVariable<?> tv) {
            Type resolved = context.get(tv);
            if (resolved == null || resolved.equals(current)) {
                // Unresolved variable
                return null;
            }
            current = resolved;
        }

        // Return the resolved type (Class, ParameterizedType, etc.)
        return current;
    }
}

