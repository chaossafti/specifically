package de.safti.specs.types;

public final class Type {

    private Type() {
        throw new UnsupportedOperationException("no");
    }


    public @interface Int {

        /**
         * @return The bit size of the signed integer number.
         */
        int value();
    }

    public @interface UInt {

        /**
         * @return The bit size of the unsigned integer number.
         */
        int value();
    }

    public @interface Float {

    }

    public @interface Double {

    }

}
