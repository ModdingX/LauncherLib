package org.moddingx.launcherlib.mappings.visitor;

import javax.annotation.Nullable;

/**
 * Provides an ASM-like visitor for parameter mappings.
 */
public class ParameterMappingVisitor {
    
    @Nullable
    private final ParameterMappingVisitor visitor;

    public ParameterMappingVisitor() {
        this(null);
    }
    
    public ParameterMappingVisitor(@Nullable ParameterMappingVisitor visitor) {
        this.visitor = visitor;
    }
    
    public void visitMeta(String key, String value) {
        if (this.visitor != null) this.visitor.visitMeta(key, value);
    }

    public void visitEnd() {
        if (this.visitor != null) this.visitor.visitEnd();
    }
}
