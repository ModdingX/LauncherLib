package org.moddingx.launcherlib.nbt.tag;

import org.moddingx.launcherlib.nbt.SNBT;
import org.moddingx.launcherlib.nbt.TagType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * List tag ({@link TagType#LIST}).
 */
public final class ListTag extends ArrayTag<Tag> {

    private TagType elementType;
    
    /**
     * Creates an empty list tag.
     */
    public ListTag() {
        super(TagType.LIST, List.of());
        this.updateElementType();
    }
    
    /**
     * Creates a new list tag with the given contents.
     */
    public ListTag(List<Tag> elements) {
        super(TagType.LIST, elements);
        this.updateElementType();
    }
    
    /**
     * Creates a new list tag with the given contents.
     */
    private ListTag(TagType elementType, List<Tag> elements) {
        super(TagType.LIST, elements);
        this.elementType = elementType;
    }
    
    private void updateElementType() {
        if (this.elements.isEmpty()) {
            this.elementType = TagType.END;
        } else {
            List<TagType> allTypes = this.elements.stream().map(Tag::type).distinct().toList();
            if (allTypes.size() == 1) {
                this.elementType = allTypes.getFirst();
            } else {
                throw new IllegalArgumentException("List of different tag types: " + allTypes.stream().map(TagType::name).collect(Collectors.joining(", ")));
            }
        }
    }
    
    protected void onAdd(Tag newElement) {
        if (this.elementType == TagType.END) {
            this.elementType = newElement.type();
        } else if (this.elementType != newElement.type()) {
            throw new IllegalArgumentException("Invalid element type: expected " + this.elementType + ", got " + newElement.type());
        }
    }

    @Override
    public void set(int idx, Tag value) {
        if (this.size() >= 2) {
            if (this.elementType != value.type()) {
                throw new IllegalArgumentException("Invalid element type: expected " + this.elementType + ", got " + value.type());
            } else {
                super.set(idx, value);
            }
        } else {
            super.set(idx, value);
            this.updateElementType();
        }
    }

    @Override
    public Tag remove(int idx) {
        Tag result = super.remove(idx);
        if (this.elements.isEmpty()) this.elementType = TagType.END;
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        this.elementType = TagType.END;
    }

    /**
     * Gets the type of this lists elements. If the list is empty, this is {@link TagType#END}.
     */
    public TagType elementType() {
        return this.elements.isEmpty() ? TagType.END : this.elementType;
    }
    
    @Override
    public ListTag copy() {
        return new ListTag(this.elementType, this.elements.stream().map(Tag::copy).toList());
    }
    
    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof ListTag lt && Objects.equals(this.elements, lt.elements));
    }

    @Override
    public int hashCode() {
        return this.elements.hashCode();
    }

    /**
     * Adds all elements from the given {@link ListTag} to this {@link ListTag}.
     */
    public void addAll(ListTag tag) {
        this.addAll(tag.elements.stream().map(Tag::copy).toList());
    }
    
    /**
     * Adds all elements from the given {@link ListTag} to this {@link ListTag} at the given index.
     */
    public void addAll(int idx, ListTag tag) {
        this.addAll(idx, tag.elements.stream().map(Tag::copy).toList());
    }
    
    public byte getByte(int idx) {
        return this.get(idx) instanceof NumberTag num ? num.asByte() : 0;
    }
    
    public short getShort(int idx) {
        return this.get(idx) instanceof NumberTag num ? num.asShort() : 0;
    }
    
    public int getInt(int idx) {
        return this.get(idx) instanceof NumberTag num ? num.asInt() : 0;
    }
    
    public long getLong(int idx) {
        return this.get(idx) instanceof NumberTag num ? num.asLong() : 0;
    }
    
    public float getFloat(int idx) {
        return this.get(idx) instanceof NumberTag num ? num.asFloat() : 0;
    }
    
    public double getDouble(int idx) {
        return this.get(idx) instanceof NumberTag num ? num.asDouble() : 0;
    }
    
    public String getString(int idx) {
        Tag tag = this.get(idx);
        if (tag == null) return "";
        return tag instanceof StringTag str ? str.value() : SNBT.write(tag);
    }
    
    public ListTag getList(int idx) {
        return (ListTag) Objects.requireNonNull(this.get(idx));
    }
    
    public CompoundTag getCompound(int idx) {
        return (CompoundTag) Objects.requireNonNull(this.get(idx));
    }
    
    public void addByte(byte value) {
        this.add(NumberTag.createByte(value));
    }

    public void addShort(short value) {
        this.add(NumberTag.createShort(value));
    }

    public void addInt(int value) {
        this.add(NumberTag.createInt(value));
    }

    public void addLong(long value) {
        this.add(NumberTag.createLong(value));
    }

    public void addFloat(float value) {
        this.add(NumberTag.createFloat(value));
    }

    public void addDouble(double value) {
        this.add(NumberTag.createDouble(value));
    }

    public void addString(String value) {
        this.add(new StringTag(value));
    }
    
    public void setByte(int idx, byte value) {
        this.set(idx, NumberTag.createByte(value));
    }
    
    public void setShort(int idx, short value) {
        this.set(idx, NumberTag.createShort(value));
    }
    
    public void setInt(int idx, int value) {
        this.set(idx, NumberTag.createInt(value));
    }
    
    public void setLong(int idx, long value) {
        this.set(idx, NumberTag.createLong(value));
    }
    
    public void setFloat(int idx, float value) {
        this.set(idx, NumberTag.createFloat(value));
    }
    
    public void setDouble(int idx, double value) {
        this.set(idx, NumberTag.createDouble(value));
    }
    
    public void setString(int idx, String value) {
        this.set(idx, new StringTag(value));
    }
}
