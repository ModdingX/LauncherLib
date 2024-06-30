package org.moddingx.launcherlib.util;

import jakarta.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * Marks a physical side for a mod to be placed in.
 */
public enum Side {

    /**
     * Only the physical client without server.
     */
    CLIENT("client", true, false),
    
    /**
     * Only the dedicated server without the client.
     */
    SERVER("server", false, true),

    /**
     * Both client and server.
     */
    COMMON("common", true, true);

    Side(String id, boolean client, boolean server) {
        this.id = id;
        this.client = client;
        this.server = server;
    }

    /**
     * The id of this side.
     */
    public final String id;
    
    /**
     * Whether this side includes the client.
     */
    public final boolean client;
    
    /**
     * Whether this side includes the dedicated server.
     */
    public final boolean server;

    /**
     * Gets a {@link Side} from its id.
     * 
     * @throws IllegalArgumentException if an invalid id is given.
     */
    public static Side byId(String id) {
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "client" -> CLIENT;
            case "server" -> SERVER;
            case "common" -> COMMON;
            default -> throw new IllegalArgumentException("Unknown side: " + id);
        };
    }

    /**
     * Gets a side object that matches the given availability on client and server. If both {@code client}
     * and {@code server} are {@code false}, {@code null} is returned.
     */
    @Nullable
    public static Side get(boolean client, boolean server) {
        if (client && server) return COMMON;
        else if (client) return CLIENT;
        else if (server) return SERVER;
        else return null;
    }
    
    /**
     * Merges multiple {@link Side}s to return a {@link Side} that includes all of them. Merging zero sides
     * returns {@link #COMMON}. Otherwise if {@code sides} contains only {@link #CLIENT} values, {@link #CLIENT}
     * is returned, if {@code sides} contains only {@link #SERVER} values, {@link #SERVER} is returned. If
     * {@code sides} contains both {@link #CLIENT} <b>and</b> {@link #SERVER} or contains {@link #COMMON},
     * {@link #COMMON} is returned.
     */
    public static Side merge(Side... sides) {
        boolean client = false;
        boolean server = false;
        for (Side side : sides) {
            if (side.client) client = true;
            if (side.server) server = true;
        }
        return Objects.requireNonNullElse(get(client, server), Side.COMMON);
    }

    /**
     * Adds multiple {@link Side}s. This can be used to determine the required side for transitive dependencies.
     * Suppose a mod {@code A} is installed on side {@code sideA} and has a dependency for side {@code sideB}.
     * {@code and(sideA, sideB)} would then be the side, on which the dependency is required.
     * <p>
     * If the dependency is not required at all (for example because mod A is only installed on the server but
     * mod B is a client only dependency of mod A), this method returns {@code null}.
     */
    @Nullable
    public static Side and(Side... sides) {
        boolean client = true;
        boolean server = true;
        for (Side side : sides) {
            if (!side.client) client = false;
            if (!side.server) server = false;
        }
        if (!client && !server) return null;
        return get(client, server);
    }
}
