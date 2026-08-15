package com.vayolin.miniloader;

import net.minecraft.client.Minecraft;

import java.nio.file.Path;

public final class MiniAddonContext {
    private final Minecraft client;
    private final Path addonDirectory;

    public MiniAddonContext(Minecraft client, Path addonDirectory) {
        this.client = client;
        this.addonDirectory = addonDirectory;
    }

    public Minecraft client() { return client; }
    public Path addonDirectory() { return addonDirectory; }
    public MiniAddonLoader loader() { return MiniAddonLoader.INSTANCE; }
}
