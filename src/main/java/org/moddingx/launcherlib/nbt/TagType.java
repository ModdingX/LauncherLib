package org.moddingx.launcherlib.nbt;

import org.moddingx.launcherlib.nbt.tag.ByteArrayTag;
import org.moddingx.launcherlib.nbt.tag.CompoundTag;
import org.moddingx.launcherlib.nbt.tag.IntArrayTag;
import org.moddingx.launcherlib.nbt.tag.ListTag;
import org.moddingx.launcherlib.nbt.tag.LongArrayTag;
import org.moddingx.launcherlib.nbt.tag.NumberTag;
import org.moddingx.launcherlib.nbt.tag.StringTag;

/**
 * A tag type for a NBT tag.
 */
public enum TagType {

    /**
     * Marks the end of a {@link #COMPOUND compound} tag.
     */
    END,

    /**
     * A {@link NumberTag#createByte(int) byte tag}.
     */
    BYTE,

    /**
     * A {@link NumberTag#createShort(int) short tag}.
     */
    SHORT,

    /**
     * An {@link NumberTag#createInt(int) int tag}.
     */
    INT,

    /**
     * A {@link NumberTag#createLong(long) long tag}.
     */
    LONG,

    /**
     * A {@link NumberTag#createFloat(float) float tag}.
     */
    FLOAT,

    /**
     * A {@link NumberTag#createDouble(double) double tag}.
     */
    DOUBLE,

    /**
     * A {@link ByteArrayTag byte array tag}.
     */
    BYTE_ARRAY,
    
    /**
     * A {@link StringTag string tag}.
     */
    STRING,
    
    /**
     * A {@link ListTag list tag}.
     */
    LIST,
    
    /**
     * A {@link CompoundTag compound tag}.
     */
    COMPOUND,

    /**
     * An {@link IntArrayTag int array tag}.
     */
    INT_ARRAY,

    /**
     * A {@link LongArrayTag long array tag}.
     */
    LONG_ARRAY;

    /**
     * Gets the id of the tag type.
     * 
     * @see <a href="https://minecraft.wiki/w/NBT_format#Binary_format">NBT format/Binary format</a>
     */
    public final int id() {
        return this.ordinal();
    }

    /**
     * Gets a tag type by its id.
     * 
     * @throws InvalidNbtException If there is no such tag type.
     * 
     * @see <a href="https://minecraft.wiki/w/NBT_format#Binary_format">NBT format/Binary format</a>
     */
    public static TagType get(int id) throws InvalidNbtException {
        try {
            return TagType.values()[id];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidNbtException("Unknown tag type: " + id, e);
        }
    }
}
