package com.inmobi.adserve.channels.scope;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;


/**
 * Scopes a single execution of a block of code. Apply this scope with a try/finally block:
 * 
 * <pre>
 * <code>
 * 
 *   scope.enter();
 *   try {
 *     // explicitly seed some seed objects...
 *     scope.seed(Key.get(SomeObject.class), someObject);
 *     // create and access scoped objects
 *   } finally {
 *     scope.exit();
 *   }
 * </code>
 * </pre>
 * 
 * The scope can be initialized with one or more seed values by calling <code>seed(key, value)</code> before the
 * injector will be called upon to provide for this key. A typical use is for a servlet filter to enter/exit the scope,
 * representing a Request Scope, and seed HttpServletRequest and HttpServletResponse. For each key inserted with seed(),
 * you must include a corresponding binding:
 * 
 * <pre>
 * <code>
 *   bind(key)
 *       .toProvider(SimpleScope.&lt;KeyClass&gt;seededKeyProvider())
 *       .in(ScopeAnnotation.class);
 * </code>
 * </pre>
 * 
 * @author Jesse Wilson
 * @author Fedor Karpelevitch
 * @author abhishek.parwal
 */
public class NettyRequestScope implements Scope {

    public static final Marker TRACE_MAKER = MarkerFactory.getMarker("TRACE_MAKER");

    private static final Provider<Object> SEEDED_KEY_PROVIDER = new Provider<Object>() {
        @Override
        public Object get() {
            throw new IllegalStateException("If you got here then it means" + " that your code asked"
                    + " for scoped object which" + " should have been explicitly" + " seeded in this scope by calling"
                    + " SimpleScope.seed(), but was not.");
        }
    };
    private final ThreadLocal<Map<Key<?>, Object>> values = new ThreadLocal<Map<Key<?>, Object>>();



    public void enter() {
        checkState(values.get() == null, "A scoping block is already in progress");
        values.set(Maps.<Key<?>, Object>newHashMap());
    }

    public void exit() {
        checkState(values.get() != null, "No scoping block in progress");
        values.remove();
    }

    public <T> void seed(final Key<T> key, final T value) {
        final Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);
        checkState(scopedObjects.get(key) == null,
                "A value for the key %s was already seeded in this scope. Old value: %s New value: %s", key,
                scopedObjects.get(key), value);
        scopedObjects.put(key, value);
    }

    public <T> void seed(final Class<T> clazz, final T value) {
        seed(Key.get(clazz), value);
    }

    @Override
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return new Provider<T>() {
            @Override
            public T get() {
                final Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);

                @SuppressWarnings("unchecked")
                T current = (T) scopedObjects.get(key);
                if (current == null && !scopedObjects.containsKey(key)) {
                    current = unscoped.get();
                    scopedObjects.put(key, current);
                }
                return current;
            }
        };
    }

    private <T> Map<Key<?>, Object> getScopedObjectMap(final Key<T> key) {
        final Map<Key<?>, Object> scopedObjects = values.get();
        if (scopedObjects == null) {
            throw new OutOfScopeException("Cannot access " + key + " outside of a scoping block");
        }
        return scopedObjects;
    }

    /**
     * Returns a provider that always throws exception complaining that the object in question must be seeded before it
     * can be injected.
     * 
     * @return typed provider
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Provider<T> seededKeyProvider() {
        return (Provider<T>) SEEDED_KEY_PROVIDER;
    }
}
