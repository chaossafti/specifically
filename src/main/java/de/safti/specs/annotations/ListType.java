package de.safti.specs.annotations;

import it.unimi.dsi.fastutil.booleans.*;
import it.unimi.dsi.fastutil.bytes.*;
import it.unimi.dsi.fastutil.chars.*;
import it.unimi.dsi.fastutil.doubles.*;
import it.unimi.dsi.fastutil.floats.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.shorts.*;

import java.util.*;

/**
 * Represents various {@link List} implementations, including FastUtil (FU) primitive lists.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public enum ListType {

    /** Standard {@link ArrayList}. */
    ARRAY(ArrayList.class, ArrayList::new, ArrayList::new, false),

    /** Standard {@link LinkedList}. */
    LINKED(LinkedList.class, LinkedList::new, s -> new LinkedList<>(), false),

    /** Standard unmodifiable {@link List}. */
    UNMODIFIABLE(Collections.unmodifiableList(Collections.emptyList()).getClass(),
            List::copyOf,
            s -> Collections.emptyList(),
            true),

    /** FU = FastUtil. {@link ByteArrayList}. */
    FU_BYTE(ByteArrayList.class,
            c -> new ByteArrayList((Collection<? extends Byte>) c),
            ByteArrayList::new,
            false),

    /** FU = FastUtil. {@link ShortArrayList}. */
    FU_SHORT(ShortArrayList.class,
            c -> new ShortArrayList((Collection<? extends Short>) c),
            ShortArrayList::new,
            false),

    /** FU = FastUtil. {@link IntArrayList}. */
    FU_INT(IntArrayList.class,
            c -> new IntArrayList((Collection<? extends Integer>) c),
            IntArrayList::new,
            false),

    /** FU = FastUtil. {@link LongArrayList}. */
    FU_LONG(LongArrayList.class,
            c -> new LongArrayList((Collection<? extends Long>) c),
            LongArrayList::new,
            false),

    /** FU = FastUtil. {@link FloatArrayList}. */
    FU_FLOAT(FloatArrayList.class,
            c -> new FloatArrayList((Collection<? extends Float>) c),
            FloatArrayList::new,
            false),

    /** FU = FastUtil. {@link DoubleArrayList}. */
    FU_DOUBLE(DoubleArrayList.class,
            c -> new DoubleArrayList((Collection<? extends Double>) c),
            DoubleArrayList::new,
            false),

    /** FU = FastUtil. {@link CharArrayList}. */
    FU_CHAR(CharArrayList.class,
            c -> new CharArrayList((Collection<? extends Character>) c),
            CharArrayList::new,
            false),

    /** FU = FastUtil. {@link BooleanArrayList}. */
    FU_BOOLEAN(BooleanArrayList.class,
            c -> new BooleanArrayList((Collection<? extends Boolean>) c),
            BooleanArrayList::new,
            false),

    /** FU = FastUtil. Synchronized {@link ByteLists.SynchronizedList}. */
    FU_SYNCHRONIZED_BYTE(ByteLists.SynchronizedList.class,
            c -> ByteLists.synchronize(new ByteArrayList((Collection<? extends Byte>) c)),
            s -> ByteLists.synchronize(new ByteArrayList(s)),
            false),

    /** FU = FastUtil. Synchronized {@link ShortLists.SynchronizedList}. */
    FU_SYNCHRONIZED_SHORT(ShortLists.SynchronizedList.class,
            c -> ShortLists.synchronize(new ShortArrayList((Collection<? extends Short>) c)),
            s -> ShortLists.synchronize(new ShortArrayList(s)),
            false),

    /** FU = FastUtil. Synchronized {@link IntLists.SynchronizedList}. */
    FU_SYNCHRONIZED_INT(IntLists.SynchronizedList.class,
            c -> IntLists.synchronize(new IntArrayList((Collection<? extends Integer>) c)),
            s -> IntLists.synchronize(new IntArrayList(s)),
            false),

    /** FU = FastUtil. Synchronized {@link LongLists.SynchronizedList}. */
    FU_SYNCHRONIZED_LONG(LongLists.SynchronizedList.class,
            c -> LongLists.synchronize(new LongArrayList((Collection<? extends Long>) c)),
            s -> LongLists.synchronize(new LongArrayList(s)),
            false),

    /** FU = FastUtil. Synchronized {@link FloatLists.SynchronizedList}. */
    FU_SYNCHRONIZED_FLOAT(FloatLists.SynchronizedList.class,
            c -> FloatLists.synchronize(new FloatArrayList((Collection<? extends Float>) c)),
            s -> FloatLists.synchronize(new FloatArrayList(s)),
            false),

    /** FU = FastUtil. Synchronized {@link DoubleLists.SynchronizedList}. */
    FU_SYNCHRONIZED_DOUBLE(DoubleLists.SynchronizedList.class,
            c -> DoubleLists.synchronize(new DoubleArrayList((Collection<? extends Double>) c)),
            s -> DoubleLists.synchronize(new DoubleArrayList(s)),
            false),

    /** FU = FastUtil. Synchronized {@link CharLists.SynchronizedList}. */
    FU_SYNCHRONIZED_CHAR(CharLists.SynchronizedList.class,
            c -> CharLists.synchronize(new CharArrayList((Collection<? extends Character>) c)),
            s -> CharLists.synchronize(new CharArrayList(s)),
            false),

    /** FU = FastUtil. Synchronized {@link BooleanLists.SynchronizedList}. */
    FU_SYNCHRONIZED_BOOLEAN(BooleanLists.SynchronizedList.class,
            c -> BooleanLists.synchronize(new BooleanArrayList((Collection<? extends Boolean>) c)),
            s -> BooleanLists.synchronize(new BooleanArrayList(s)),
            false),

    /** FU = FastUtil. Immutable {@link ByteLists.UnmodifiableList}. */
    FU_UNMODIFIABLE_BYTE(ByteLists.UnmodifiableList.class,
            c -> ByteLists.unmodifiable(new ByteArrayList((Collection<? extends Byte>) c)),
            s -> ByteLists.unmodifiable(new ByteArrayList(s)),
            true),

    /** FU = FastUtil. Immutable {@link ShortLists.UnmodifiableList}. */
    FU_UNMODIFIABLE_SHORT(ShortLists.UnmodifiableList.class,
            c -> ShortLists.unmodifiable(new ShortArrayList((Collection<? extends Short>) c)),
            s -> ShortLists.unmodifiable(new ShortArrayList(s)),
            true),

    /** FU = FastUtil. Immutable {@link IntLists.UnmodifiableList}. */
    FU_UNMODIFIABLE_INT(IntLists.UnmodifiableList.class,
            c -> IntLists.unmodifiable(new IntArrayList((Collection<? extends Integer>) c)),
            s -> IntLists.unmodifiable(new IntArrayList(s)),
            true),

    /** FU = FastUtil. Immutable {@link LongLists.UnmodifiableList}. */
    FU_UNMODIFIABLE_LONG(LongLists.UnmodifiableList.class,
            c -> LongLists.unmodifiable(new LongArrayList((Collection<? extends Long>) c)),
            s -> LongLists.unmodifiable(new LongArrayList(s)),
            true),

    /** FU = FastUtil. Immutable {@link FloatLists.UnmodifiableList}. */
    FU_UNMODIFIABLE_FLOAT(FloatLists.UnmodifiableList.class,
            c -> FloatLists.unmodifiable(new FloatArrayList((Collection<? extends Float>) c)),
            s -> FloatLists.unmodifiable(new FloatArrayList(s)),
            true),

    /** FU = FastUtil. Immutable {@link DoubleLists.UnmodifiableList}. */
    FU_UNMODIFIABLE_DOUBLE(DoubleLists.UnmodifiableList.class,
            c -> DoubleLists.unmodifiable(new DoubleArrayList((Collection<? extends Double>) c)),
            s -> DoubleLists.unmodifiable(new DoubleArrayList(s)),
            true),

    /** FU = FastUtil. Immutable {@link CharLists.UnmodifiableList}. */
    FU_UNMODIFIABLE_CHAR(CharLists.UnmodifiableList.class,
            c -> CharLists.unmodifiable(new CharArrayList((Collection<? extends Character>) c)),
            s -> CharLists.unmodifiable(new CharArrayList(s)),
            true),

    /** FU = FastUtil. Immutable {@link BooleanLists.UnmodifiableList}. */
    FU_UNMODIFIABLE_BOOLEAN(BooleanLists.UnmodifiableList.class,
            c -> BooleanLists.unmodifiable(new BooleanArrayList((Collection<? extends Boolean>) c)),
            s -> BooleanLists.unmodifiable(new BooleanArrayList(s)),
            true);

    // === Fields ===
    private final Class<?> listClass;
    private final ListCopier copier;
    private final ListFactory factory;
    private final boolean unmodifiable;

    // === Constructor ===
    ListType(Class<?> listClass, ListCopier copier, ListFactory factory, boolean unmodifiable) {
        this.listClass = listClass;
        this.copier = copier;
        this.factory = factory;
        this.unmodifiable = unmodifiable;
    }

    // === Methods ===
    public <T, L extends List<T>> L create() {
        return create(16);
    }

    public <T, L extends List<T>> L create(int size) {
        return (L) factory.create(size);
    }

    public <T, L extends List<T>> L copy(Collection<T> collection) {
        return (L) copier.copy(collection);
    }

    public Class<?> getListClass() {
        return listClass;
    }

    public boolean isUnmodifiable() {
        return unmodifiable;
    }

    // === Factory interfaces ===
    interface ListFactory {
        default List<?> create() {
            return create(16);
        }

        List<?> create(int size);
    }

    interface ListCopier {
        List<?> copy(Collection<?> collection);
    }
}
