package org.moddingx.launcherlib.launcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.srgutils.IMappingFile;
import org.moddingx.launcherlib.launcher.cache.LauncherCache;
import org.moddingx.launcherlib.mappings.MappingHelper;
import org.moddingx.launcherlib.util.LazyValue;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Launcher metadata for one specific minecraft version.
 */
public class VersionInfo {

    private final LauncherCache cache;
    private final String id;
    private final int java;
    private final URL client;
    private final URL server;
    private final List<Library> libraries;
    private final LazyValue<AssetIndex> assets;
    @Nullable private final LazyValue<IMappingFile> clientMap;
    @Nullable private final LazyValue<IMappingFile> serverMap;
    @Nullable private final LazyValue<IMappingFile> mergedMap;

    public VersionInfo(LauncherCache cache, JsonObject json, String id) throws IOException {
        this.cache = cache;
        this.id = id;
        this.java = json.getAsJsonObject("javaVersion").get("majorVersion").getAsInt();
        URL assetURL = new URL(json.getAsJsonObject("assetIndex").get("url").getAsString());
        this.assets = new LazyValue<>(() -> Launcher.make(assetURL, j -> new AssetIndex(cache, j)));
        JsonObject downloads = json.getAsJsonObject("downloads");
        this.client = new URL(downloads.getAsJsonObject("client").get("url").getAsString());
        this.server = new URL(downloads.getAsJsonObject("server").get("url").getAsString());
        if (downloads.has("client_mappings") && downloads.has("server_mappings")) {
            URL clientMapUrl = new URL(downloads.getAsJsonObject("client_mappings").get("url").getAsString());
            this.clientMap = new LazyValue<>(() -> this.loadMappings("client", clientMapUrl));
            URL serverMapUrl = new URL(downloads.getAsJsonObject("server_mappings").get("url").getAsString());
            this.serverMap = new LazyValue<>(() -> this.loadMappings("server", serverMapUrl));
            this.mergedMap = new LazyValue<>(() -> MappingHelper.merge(this.clientMap.get(), this.serverMap.get()));
        } else {
            this.clientMap = null;
            this.serverMap = null;
            this.mergedMap = null;
        }
        List<Library> libraries = new ArrayList<>();
        for (JsonElement lib : json.getAsJsonArray("libraries")) {
            JsonObject download = lib.getAsJsonObject();
            Set<String> os = null;
            if (download.has("rules")) {
                JsonArray rules = download.getAsJsonArray("rules");
                if (!rules.isEmpty()) {
                    os = new HashSet<>();
                    for (JsonElement ruleElem : rules) {
                        JsonObject rule = ruleElem.getAsJsonObject();
                        if (rule.has("os") && rule.getAsJsonObject("os").has("name")
                                && "allow".equals(rule.get("action").getAsString())) {
                            os.add(rule.getAsJsonObject("os").get("name").getAsString());
                        }
                    }
                }
            }
            String key = download.get("name").getAsString();
            String path = download.getAsJsonObject("downloads").getAsJsonObject("artifact").get("path").getAsString();
            URL url = new URL(download.getAsJsonObject("downloads").getAsJsonObject("artifact").get("url").getAsString());
            libraries.add(new Library(this.cache, key, path, url, os));
        }
        this.libraries = List.copyOf(libraries);
    }

    private IMappingFile loadMappings(String key, URL url) {
        try (InputStream in = this.cache.downloadMappings(this.id, key, url)) {
            IMappingFile mappings = IMappingFile.load(in);
            return mappings.reverse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets this versions name.
     */
    public String id() {
        return this.id;
    }

    /**
     * Gets the java version required to run this minecraft version.
     */
    public int java() {
        return this.java;
    }

    /**
     * Downloads the client.
     */
    public InputStream client() throws IOException {
        return this.cache.downloadVersion(this.id, "client", this.client);
    }

    /**
     * Downloads the server.
     */
    public InputStream server() throws IOException {
        return this.cache.downloadVersion(this.id, "server", this.server);
    }

    /**
     * Gets all libraries required to launch the game.
     */
    public List<Library> libraries() {
        return this.libraries;
    }

    /**
     * Gets the {@link AssetIndex} to use with this version.
     */
    public AssetIndex assets() {
        return this.assets.get();
    }

    /**
     * Gets the client mappings for this version.
     * 
     * @throws NoSuchElementException if there are no mappings for this version.
     */
    public IMappingFile clientMap() {
        if (this.clientMap == null) throw new NoSuchElementException("No mappings available for version " + this.id);
        return this.clientMap.get();
    }
    
    /**
     * Gets the server mappings for this version.
     * 
     * @throws NoSuchElementException if there are no mappings for this version.
     */
    public IMappingFile serverMap() {
        if (this.serverMap == null) throw new NoSuchElementException("No mappings available for version " + this.id);
        return this.serverMap.get();
    }
    
    /**
     * Gets the merged mappings for this version.
     * 
     * @throws NoSuchElementException if there are no mappings for this version.
     */
    public IMappingFile mergedMap() {
        if (this.mergedMap == null) throw new NoSuchElementException("No mappings available for version " + this.id);
        return this.mergedMap.get();
    }
}
