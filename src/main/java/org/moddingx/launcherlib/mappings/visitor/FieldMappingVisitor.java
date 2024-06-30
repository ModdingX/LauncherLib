package org.moddingx.launcherlib.mappings.visitor;

import jakarta.annotation.Nullable;

/**
 * Provides an ASM-like visitor for field mappings.
 */
public class FieldMappingVisitor {
    
    @Nullable
    private final FieldMappingVisitor visitor;

    public FieldMappingVisitor() {
        this(null);
    }
    
    public FieldMappingVisitor(@Nullable FieldMappingVisitor visitor) {
        this.visitor = visitor;
    }
    
    public void visitMeta(String key, String value) {
        if (this.visitor != null) this.visitor.visitMeta(key, value);
    }

    public void visitEnd() {
        if (this.visitor != null) this.visitor.visitEnd();
    }
}
