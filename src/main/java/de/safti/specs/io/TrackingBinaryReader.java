package de.safti.specs.io;

import de.safti.specs.layout.SpecLayout;
import de.safti.specs.utils.SpecPrinter;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Iterator;

/**
 * A subclass of BinaryReader that tracks every read operation.
 *
 * @see SpecPrinter#print(BinaryData, SpecLayout, PrintStream)
 */
public class TrackingBinaryReader extends BinaryReader implements Iterable<Long> {

    private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();

    /**
     * Creates a BitReader wrapping the given byte array.
     *
     * @param data The byte array containing the bit stream array.
     */
    public TrackingBinaryReader(BinaryData data) {
        super(data);
    }


    @Override
    public long readBits(int numBits) throws ArrayIndexOutOfBoundsException {
        long bits = super.readBits(numBits);
        queue.enqueue(bits);
        queue.enqueue(numBits);

        return bits;
    }

    public LongArrayFIFOQueue getQueue() {
        return queue;
    }


    @Override
    public @NotNull Iterator<Long> iterator() {
        return new QueueIterator(queue);
    }

    private record QueueIterator(LongArrayFIFOQueue queue) implements Iterator<Long> {

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public Long next() {
            return queue.dequeueLong();
        }
    }

}
