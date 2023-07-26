package org.moddingx.launcherlib.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a maven artifact.
 */
public final class Artifact implements Comparable<Artifact>, Serializable {
    
    @Serial private static final long serialVersionUID = 0L;
    
    private final String group;
    private final String name;
    private final String version;
    @Nullable private final String classifier;
    @Nullable private final String extension;
    
    private transient String descriptor;
    private transient String path;
    private transient Artifact pom;
    private transient Object[][] decomposedVersion;

    private Artifact(String group, String name, String version, @Nullable String classifier, @Nullable String extension) {
        this.group = Objects.requireNonNull(group, "Artifact with null group.").intern();
        this.name = Objects.requireNonNull(name, "Artifact with null name.").intern();
        this.version = Objects.requireNonNull(version, "Artifact with null version.").intern();
        this.classifier = classifier == null ? null : classifier.intern();
        this.extension = extension == null ? null : extension.intern();
        if (this.classifier == null && Objects.equals(this.extension, "pom")) {
            this.pom = this;
        }
    }

    /**
     * Creates a new artifact from the given descriptor.
     */
    public static Artifact from(String descriptor) {
        String group, name, version, classifier = null, extension = null;
        String[] parts = descriptor.split(":");
        if (parts.length != 3 && parts.length != 4) {
            throw new IllegalArgumentException("Invalid artifact descriptor: " + descriptor);
        }
        String[] extParts = parts[parts.length - 1].split("@");
        if (extParts.length != 1 && extParts.length != 2) {
            throw new IllegalArgumentException("Invalid artifact descriptor: " + descriptor);
        }
        parts[parts.length - 1] = extParts[0];
        group = parts[0];
        name = parts[1];
        version = parts[2];
        if (parts.length == 4) classifier = parts[3];
        if (extParts.length == 2) extension = extParts[1];
        return new Artifact(group, name, version, classifier, extension);
    }
    
    /**
     * Creates a new artifact from the given values.
     */
    public static Artifact from(String group, String name, String version) {
        return new Artifact(group, name, version, null, null);
    }
    
    /**
     * Creates a new artifact from the given values.
     */
    public static Artifact from(String group, String name, String version, @Nullable String classifier) {
        return new Artifact(group, name, version, classifier, null);
    }
    
    /**
     * Creates a new artifact from the given values.
     */
    public static Artifact from(String group, String name, String version, @Nullable String classifier, @Nullable String extension) {
        return new Artifact(group, name, version, classifier, extension);
    }

    /**
     * Gets the artifacts group.
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Gets the artifacts name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the artifacts version.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the artifacts classifier or {@code null} if there is none.
     */
    @Nullable
    public String getClassifier() {
        return this.classifier;
    }

    /**
     * Gets the artifacts extension or {@code null} if there is none.
     */
    @Nullable
    public String getExtension() {
        return this.extension;
    }

    /**
     * Gets the artifacts descriptor.
     */
    public String getDescriptor() {
        if (this.descriptor == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.group).append(':').append(this.name).append(':').append(this.version);
            if (this.classifier != null) sb.append(':').append(this.classifier);
            if (this.extension != null) sb.append('@').append(this.extension);
            this.descriptor = sb.toString();
        }
        return this.descriptor;
    }

    /**
     * Gets the path of the artifact inside a maven repository.
     */
    public String getPath() {
        if (this.path == null) {
            StringBuilder sb = new StringBuilder();
            sb.append('/').append(this.group.replace('.', '/')).append('/').append(this.name).append('/').append(this.version);
            sb.append('/').append(this.name).append('-').append(this.version);
            if (this.classifier != null) sb.append('-').append(this.classifier);
            if (this.extension == null) {
                sb.append(".jar");
            } else if (!this.extension.isEmpty()) {
                sb.append('.').append(this.extension);
            }
            this.path = sb.toString();
        }
        return this.path;
    }

    /**
     * Gets an artifact describing the POM of this artifact.
     */
    public Artifact getPom() {
        if (this.pom == null) {
            this.pom = new Artifact(this.group, this.name, this.version, null, "pom");
        }
        return this.pom;
    }

    /**
     * Returns a copy of this artifact with the given classifier and no explicit extension.
     */
    public Artifact withClassifier(@Nullable String classifier) {
        return this.withClassifier(classifier, null);
    }

    /**
     * Returns a copy of this artifact with the given classifier and extension.
     */
    public Artifact withClassifier(@Nullable String classifier, @Nullable String extension) {
        return new Artifact(this.group, this.name, this.version, classifier, extension);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Artifact artifact)) return false;
        return Objects.equals(this.group, artifact.group)
                && Objects.equals(this.name, artifact.name)
                && Objects.equals(this.version, artifact.version)
                && Objects.equals(this.classifier, artifact.classifier)
                && Objects.equals(this.extension, artifact.extension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.group, this.name, this.version, this.classifier, this.extension);
    }

    private static final Comparator<String> STRING_COMPARATOR = Comparator.nullsFirst(Comparator.naturalOrder());
    private static final Comparator<Object> VERSION_PART_COMPARATOR = (a, b) -> {
        if (a instanceof Long n1 && b instanceof Long n2) return Long.compare(n1, n2);
        return Objects.toString(a).compareTo(Objects.toString(b));
    };
    private static final Comparator<Object[]> VERSION_COMPARATOR = (a, b) -> Arrays.compare(a, b, VERSION_PART_COMPARATOR);
    
    @Override
    public int compareTo(@Nonnull Artifact artifact) {
        if (this.decomposedVersion == null) this.decomposedVersion = this.decomposeVersion();
        if (artifact.decomposedVersion == null) artifact.decomposedVersion = artifact.decomposeVersion();
        int cmp;
        cmp = Objects.compare(this.group, artifact.group, STRING_COMPARATOR);
        if (cmp != 0) return Integer.compare(cmp, 0);
        cmp = Objects.compare(this.name, artifact.name, STRING_COMPARATOR);
        if (cmp != 0) return Integer.compare(cmp, 0);
        cmp = Arrays.compare(this.decomposedVersion, artifact.decomposedVersion, VERSION_COMPARATOR);
        if (cmp != 0) return Integer.compare(cmp, 0);
        cmp = Objects.compare(this.classifier, artifact.classifier, STRING_COMPARATOR);
        if (cmp != 0) return Integer.compare(cmp, 0);
        cmp = Objects.compare(this.extension, artifact.extension, STRING_COMPARATOR);
        return Integer.compare(cmp, 0);
    }
    
    private Object[][] decomposeVersion() {
        String[] parts0 = this.version.split("-");
        Object[][] decomposed = new Object[parts0.length][];
        for (int i = 0; i < parts0.length; i++) {
            String[] parts1 = parts0[i].split("\\.");
            decomposed[i] = new Object[parts1.length];
            for (int j = 0; j < parts1.length; j++) {
                try {
                    decomposed[i][j] = Long.parseLong(parts1[j]);
                } catch (NumberFormatException e) {
                    decomposed[i][j] = parts1[j];
                }
            }
        }
        return decomposed;
    }

    @Override
    public String toString() {
        return this.getDescriptor();
    }
}
