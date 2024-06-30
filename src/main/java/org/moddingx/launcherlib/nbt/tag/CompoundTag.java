package org.moddingx.launcherlib.nbt.tag;

import jakarta.annotation.Nonnull;
import org.moddingx.launcherlib.nbt.SNBT;
import org.moddingx.launcherlib.nbt.TagType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Compound tag ({@link TagType#COMPOUND}).
 */
public final class CompoundTag implements Tag, Iterable<Map.Entry<String, Tag>> {

    private final Map<String, Tag> elements;
    
    /**
     * Creates an empty compound tag.
     */
    public CompoundTag() {
        this.elements = new HashMap<>();
    }

    /**
     * Creates a new compound tag with the given contents.
     */
    public CompoundTag(Map<String, Tag> elements) {
        this.elements = new HashMap<>(elements);
    }

    @Override
    public TagType type() {
        return TagType.COMPOUND;
    }

    @Override
    public CompoundTag copy() {
        return new CompoundTag(this.elements.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().copy())));
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof CompoundTag ct && Objects.equals(this.elements, ct.elements));
    }

    @Override
    public int hashCode() {
        return this.elements.hashCode();
    }

    @Override
    public String toString() {
        return SNBT.write(this);
    }
    
    /**
     * Gets the size of this tag.
     */
    public int size() {
        return this.elements.size();
    }

    /**
     * Gets whether this tag contains a value assigned to the given key.
     */
    public boolean has(String key) {
        return this.elements.containsKey(key);
    }

    /**
     * Gets whether this tag contains a value assigned to the given key that is of type {@code type}.
     */
    public boolean has(String key, TagType type) {
        return this.elements.containsKey(key) && this.elements.get(key).type() == type;
    }

    /**
     * Gets a value from this tag.
     * 
     * @throws NoSuchElementException if the given key does not exist.
     */
    public Tag get(String key) {
        if (!this.elements.containsKey(key)) throw new NoSuchElementException(key);
        return this.elements.get(key);
    }

    /**
     * Puts a new value into this tag.
     * 
     * @return The old value for that key or {@code null} if there was none before.
     */
    public Tag put(String key, Tag value) {
        return this.elements.put(key, value);
    }
    
    /**
     * Puts a new value into this tag.
     * 
     * @return The old value for that key or {@code null} if there was none before.
     */
    public Tag put(Map.Entry<String, Tag> entry) {
        return this.elements.put(entry.getKey(), entry.getValue());
    }

    /**
     * Puts multiple values into this tag.
     */
    public void putAll(CompoundTag tag) {
        this.elements.putAll(tag.copy().elements);
    }
    
    /**
     * Puts multiple values into this tag.
     */
    public void putAll(Map<String, Tag> values) {
        this.elements.putAll(values);
    }
    
    /**
     * Puts all values from the given {@link CompoundTag} into this {@link CompoundTag}, recursively merging entries,
     * where both tags contain a {@link CompoundTag}.
     */
    public void merge(CompoundTag tag) {
        for (Map.Entry<String, Tag> entry : tag) {
            if (this.has(entry.getKey(), TagType.COMPOUND) && entry.getValue() instanceof CompoundTag cmp) {
                this.getCompound(entry.getKey()).merge(cmp);
            } else {
                this.put(entry);
            }
        }
    }

    /**
     * Removes the value assigned to the given key.
     *
     * @return The value that was removed.
     */
    public Tag remove(String key) {
        return this.elements.remove(key);
    }

    /**
     * Removes all elements from this tag.
     */
    public void clear() {
        this.elements.clear();
    }

    /**
     * Gets a set of all keys in this tag.
     */
    public Set<String> keys() {
        return this.elements.keySet();
    }
    
    public byte getByte(String key) {
        return this.get(key) instanceof NumberTag num ? num.asByte() : 0;
    }
    
    public short getShort(String key) {
        return this.get(key) instanceof NumberTag num ? num.asShort() : 0;
    }
    
    public int getInt(String key) {
        return this.get(key) instanceof NumberTag num ? num.asInt() : 0;
    }
    
    public long getLong(String key) {
        return this.get(key) instanceof NumberTag num ? num.asLong() : 0;
    }
    
    public float getFloat(String key) {
        return this.get(key) instanceof NumberTag num ? num.asFloat() : 0;
    }
    
    public double getDouble(String key) {
        return this.get(key) instanceof NumberTag num ? num.asDouble() : 0;
    }
    
    public String getString(String key) {
        Tag tag = this.get(key);
        if (tag == null) return "";
        return tag instanceof StringTag str ? str.value() : SNBT.write(tag);
    }
    
    public ListTag getList(String key) {
        return (ListTag) Objects.requireNonNull(this.get(key));
    }
    
    public CompoundTag getCompound(String key) {
        return (CompoundTag) Objects.requireNonNull(this.get(key));
    }
    
    public void putByte(String key, byte value) {
        this.put(key, NumberTag.createByte(value));
    }
    
    public void putShort(String key, short value) {
        this.put(key, NumberTag.createShort(value));
    }
    
    public void putInt(String key, int value) {
        this.put(key, NumberTag.createInt(value));
    }
    
    public void putLong(String key, long value) {
        this.put(key, NumberTag.createLong(value));
    }
    
    public void putFloat(String key, float value) {
        this.put(key, NumberTag.createFloat(value));
    }
    
    public void putDouble(String key, double value) {
        this.put(key, NumberTag.createDouble(value));
    }
    
    public void putString(String key, String value) {
        this.put(key, new StringTag(value));
    }
    
    @Nonnull
    @Override
    public Iterator<Map.Entry<String, Tag>> iterator() {
        return this.elements.entrySet().iterator();
    }
    
    @Nonnull
    public Stream<Map.Entry<String, Tag>> stream() {
        return this.elements.entrySet().stream();
    }
}
