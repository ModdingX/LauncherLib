package org.moddingx.launcherlib.mappings;

import net.neoforged.srgutils.IMappingBuilder;
import net.neoforged.srgutils.IMappingFile;
import org.jetbrains.annotations.Nullable;
import org.moddingx.launcherlib.mappings.visitor.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MappingHelper {

    /**
     * Visits the given {@link IMappingFile mappings} using the given {@link MappingVisitor visitor}.
     */
    public static void accept(IMappingFile mappings, MappingVisitor visitor) {
        for (IMappingFile.IPackage pkg : mappings.getPackages()) {
            PackageMappingVisitor pkgVisitor = visitor.visitPackage(pkg.getOriginal(), pkg.getMapped());
            if (pkgVisitor != null) {
                for (Map.Entry<String, String> entry : pkg.getMetadata().entrySet()) {
                    pkgVisitor.visitMeta(entry.getKey(), entry.getValue());
                }
                pkgVisitor.visitEnd();
            }
        }
        for (IMappingFile.IClass cls : mappings.getClasses()) {
            ClassMappingVisitor clsVisitor = visitor.visitClass(cls.getOriginal(), cls.getMapped());
            if (clsVisitor != null) {
                for (IMappingFile.IField fd : cls.getFields()) {
                    FieldMappingVisitor fdVisitor = clsVisitor.visitField(fd.getDescriptor(), fd.getOriginal(), fd.getMapped());
                    if (fdVisitor != null) {
                        for (Map.Entry<String, String> entry : fd.getMetadata().entrySet()) {
                            fdVisitor.visitMeta(entry.getKey(), entry.getValue());
                        }
                    }
                }
                for (IMappingFile.IMethod md : cls.getMethods()) {
                    MethodMappingVisitor mdVisitor = clsVisitor.visitMethod(md.getDescriptor(), md.getOriginal(), md.getMapped());
                    if (mdVisitor != null) {
                        for (IMappingFile.IParameter param : md.getParameters()) {
                            ParameterMappingVisitor paramVisitor = mdVisitor.visitParameter(param.getIndex(), param.getOriginal(), param.getMapped());
                            if (paramVisitor != null) {
                                for (Map.Entry<String, String> entry : param.getMetadata().entrySet()) {
                                    paramVisitor.visitMeta(entry.getKey(), entry.getValue());
                                }
                                paramVisitor.visitEnd();
                            }
                        }
                        for (Map.Entry<String, String> entry : md.getMetadata().entrySet()) {
                            mdVisitor.visitMeta(entry.getKey(), entry.getValue());
                        }
                        mdVisitor.visitEnd();
                    }
                }
                for (Map.Entry<String, String> entry : cls.getMetadata().entrySet()) {
                    clsVisitor.visitMeta(entry.getKey(), entry.getValue());
                }
                clsVisitor.visitEnd();
            }
        }
        visitor.visitEnd();
    }

    /**
     * Merges multiple mapping files into one. These mappings should have common names in the {@code original} name
     * set. Mappings that occur earlier in the given list replace mappings that are found later.
     */
    public static IMappingFile merge(IMappingFile... mappings) {
        return merge(Arrays.asList(mappings));
    }
    
    /**
     * Merges multiple mapping files into one. These mappings should have common names in the {@code original} name
     * set. Mappings that occur earlier in the given list replace mappings that are found later.
     */
    public static IMappingFile merge(List<IMappingFile> mappings) {
        if (mappings.isEmpty()) {
            return IMappingBuilder.create("left", "right").build().getMap("left", "right");
        } else if (mappings.size() == 1) {
            return mappings.getFirst();
        } else {
            MappingWriter writer = new MappingWriter();
            for (IMappingFile mapping : mappings) {
                accept(mapping, writer);
            }
            return writer.result();
        }
    }

    /**
     * Returns new {@link IMappingFile} that has the same mappings as the given {@link IMappingFile} but with all
     * parameter mappings removed.
     */
    public static IMappingFile removeParameters(IMappingFile mappings) {
        MappingWriter writer = new MappingWriter();
        accept(mappings, new MappingVisitor(writer) {

            @Override
            public ClassMappingVisitor visitClass(String original, String mapped) {
                return new ClassMappingVisitor(super.visitClass(original, mapped)) {

                    @Override
                    public MethodMappingVisitor visitMethod(String descriptor, String original, String mapped) {
                        return new MethodMappingVisitor(super.visitMethod(descriptor, original, mapped)) {

                            @Nullable
                            @Override
                            public ParameterMappingVisitor visitParameter(int idx, String original, String mapped) {
                                return null;
                            }
                        };
                    }
                };
            }
        });
        return writer.result();
    }

    /**
     * Returns new {@link IMappingFile} that has the same mappings as the given {@link IMappingFile} but with all
     * metadata mappings removed.
     */
    public static IMappingFile removeMeta(IMappingFile mappings) {
        MappingWriter writer = new MappingWriter();
        accept(mappings, new MappingVisitor(writer) {

            @Override
            public PackageMappingVisitor visitPackage(String original, String mapped) {
                return new PackageMappingVisitor(super.visitPackage(original, mapped)) {

                    @Override
                    public void visitMeta(String key, String value) {
                        //
                    }
                };
            }

            @Override
            public ClassMappingVisitor visitClass(String original, String mapped) {
                return new ClassMappingVisitor(super.visitClass(original, mapped)) {

                    @Override
                    public FieldMappingVisitor visitField(@Nullable String descriptor, String original, String mapped) {
                        return new FieldMappingVisitor(super.visitField(descriptor, original, mapped)) {

                            @Override
                            public void visitMeta(String key, String value) {
                                //
                            }
                        };
                    }

                    @Override
                    public MethodMappingVisitor visitMethod(String descriptor, String original, String mapped) {
                        return new MethodMappingVisitor(super.visitMethod(descriptor, original, mapped)) {

                            @Override
                            public ParameterMappingVisitor visitParameter(int idx, String original, String mapped) {
                                return new ParameterMappingVisitor(super.visitParameter(idx, original, mapped)) {

                                    @Override
                                    public void visitMeta(String key, String value) {
                                        //
                                    }
                                };
                            }

                            @Override
                            public void visitMeta(String key, String value) {
                                //
                            }
                        };
                    }

                    @Override
                    public void visitMeta(String key, String value) {
                        //
                    }
                };
            }
        });
        return writer.result();
    }
}
