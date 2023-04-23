package org.moddingx.launcherlib.nbt.tag;

import org.moddingx.launcherlib.nbt.TagType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Long array tag ({@link TagType#LONG_ARRAY}).
 */
public final class LongArrayTag extends ArrayTag<Long> {

    /**
     * Creates an empty long array tag.
     */
    public LongArrayTag() {
        super(TagType.LONG_ARRAY, List.of());
    }

    /**
     * Creates a new long array tag with the given contents.
     */
    public LongArrayTag(long[] elements) {
        super(TagType.LONG_ARRAY, Arrays.stream(elements).boxed().toList());
    }

    /**
     * Creates a new long array tag with the given contents.
     */
    public LongArrayTag(List<Long> elements) {
        super(TagType.LONG_ARRAY, elements);
    }
    
    @Override
    public LongArrayTag copy() {
        return new LongArrayTag(this.elements);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof LongArrayTag lt && Objects.equals(this.elements, lt.elements));
    }

    @Override
    public int hashCode() {
        return this.elements.hashCode();
    }
}
