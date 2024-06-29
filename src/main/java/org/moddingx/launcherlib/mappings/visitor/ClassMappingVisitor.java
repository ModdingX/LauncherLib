package org.moddingx.launcherlib.mappings.visitor;

import org.jetbrains.annotations.Nullable;

/**
 * Provides an ASM-like visitor for class mappings.
 */
public class ClassMappingVisitor {
    
    @Nullable
    private final ClassMappingVisitor visitor;

    public ClassMappingVisitor() {
        this(null);
    }
    
    public ClassMappingVisitor(@Nullable ClassMappingVisitor visitor) {
        this.visitor = visitor;
    }

    @Nullable
    public FieldMappingVisitor visitField(@Nullable String descriptor, String original, String mapped) {
        return this.visitor != null ? this.visitor.visitField(descriptor, original, mapped) : null;
    }

    @Nullable
    public MethodMappingVisitor visitMethod(String descriptor, String original, String mapped) {
        return this.visitor != null ? this.visitor.visitMethod(descriptor, original, mapped) : null;
    }
    
    public void visitMeta(String key, String value) {
        if (this.visitor != null) this.visitor.visitMeta(key, value);
    }
    
    public void visitEnd() {
        if (this.visitor != null) this.visitor.visitEnd();
    }
}
