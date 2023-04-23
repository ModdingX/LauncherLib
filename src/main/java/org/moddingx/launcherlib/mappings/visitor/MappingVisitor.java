package org.moddingx.launcherlib.mappings.visitor;

import javax.annotation.Nullable;

/**
 * Provides an ASM-like visitor for mappings.
 */
public class MappingVisitor {
    
    @Nullable
    private final MappingVisitor visitor;

    public MappingVisitor() {
        this(null);
    }
    
    public MappingVisitor(@Nullable MappingVisitor visitor) {
        this.visitor = visitor;
    }
    
    @Nullable
    public PackageMappingVisitor visitPackage(String original, String mapped) {
        return this.visitor != null ? this.visitor.visitPackage(original, mapped) : null;
    }
    
    @Nullable
    public ClassMappingVisitor visitClass(String original, String mapped) {
        return this.visitor != null ? this.visitor.visitClass(original, mapped) : null;
    }

    public void visitEnd() {
        if (this.visitor != null) this.visitor.visitEnd();
    }
}
