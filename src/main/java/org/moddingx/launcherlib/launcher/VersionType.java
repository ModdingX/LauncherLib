package org.moddingx.launcherlib.launcher;

import java.util.Locale;

/**
 * A minecraft version type.
 */
public enum VersionType {

    /**
     * The {@code old_alpha} version type.
     */
    ALPHA,
    
    /**
     * The {@code old_beta} version type.
     */
    BETA,
    
    /**
     * The {@code snapshot} version type.
     */
    SNAPSHOT,
    
    /**
     * The {@code release} version type.
     */
    RELEASE;

    /**
     * Gets a version type by id.
     */
    public static VersionType get(String id) {
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "alpha", "old_alpha" -> ALPHA;
            case "beta", "old_beta" -> BETA;
            case "snapshot" -> SNAPSHOT;
            case "release" -> RELEASE;
            default -> throw new IllegalArgumentException("Invalid version type: '" + id + "'");
        };
    }
}
