package org.moddingx.launcherlib.nbt.tag;

import org.moddingx.launcherlib.nbt.SNBT;
import org.moddingx.launcherlib.nbt.TagType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

/**
 * Base class for all list-like tags.
 */
public abstract sealed class ArrayTag<T> implements Tag, Iterable<T> permits ByteArrayTag, IntArrayTag, ListTag, LongArrayTag {
    
    private final TagType type;
    protected final List<T> elements;

    protected ArrayTag(TagType type, List<T> elements) {
        this.type = type;
        this.elements = new ArrayList<>(elements);
    }

    @Override
    public TagType type() {
        return this.type;
    }
    
    @Override
    public abstract ArrayTag<T> copy();

    @Override
    public final String toString() {
        return SNBT.write(this);
    }

    /**
     * Gets the size of this tag.
     */
    public int size() {
        return this.elements.size();
    }

    /**
     * Gets the element at the given index.
     */
    public T get(int idx) {
        return this.elements.get(idx);
    }
    
    /**
     * Adds a new element to this tag.
     */
    public void add(T value) {
        this.onAdd(value);
        this.elements.add(value);
    }
    
    /**
     * Adds a new element to this tag at the given index.
     */
    public void add(int idx, T value) {
        this.onAdd(value);
        this.elements.add(idx, value);
    }
    
    /**
     * Adds multiple new elements to this tag.
     */
    public void addAll(List<T> values) {
        values.forEach(this::onAdd);
        this.elements.addAll(values);
    }
    
    /**
     * Adds multiple new elements to this tag at the given index.
     */
    public void addAll(int idx, List<T> values) {
        values.forEach(this::onAdd);
        this.elements.addAll(idx, values);
    }

    /**
     * Changes the value at the given index.
     */
    public void set(int idx, T value) {
        this.elements.set(idx, value);
    }
    
    /**
     * Removes the value at the given index.
     * 
     * @return The value that was removed.
     */
    public T remove(int idx) {
        return this.elements.remove(idx);
    }

    /**
     * Removes all elements from this tag.
     */
    public void clear() {
        this.elements.clear();
    }
    
    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return this.elements.iterator();
    }
    
    @Nonnull
    public ListIterator<T> listIterator() {
        return this.elements.listIterator();
    }
    
    @Nonnull
    public ListIterator<T> listIterator(int idx) {
        return this.elements.listIterator(idx);
    }
    
    @Nonnull
    public Stream<T> stream() {
        return this.elements.stream();
    }
    
    protected void onAdd(T elem) {
        
    }
}
