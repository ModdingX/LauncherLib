package org.moddingx.launcherlib.launcher;

import java.util.Locale;

public enum VersionType {
    ALPHA, BETA, SNAPSHOT, RELEASE;
    
    public static VersionType get(String id) {
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "alpha", "old_alpha" -> ALPHA;
            case "beta", "old_beta" -> BETA;
            case "snapshot" -> SNAPSHOT;
            case "release" -> RELEASE;
            default -> throw new IllegalArgumentException("Invalid verion type: '" + id + "'");
        };
    }
}
