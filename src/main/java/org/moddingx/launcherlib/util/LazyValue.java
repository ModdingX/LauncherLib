package org.moddingx.launcherlib.util;

import java.util.function.Supplier;

/**
 * A lazy value that is initialised when first needed.
 */
public final class LazyValue<T> {
    
    private final Object lock;
    private volatile Supplier<? extends T> supplier;
    private volatile T value;

    /**
     * Creates a new lazy value.
     */
    public LazyValue(Supplier<? extends T> supplier) {
        this.lock = new Object();
        this.supplier = supplier;
        this.value = null;
    }

    /**
     * Gets the value, computing it on first access.
     */
    public T get() {
        if (this.supplier != null) {
            synchronized (this.lock) {
                if (this.supplier != null) {
                    this.value = this.supplier.get();
                    this.supplier = null;
                }
            }
        }
        return this.value;
    }
}
