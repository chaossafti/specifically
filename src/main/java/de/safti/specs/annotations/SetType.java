package de.safti.specs.annotations;

import javax.management.timer.TimerMBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO: FastUtil and Unmodifiable support
public enum SetType {
    HASH_SET(HashSet.class, HashSet::new, HashSet::new, false),
    LINKED_HASH_SET(LinkedHashSet.class, LinkedHashSet::new, LinkedHashSet::new, false),
    TREE_SET(TreeSet.class, TreeSet::new,_ -> new TreeSet<>(), false),
    CONCURRENT_HASH_SET(ConcurrentHashMap.KeySetView.class, collection -> {
        ConcurrentHashMap.KeySetView<Object, Boolean> set = ConcurrentHashMap.newKeySet();
        set.addAll(collection);
        return set;
    }, ConcurrentHashMap::newKeySet,false)

    ;

    // === Fields ===
    private final Class<?> setClass;
    private final SetFactory factory;
    private final SetCopier copier;
    private final boolean unmodifiable;

    // === Constructor ===
    SetType(Class<?> setClass, SetCopier copier, SetFactory factory, boolean unmodifiable) {
        this.setClass = setClass;
        this.copier = copier;
        this.factory = factory;
        this.unmodifiable = unmodifiable;
    }

    public <T, L extends Set<T>> L create() {
        return create(16);
    }

    public <T, L extends Set<T>> L create(int size) {
        return (L) factory.create(size);
    }

    public <T, L extends Set<T>> L copy(Collection<T> collection) {
        return (L) copier.copy(collection);
    }

    public Class<?> getSetClass() {
        return setClass;
    }

    public boolean isUnmodifiable() {
        return unmodifiable;
    }

    // === Factory interface ===
    interface SetFactory {
        default Set<?> create() {
            return create(16);
        }

        Set<?> create(int size);
    }

    interface SetCopier {
        Set<?> copy(Collection<?> collection);
    }

}
