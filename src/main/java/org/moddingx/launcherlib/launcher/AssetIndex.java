package org.moddingx.launcherlib.launcher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.moddingx.launcherlib.launcher.cache.LauncherCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * An asset index that provides the additional assets, the game needs.
 */
public class AssetIndex {

    private final LauncherCache cache;
    private final Map<String, String> objects;

    public AssetIndex(LauncherCache cache, JsonObject json) {
        this.cache = cache;
        Map<String, String> objects = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("objects").entrySet()) {
            String key = entry.getKey();
            while (key.startsWith("/")) key = key.substring(1);
            String hash = entry.getValue().getAsJsonObject().get("hash").getAsString();
            objects.put(key, hash);
        }
        this.objects = Map.copyOf(objects);
    }

    /**
     * Downloads the assets to the given directory.
     */
    public void download(Path path) throws IOException {
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        try (ExecutorService executor = new ScheduledThreadPoolExecutor(threads)) {
            List<Future<?>> futures = new ArrayList<>(this.objects.size());
            for (Map.Entry<String, String> entry : this.objects.entrySet()) {
                futures.add(executor.submit(() -> {
                    try {
                        Path target = path.resolve(entry.getKey().replace("/", path.getFileSystem().getSeparator()));
                        if (!Files.exists(target.getParent())) {
                            Files.createDirectories(target.getParent());
                        }
                        URL url = new URI("https://resources.download.minecraft.net/" + entry.getValue().substring(0, 2) + "/" + entry.getValue()).toURL();
                        try (InputStream in = this.cache.downloadAsset(entry.getValue(), url)) {
                            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (URISyntaxException e) {
                        throw new UncheckedIOException("Failed to download " + entry.getKey(), new IOException(e));
                    } catch (IOException e) {
                        throw new UncheckedIOException("Failed to download " + entry.getKey(), e);
                    }
                }));
            }
            try {
                for (Future<?> future : futures) {
                    future.get();
                }
            } catch (ExecutionException e) {
                if (e.getCause() instanceof UncheckedIOException x) {
                    throw new IOException(Objects.requireNonNullElse(x.getMessage(), "Failed to download assets"), x.getCause());
                } else {
                    throw new RuntimeException("Failed to download assets", e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to download assets", e);
            } finally {
                try {
                    executor.shutdownNow();
                } catch (Exception e) {
                    //
                }
            }
        }
    }
}