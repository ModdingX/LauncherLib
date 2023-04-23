package org.moddingx.launcherlib.nbt.tag;

import org.moddingx.launcherlib.nbt.TagType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Int array tag ({@link TagType#INT_ARRAY}).
 */
public final class IntArrayTag extends ArrayTag<Integer> {
    
    /**
     * Creates an empty int array tag.
     */
    public IntArrayTag() {
        super(TagType.INT_ARRAY, List.of());
    }

    /**
     * Creates a new int array tag with the given contents.
     */
    public IntArrayTag(int[] elements) {
        super(TagType.INT_ARRAY, Arrays.stream(elements).boxed().toList());
    }

    /**
     * Creates a new int array tag with the given contents.
     */
    public IntArrayTag(List<Integer> elements) {
        super(TagType.INT_ARRAY, elements);
    }

    @Override
    public IntArrayTag copy() {
        return new IntArrayTag(this.elements);
    }
    
    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof IntArrayTag it && Objects.equals(this.elements, it.elements));
    }

    @Override
    public int hashCode() {
        return this.elements.hashCode();
    }
}
