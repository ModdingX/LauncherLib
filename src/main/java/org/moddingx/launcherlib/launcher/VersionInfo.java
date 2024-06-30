package org.moddingx.launcherlib.launcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Nullable;
import net.neoforged.srgutils.IMappingFile;
import org.moddingx.launcherlib.launcher.cache.LauncherCache;
import org.moddingx.launcherlib.mappings.MappingHelper;
import org.moddingx.launcherlib.util.Artifact;
import org.moddingx.launcherlib.util.LazyValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Launcher metadata for one specific minecraft version.
 */
public class VersionInfo {

    private final LauncherCache cache;
    private final String id;
    private final VersionType type;
    private final Instant releaseTime;
    private final int java;
    private final URL client;
    @Nullable private final URL server; // Old versions have no server
    private final List<Library> libraries;
    private final LazyValue<AssetIndex> assets;
    @Nullable private final LazyValue<IMappingFile> clientMap;
    @Nullable private final LazyValue<IMappingFile> serverMap;
    @Nullable private final LazyValue<IMappingFile> mergedMap;

    public VersionInfo(LauncherCache cache, JsonObject json, String id) throws IOException {
        try {
            this.cache = cache;
            this.id = id;
            this.type = VersionType.get(json.get("type").getAsString());
            this.releaseTime = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(json.get("releaseTime").getAsString()));
            if (MANUAL_JAVA_MAP.containsKey(id)) {
                this.java = MANUAL_JAVA_MAP.get(id);
            } else {
                this.java = json.getAsJsonObject("javaVersion").get("majorVersion").getAsInt();
            }
            URL assetURL = new URI(json.getAsJsonObject("assetIndex").get("url").getAsString()).toURL();
            this.assets = new LazyValue<>(() -> Launcher.make(assetURL, j -> new AssetIndex(cache, j)));
            JsonObject downloads = json.getAsJsonObject("downloads");
            this.client = new URI(downloads.getAsJsonObject("client").get("url").getAsString()).toURL();
            if (downloads.has("server")) {
                this.server = new URI(downloads.getAsJsonObject("server").get("url").getAsString()).toURL();
            } else {
                this.server = null;
            }
            if (downloads.has("client_mappings") && downloads.has("server_mappings")) {
                URL clientMapUrl = new URI(downloads.getAsJsonObject("client_mappings").get("url").getAsString()).toURL();
                this.clientMap = new LazyValue<>(() -> this.loadMappings("client", clientMapUrl));
                URL serverMapUrl = new URI(downloads.getAsJsonObject("server_mappings").get("url").getAsString()).toURL();
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
                if (!download.getAsJsonObject("downloads").has("classifiers")) { // LWJGL natives on old versions, unsupported
                    String path = download.getAsJsonObject("downloads").getAsJsonObject("artifact").get("path").getAsString();
                    Artifact artifact = Artifact.from(key);
                    URL url = new URI(download.getAsJsonObject("downloads").getAsJsonObject("artifact").get("url").getAsString()).toURL();
                    libraries.add(new Library(this.cache, key, path, artifact, url, os));
                }
            }
            this.libraries = List.copyOf(libraries);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse version information for " + id, e);
        }
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
     * Gets the type of this minecraft version.
     */
    public VersionType type() {
        return this.type;
    }

    /**
     * Gets the release time of this minecraft version.
     */
    public Instant releaseTime() {
        return this.releaseTime;
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
        if (this.server == null) throw new NoSuchElementException("No server available for version " + this.id);
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

    // At the time of writing, versions from 13w24a to 13w38c have no java version set in the metadata
    private static final Map<String, Integer> MANUAL_JAVA_MAP;
    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("13w24a", 8);
        map.put("13w24b", 8);
        map.put("13w25a", 8);
        map.put("13w25b", 8);
        map.put("13w25c", 8);
        map.put("13w26a", 8);
        map.put("1.6", 8);
        map.put("1.6.1", 8);
        map.put("1.6.2", 8);
        map.put("13w36a", 8);
        map.put("13w36b", 8);
        map.put("13w37a", 8);
        map.put("1.6.3", 8);
        map.put("13w37b", 8);
        map.put("1.6.4", 8);
        map.put("13w38a", 8);
        map.put("13w38b", 8);
        map.put("13w38c", 8);
        MANUAL_JAVA_MAP = Map.copyOf(map);
    }
}
