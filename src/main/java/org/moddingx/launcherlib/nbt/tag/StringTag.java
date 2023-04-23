package org.moddingx.launcherlib.nbt.tag;

import org.moddingx.launcherlib.nbt.SNBT;
import org.moddingx.launcherlib.nbt.TagType;

/**
 * A string tag of type {@link TagType#STRING}.
 */
public record StringTag(String value) implements Tag {

    @Override
    public TagType type() {
        return TagType.STRING;
    }

    @Override
    public StringTag copy() {
        return this;
    }

    @Override
    public String toString() {
        return SNBT.write(this);
    }
}
