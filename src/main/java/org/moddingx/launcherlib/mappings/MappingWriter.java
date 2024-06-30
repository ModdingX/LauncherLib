package org.moddingx.launcherlib.mappings;

import jakarta.annotation.Nullable;
import net.neoforged.srgutils.IMappingBuilder;
import net.neoforged.srgutils.IMappingFile;
import org.moddingx.launcherlib.mappings.visitor.*;

import java.util.*;

/**
 * A {@link MappingVisitor} that assembles an {@link IMappingFile} from the things it visits.
 * If the same class/field/method in the original name set is visited twice, the first visit will be used.
 */
public class MappingWriter extends MappingVisitor {
    
    private final IMappingBuilder builder;
    private final Map<String, PackageVisitor> packages;
    private final Map<String, ClassVisitor> classes;
    
    @Nullable
    private IMappingFile result;

    public MappingWriter() {
        this.builder = IMappingBuilder.create("left", "right");
        this.packages = new HashMap<>();
        this.classes = new HashMap<>();
        this.result = null;
    }
    
    private void ensureMutable() {
        if (this.result != null) {
            throw new IllegalStateException("MappingWriter#result() has already been called.");
        }
    }

    @Nullable
    @Override
    public PackageMappingVisitor visitPackage(String original, String mapped) {
        this.ensureMutable();
        return this.packages.computeIfAbsent(original, k -> new PackageVisitor(this.builder.addPackage(original, mapped)));
    }

    @Nullable
    @Override
    public ClassMappingVisitor visitClass(String original, String mapped) {
        this.ensureMutable();
        return this.classes.computeIfAbsent(original, k-> new ClassVisitor(this.builder.addClass(original, mapped)));
    }

    @Override
    public void visitEnd() {
        // We allow this to be called multiple times
        MappingWriter.this.ensureMutable();
    }

    /**
     * Gets the resulting mappings.
     */
    public IMappingFile result() {
        if (this.result == null) this.result = this.builder.build().getMap("left", "right");
        return this.result;
    }

    private class PackageVisitor extends PackageMappingVisitor {
        
        private final IMappingBuilder.IPackage pkg;
        private final Set<String> metaKeys;
        
        public PackageVisitor(IMappingBuilder.IPackage pkg) {
            this.pkg = pkg;
            this.metaKeys = new HashSet<>();
        }

        @Override
        public void visitMeta(String key, String value) {
            MappingWriter.this.ensureMutable();
            if (this.metaKeys.add(key)) this.pkg.meta(key, value);
        }

        @Override
        public void visitEnd() {
            // We allow this to be called multiple times
            MappingWriter.this.ensureMutable();
        }
    }
    
    private class ClassVisitor extends ClassMappingVisitor {
        
        private final IMappingBuilder.IClass cls;
        private final Map<String, FieldVisitor> fields;
        private final Map<MethodKey, MethodVisitor> methods;
        private final Set<String> metaKeys;

        public ClassVisitor(IMappingBuilder.IClass cls) {
            this.cls = cls;
            this.fields = new HashMap<>();
            this.methods = new HashMap<>();
            this.metaKeys = new HashSet<>();
        }

        @Nullable
        @Override
        public FieldMappingVisitor visitField(@Nullable String descriptor, String original, String mapped) {
            MappingWriter.this.ensureMutable();
            FieldVisitor fv = this.fields.computeIfAbsent(original, k -> {
                IMappingBuilder.IField fd = this.cls.field(original, mapped);
                return new FieldVisitor(fd, mapped);
            });
            if (!fv.hasDescriptor && Objects.equals(mapped, fv.mapped)) {
                // If we map the same field to the same name again but include a descriptor at a later point,
                // use that descriptor.
                fv.fd.descriptor(descriptor);
                fv.hasDescriptor = true;
            }
            return fv;
        }

        @Nullable
        @Override
        public MethodMappingVisitor visitMethod(String descriptor, String original, String mapped) {
            MappingWriter.this.ensureMutable();
            return this.methods.computeIfAbsent(new MethodKey(original, descriptor), k -> new MethodVisitor(this.cls.method(descriptor, original, mapped)));
        }

        @Override
        public void visitMeta(String key, String value) {
            MappingWriter.this.ensureMutable();
            if (this.metaKeys.add(key)) this.cls.meta(key, value);
        }

        @Override
        public void visitEnd() {
            // We allow this to be called multiple times
            MappingWriter.this.ensureMutable();
        }
    }

    private class FieldVisitor extends FieldMappingVisitor {

        private final IMappingBuilder.IField fd;
        private final String mapped;
        private boolean hasDescriptor;
        private final Set<String> metaKeys;
        
        private FieldVisitor(IMappingBuilder.IField fd, String mapped) {
            this.fd = fd;
            this.mapped = mapped;
            this.hasDescriptor = false;
            this.metaKeys = new HashSet<>();
        }

        @Override
        public void visitMeta(String key, String value) {
            MappingWriter.this.ensureMutable();
            if (this.metaKeys.add(key)) this.fd.meta(key, value);
        }

        @Override
        public void visitEnd() {
            // We allow this to be called multiple times
            MappingWriter.this.ensureMutable();
        }
    }

    private class MethodVisitor extends MethodMappingVisitor {

        private final IMappingBuilder.IMethod md;
        private final Map<Integer, ParameterVisitor> parameters;
        private final Set<String> metaKeys;
        
        private MethodVisitor(IMappingBuilder.IMethod md) {
            this.md = md;
            this.parameters = new HashMap<>();
            this.metaKeys = new HashSet<>();
        }

        @Nullable
        @Override
        public ParameterMappingVisitor visitParameter(int idx, String original, String mapped) {
            MappingWriter.this.ensureMutable();
            return this.parameters.computeIfAbsent(idx, k -> new ParameterVisitor(this.md.parameter(idx, original, mapped)));
        }

        @Override
        public void visitMeta(String key, String value) {
            MappingWriter.this.ensureMutable();
            if (this.metaKeys.add(key)) this.md.meta(key, value);
        }

        @Override
        public void visitEnd() {
            // We allow this to be called multiple times
            MappingWriter.this.ensureMutable();
        }
    }

    private class ParameterVisitor extends ParameterMappingVisitor {

        private final IMappingBuilder.IParameter param;
        private final Set<String> metaKeys;

        private ParameterVisitor(IMappingBuilder.IParameter param) {
            this.param = param;
            this.metaKeys = new HashSet<>();
        }

        @Override
        public void visitMeta(String key, String value) {
            MappingWriter.this.ensureMutable();
            if (this.metaKeys.add(key)) this.param.meta(key, value);
        }

        @Override
        public void visitEnd() {
            // We allow this to be called multiple times
            MappingWriter.this.ensureMutable();
        }
    }
    
    private record MethodKey(String name, String descriptor) {
        
    }
}
