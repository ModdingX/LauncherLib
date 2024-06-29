package org.moddingx.launcherlib.mappings.visitor;

import org.jetbrains.annotations.Nullable;

/**
 * Provides an ASM-like visitor for package mappings.
 */
public class PackageMappingVisitor {
    
    @Nullable
    private final PackageMappingVisitor visitor;

    public PackageMappingVisitor() {
        this(null);
    }
    
    public PackageMappingVisitor(@Nullable PackageMappingVisitor visitor) {
        this.visitor = visitor;
    }

    public void visitMeta(String key, String value) {
        if (this.visitor != null) this.visitor.visitMeta(key, value);
    }

    public void visitEnd() {
        if (this.visitor != null) this.visitor.visitEnd();
    }
}
