package org.moddingx.launcherlib.mappings.visitor;

import jakarta.annotation.Nullable;

/**
 * Provides an ASM-like visitor for method mappings.
 */
public class MethodMappingVisitor {
    
    @Nullable
    private final MethodMappingVisitor visitor;

    public MethodMappingVisitor() {
        this(null);
    }
    
    public MethodMappingVisitor(@Nullable MethodMappingVisitor visitor) {
        this.visitor = visitor;
    }

    @Nullable
    public ParameterMappingVisitor visitParameter(int idx, String original, String mapped) {
        return this.visitor != null ? this.visitor.visitParameter(idx, original, mapped) : null;
    }
    
    public void visitMeta(String key, String value) {
        if (this.visitor != null) this.visitor.visitMeta(key, value);
    }

    public void visitEnd() {
        if (this.visitor != null) this.visitor.visitEnd();
    }
}
