package org.moddingx.launcherlib.nbt.tag;

import org.moddingx.launcherlib.nbt.TagType;

public sealed interface Tag permits ArrayTag, CompoundTag, NumberTag, StringTag {

    /**
     * Gets the {@link TagType} for this tag.
     */
    TagType type();
    
    /**
     * Makes a copy of this tag. For immutable tags, this may return the same object.
     */
    Tag copy();
}
