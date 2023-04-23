package org.moddingx.launcherlib.launcher.cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * An implementation of {@link LauncherCache} that caches all downloads under a given base directory.
 */
public class PathCache implements LauncherCache {
    
    private final Path base;

    public PathCache(Path base) {
        this.base = base;
    }

    @Override
    public InputStream downloadVersion(String version, String key, URL url) throws IOException {
        return this.downloadIfNeeded(this.base.resolve("versions").resolve(version).resolve(key + ".jar"), url);
    }
    
    @Override
    public InputStream downloadMappings(String version, String key, URL url) throws IOException {
        return this.downloadIfNeeded(this.base.resolve("versions").resolve(version).resolve(key + ".pg"), url);
    }

    @Override
    public InputStream downloadLibrary(String key, URL url) throws IOException {
        return this.downloadIfNeeded(this.base.resolve("libraries").resolve(key.replace(":", this.base.getFileSystem().getSeparator()) + ".jar"), url);
    }

    @Override
    public InputStream downloadAsset(String hash, URL url) throws IOException {
        return this.downloadIfNeeded(this.base.resolve("assets").resolve(hash.substring(0, 2)).resolve(hash), url);
    }
    
    private InputStream downloadIfNeeded(Path path, URL url) throws IOException {
        if (!Files.isRegularFile(path)) {
            this.createParents(path);
            try (InputStream in = url.openStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return Files.newInputStream(path);
    }
    
    private void createParents(Path path) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
