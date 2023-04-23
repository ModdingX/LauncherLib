package org.moddingx.launcherlib.launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.moddingx.launcherlib.launcher.cache.LauncherCache;
import org.moddingx.launcherlib.launcher.cache.NoCache;
import org.moddingx.launcherlib.launcher.cache.PathCache;
import org.moddingx.launcherlib.util.IoFunction;
import org.moddingx.launcherlib.util.LazyValue;

import javax.annotation.WillClose;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

/**
 * Provides access to the launcher metadata.
 * 
 * @see <a href="https://minecraft.fandom.com/wiki/Version_manifest.json">version manifest</a>
 */
public class Launcher {

    private static final String MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final Gson GSON;    
    static {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        GSON = builder.create();
    }
    
    private final LauncherCache cache;
    private List<String> versions = null;
    private Map<String, LazyValue<VersionInfo>> versionInfo = null;

    /**
     * Creates a new <b>uncached</b> {@link Launcher} instance.
     */
    public Launcher() {
        this(NoCache.INSTANCE);
    }

    /**
     * Creates a new {@link Launcher} instance using the given {@link Path} as cache root.
     */
    public Launcher(Path cachePath) {
        this(new PathCache(cachePath));
    }

    /**
     * Creates a new {@link Launcher} instance using a custom cache logic.
     */
    public Launcher(LauncherCache cache) {
        this.cache = cache;
    }

    private synchronized void initIfNeeded() {
        try {
            if (this.versions == null || this.versionInfo == null) {
                JsonObject json = make(new URL(MANIFEST), j -> j);
                List<String> list = new ArrayList<>();
                Map<String, LazyValue<VersionInfo>> table = new HashMap<>();
                for (JsonElement entry : json.getAsJsonArray("versions")) {
                    String key = entry.getAsJsonObject().get("id").getAsString();
                    URL url = new URL(entry.getAsJsonObject().get("url").getAsString());
                    
                    list.add(key);
                    table.put(key, new LazyValue<>(() -> make(url, j -> new VersionInfo(this.cache, j, key))));
                }
                Collections.reverse(list);
                this.versions = List.copyOf(list);
                this.versionInfo = Map.copyOf(table);
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets all known minecraft versions in order.
     */
    public synchronized List<String> versions() {
        this.initIfNeeded();
        return this.versions;
    }

    /**
     * Gets the {@link VersionInfo} for a given minecraft version.
     */
    public synchronized VersionInfo version(String id) {
        this.initIfNeeded();
        if (!this.versionInfo.containsKey(id)) throw new NoSuchElementException("Unknown version: " + id);
        return this.versionInfo.get(id).get();
    }
    
    public static <T> T make(URL url, IoFunction<JsonObject, T> factory) {
        try {
            return make(url.openStream(), factory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> T make(@WillClose InputStream in, IoFunction<JsonObject, T> factory) {
        try (InputStream inStream = in; Reader reader = new InputStreamReader(inStream)) {
            return factory.apply(GSON.fromJson(reader, JsonObject.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
