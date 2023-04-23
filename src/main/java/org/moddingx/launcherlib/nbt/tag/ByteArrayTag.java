package org.moddingx.launcherlib.nbt.tag;

import org.moddingx.launcherlib.nbt.TagType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Byte array tag ({@link TagType#BYTE_ARRAY}).
 */
public final class ByteArrayTag extends ArrayTag<Byte> {

    /**
     * Creates an empty byte array tag.
     */
    public ByteArrayTag() {
        super(TagType.BYTE_ARRAY, List.of());
    }
    
    /**
     * Creates a new byte array tag with the given contents.
     */
    public ByteArrayTag(byte[] elements) {
        super(TagType.BYTE_ARRAY, makeList(elements));
    }
    
    /**
     * Creates a new byte array tag with the given contents.
     */
    public ByteArrayTag(List<Byte> elements) {
        super(TagType.BYTE_ARRAY, elements);
    }
    
    @Override
    public ByteArrayTag copy() {
        return new ByteArrayTag(this.elements);
    }
    
    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof ByteArrayTag bt && Objects.equals(this.elements, bt.elements));
    }

    @Override
    public int hashCode() {
        return this.elements.hashCode();
    }

    private static List<Byte> makeList(byte[] data) {
        List<Byte> list = new ArrayList<>(data.length);
        for (byte b : data) list.add(b);
        return list;
    }
}
