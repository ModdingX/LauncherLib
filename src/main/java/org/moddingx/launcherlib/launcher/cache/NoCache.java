package org.moddingx.launcherlib.launcher.cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * An implementation of {@link LauncherCache} that does not perform any caching.
 */
public enum NoCache implements LauncherCache {
    INSTANCE;


    @Override
    public InputStream downloadVersion(String version, String key, URL url) throws IOException {
        return url.openStream();
    }

    @Override
    public InputStream downloadMappings(String version, String key, URL url) throws IOException {
        return url.openStream();
    }

    @Override
    public InputStream downloadLibrary(String key, URL url) throws IOException {
        return url.openStream();
    }

    @Override
    public InputStream downloadAsset(String hash, URL url) throws IOException {
        return url.openStream();
    }
}
