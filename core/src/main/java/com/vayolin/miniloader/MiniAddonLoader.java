package com.vayolin.miniloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;

public final class MiniAddonLoader implements ClientModInitializer {
    public static final MiniAddonLoader INSTANCE = new MiniAddonLoader();
    private static final Logger LOG = Logger.getLogger("Mini Addon Loader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Path root;
    private Path addonsDir;
    private MiniConfig config = new MiniConfig();
    private final List<LoadedAddon> loaded = new ArrayList<>();

    private MiniAddonLoader() {}

    @Override
    public void onInitializeClient() {
        root = FabricLoader.getInstance().getConfigDir().resolve("mini-addon-loader");
        addonsDir = root.resolve("addons");

        try {
            Files.createDirectories(addonsDir);
            config = loadConfig();
            if (config.enabled && config.autoLoad) loadAddons();
        } catch (IOException e) {
            LOG.warning("Initialization failed: " + e);
        }
    }

    private MiniConfig loadConfig() throws IOException {
        Path file = root.resolve("config.json");
        if (Files.notExists(file)) {
            MiniConfig fresh = new MiniConfig();
            try (Writer w = Files.newBufferedWriter(file)) { GSON.toJson(fresh, w); }
            return fresh;
        }
        try (Reader r = Files.newBufferedReader(file)) {
            MiniConfig c = GSON.fromJson(r, MiniConfig.class);
            return c == null ? new MiniConfig() : c;
        }
    }

    public synchronized void loadAddons() {
        unloadAddons();
        if (!config.enabled) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(addonsDir, "*.jar")) {
            for (Path jar : stream) loadOne(jar);
        } catch (IOException e) {
            LOG.warning("Addon scan failed: " + e);
        }
    }

    private void loadOne(Path jar) {
        try {
            String mainClass;
            try (JarFile jf = new JarFile(jar.toFile())) {
                Manifest mf = jf.getManifest();
                if (mf == null) {
                    LOG.warning("Skipping " + jar.getFileName() + ": no manifest.");
                    return;
                }
                mainClass = mf.getMainAttributes().getValue("Mini-Addon-Class");
            }

            if (mainClass == null || mainClass.isBlank()) {
                LOG.warning("Skipping " + jar.getFileName() + ": missing Mini-Addon-Class.");
                return;
            }

            URLClassLoader cl = new URLClassLoader(
                new URL[]{jar.toUri().toURL()},
                MiniAddon.class.getClassLoader()
            );

            Class<?> type = Class.forName(mainClass, true, cl);
            if (!MiniAddon.class.isAssignableFrom(type)) {
                cl.close();
                LOG.warning("Skipping " + jar.getFileName() + ": main class is not MiniAddon.");
                return;
            }

            MiniAddon addon = (MiniAddon) type.getDeclaredConstructor().newInstance();
            addon.onLoad(new MiniAddonContext(Minecraft.getInstance(), addonsDir));
            loaded.add(new LoadedAddon(addon, cl));

            LOG.info("Loaded addon: " + addon.name());
        } catch (Throwable t) {
            LOG.warning("Failed to load addon " + jar.getFileName() + ": " + t);
        }
    }

    public synchronized void unloadAddons() {
        MiniAddonContext ctx = new MiniAddonContext(Minecraft.getInstance(), addonsDir);

        for (LoadedAddon a : loaded) {
            try {
                a.addon.onUnload(ctx);
            } catch (Throwable t) {
                LOG.warning("Addon unload failed: " + a.addon.id() + ": " + t);
            }
            try { a.classLoader.close(); } catch (IOException ignored) {}
        }
        loaded.clear();
    }

    public Path getAddonDirectory() { return addonsDir; }
    public MiniConfig config() { return config; }

    public void saveConfig() {
        try (Writer w = Files.newBufferedWriter(root.resolve("config.json"))) {
            GSON.toJson(config, w);
        } catch (IOException e) {
            LOG.warning("Config save failed: " + e);
        }
    }

    public List<String> loadedAddonIds() {
        return loaded.stream().map(a -> a.addon.id()).toList();
    }

    public static final class MiniConfig {
        public boolean enabled = true;
        public boolean autoLoad = true;
    }

    private record LoadedAddon(MiniAddon addon, URLClassLoader classLoader) {}
}
