package org.moddingx.launcherlib.launcher.cache;

import org.moddingx.launcherlib.launcher.Launcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Provides logic on how a {@link Launcher} instance should cache its downloaded files.
 */
public interface LauncherCache {

    /**
     * Cache a version file or download it if no cached version is available.
     */
    InputStream downloadVersion(String version, String key, URL url) throws IOException;
    
    /**
     * Cache a mapping file or download it if no cached version is available.
     */
    InputStream downloadMappings(String version, String key, URL url) throws IOException;
    
    /**
     * Cache a library file or download it if no cached version is available.
     */
    InputStream downloadLibrary(String key, URL url) throws IOException;
    
    /**
     * Cache an asset or download it if no cached version is available.
     */
    InputStream downloadAsset(String hash, URL url) throws IOException;
}
