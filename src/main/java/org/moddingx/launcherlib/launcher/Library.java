package org.moddingx.launcherlib.launcher;

import org.moddingx.launcherlib.launcher.cache.LauncherCache;
import org.moddingx.launcherlib.util.Artifact;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A library for the game.
 */
public class Library {
    
    private final LauncherCache cache;
    private final String key;
    private final String path;
    private final Artifact artifact;
    private final URL url;
    
    @Nullable
    private final Set<String> os;

    public Library(LauncherCache cache, String key, String path, Artifact artifact, URL url, @Nullable Set<String> os) {
        this.cache = cache;
        this.key = key;
        this.path = path;
        this.artifact = artifact;
        this.url = url;
        this.os = os == null ? null : os.stream().map(str -> str.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Gets the unique library key.
     */
    public String key() {
        return this.key;
    }
    
    /**
     * Gets the path inside a library structure to download that library to.
     */
    public String path() {
        return this.path;
    }
    
    /**
     * Gets the artifact, this library describes.
     */
    public Artifact artifact() {
        return this.artifact;
    }
    
    /**
     * Gets whether this library should be downloaded on the given operating system. {@code os} should be one
     * of {@code windows}, {@code osx} and {@code linux}.
     */
    public boolean shouldDownloadOn(String os) {
        return this.os == null || this.os.contains(os.toLowerCase(Locale.ROOT));
    }

    /**
     * Opens an {@link InputStream} to download the library.
     */
    public InputStream openStream() throws IOException {
        return this.cache.downloadLibrary(this.key, this.url);
    }

    /**
     * Downloads the library to {@code basePath.resolve(path())}.
     */
    public void download(Path basePath) throws IOException {
        Path target = basePath.resolve(this.path.replace("/", basePath.getFileSystem().getSeparator()));
        if (!Files.exists(target.getParent())) {
            Files.createDirectories(target.getParent());
        }
        try (InputStream in = this.openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
