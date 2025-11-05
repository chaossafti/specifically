package de.safti.specs.utils;

import de.safti.specs.annotations.Spec;
import de.safti.specs.io.BinaryData;
import de.safti.specs.io.BinaryReader;
import de.safti.specs.io.TrackingBinaryReader;
import de.safti.specs.layout.SpecContext;
import de.safti.specs.layout.SpecLayout;
import de.safti.specs.layout.common.SpecField;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

public class SpecPrinter {

    public static void print(Spec spec, @NotNull SpecLayout layout, @NotNull PrintStream ps) {
        SpecField[] fields = layout.getFields();

        ps.print("Spec: ");
        ps.println(layout.getSpecClass().getCanonicalName());

        for (SpecField field : fields) {

            ps.print(field.name());
            ps.print(" -> ");
            try {
                ps.println(field.getter().invoke(spec));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }


        }
    }

    public static void print(BinaryData data, @NotNull SpecLayout layout, @NotNull PrintStream ps) {
        SpecField[] fields = layout.getFields();
        Class<? extends Spec> specClass = layout.getSpecClass();

        ps.println("\u001B[1mSpec:\u001B[0m " + specClass.getCanonicalName());

        Spec spec = layout.createInstance();
        SpecContext context = new SpecContext(spec, layout);

        // ANSI colors to alternate between batches
        String[] colors = new String[] { "\u001B[34m", "\u001B[36m" }; // blue and cyan

        try {
            TrackingBinaryReader reader = new TrackingBinaryReader(data);

            for (SpecField field : fields) {
                Object object = read(reader, field, context);
                field.setterMethod().invoke(spec, object);

                ps.print("\u001B[33m" + field.name() + "\u001B[0m -> "); // field name in yellow

                LongArrayFIFOQueue batchBits = reader.getQueue();
                int colorIndex = 0;

                int totalBitsRead = 0;
                while (!batchBits.isEmpty()) {
                    long value = batchBits.dequeueLong();           // one batch
                    long numBits = batchBits.dequeueLong();
                    totalBitsRead += Math.toIntExact(numBits);

                    String color = colors[colorIndex % colors.length]; // alternate color per batch

                    // convert to binary string
                    String binary = Long.toBinaryString(value);

                    // pad to nearest multiple of 4 for grouping
                    int padding = 4 - (binary.length() % 4);
                    if (padding != 4) binary = "0".repeat(padding) + binary;

                    // insert spaces every 4 bits
                    binary = binary.replaceAll("(.{4})", "$1 ");

                    ps.print(color + binary + "\u001B[0m "); // print batch in color

                    colorIndex++;
                }

                ps.printf("(%s / %d)%n", object, totalBitsRead);
            }

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    private static Object read(BinaryReader reader, SpecField field, SpecContext context) {
        try {
            return field.type().read(reader, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read field " + field.name(), e);
        }
    }



}
