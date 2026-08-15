package com.vayolin.miniloader;

public interface MiniAddon {
    String id();
    default String name() { return id(); }
    default void onLoad(MiniAddonContext context) {}
    default void onUnload(MiniAddonContext context) {}
}
