package org.moddingx.launcherlib.nbt.tag;

import org.moddingx.launcherlib.nbt.SNBT;
import org.moddingx.launcherlib.nbt.TagType;

import java.util.Objects;

/**
 * A numeric tag. Covers tag types {@link TagType#BYTE}, {@link TagType#SHORT}, {@link TagType#INT},
 * {@link TagType#LONG}, {@link TagType#FLOAT} and {@link TagType#DOUBLE}.
 */
public final class NumberTag implements Tag {

    @SuppressWarnings("UnnecessaryBoxing")
    private static final NumberTag FALSE = new NumberTag(TagType.BYTE, Byte.valueOf((byte) 0));
    
    @SuppressWarnings("UnnecessaryBoxing")
    private static final NumberTag TRUE = new NumberTag(TagType.BYTE, Byte.valueOf((byte) 1));
    
    @SuppressWarnings("UnnecessaryBoxing")
    private static final NumberTag ZERO = new NumberTag(TagType.INT, Integer.valueOf(0));
    
    private final TagType type;
    private final Number value;

    private NumberTag(TagType type, Number value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public TagType type() {
        return this.type;
    }

    @Override
    public NumberTag copy() {
        return this;
    }

    @Override
    public String toString() {
        return SNBT.write(this);
    }

    /**
     * Gets the value of this tag as a byte.
     */
    public byte asByte() {
        return this.value.byteValue();
    }
    
    /**
     * Gets the value of this tag as a short.
     */
    public short asShort() {
        return this.value.shortValue();
    }
    
    /**
     * Gets the value of this tag as an integer.
     */
    public int asInt() {
        return this.value.intValue();
    }
    
    /**
     * Gets the value of this tag as a long.
     */
    public long asLong() {
        return this.value.longValue();
    }
    
    /**
     * Gets the value of this tag as a float.
     */
    public float asFloat() {
        return this.value.floatValue();
    }
    
    /**
     * Gets the value of this tag as a double.
     */
    public double asDouble() {
        return this.value.doubleValue();
    }

    /**
     * Gets the value of this tag.
     */
    public Number value() {
        return this.value;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof NumberTag nt && this.type == nt.type && Objects.equals(this.value, nt.value));
    }

    @Override
    public int hashCode() {
        return this.type.id() + this.value.intValue();
    }

    /**
     * Creates a new {@link NumberTag} of type {@link TagType#BYTE}.
     */
    public static NumberTag createByte(int value) {
        if (value == 0) return FALSE;
        if (value == 1) return TRUE;
        //noinspection UnnecessaryBoxing
        return new NumberTag(TagType.BYTE, Byte.valueOf((byte) value));
    }

    /**
     * Creates a new {@link NumberTag} of type {@link TagType#SHORT}.
     */
    public static NumberTag createShort(int value) {
        //noinspection UnnecessaryBoxing
        return new NumberTag(TagType.SHORT, Short.valueOf((short) value));
    }

    /**
     * Creates a new {@link NumberTag} of type {@link TagType#INT}.
     */
    public static NumberTag createInt(int value) {
        if (value == 0) return ZERO;
        //noinspection UnnecessaryBoxing
        return new NumberTag(TagType.INT, Integer.valueOf(value));
    }

    /**
     * Creates a new {@link NumberTag} of type {@link TagType#LONG}.
     */
    public static NumberTag createLong(long value) {
        //noinspection UnnecessaryBoxing
        return new NumberTag(TagType.LONG, Long.valueOf(value));
    }

    /**
     * Creates a new {@link NumberTag} of type {@link TagType#FLOAT}.
     */
    public static NumberTag createFloat(float value) {
        //noinspection UnnecessaryBoxing
        return new NumberTag(TagType.FLOAT, Float.valueOf(value));
    }

    /**
     * Creates a new {@link NumberTag} of type {@link TagType#DOUBLE}.
     */
    public static NumberTag createDouble(double value) {
        //noinspection UnnecessaryBoxing
        return new NumberTag(TagType.DOUBLE, Double.valueOf(value));
    }
}
