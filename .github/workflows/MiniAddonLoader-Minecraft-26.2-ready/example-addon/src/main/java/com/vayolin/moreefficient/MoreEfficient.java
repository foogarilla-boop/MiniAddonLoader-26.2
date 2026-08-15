package com.vayolin.moreefficient;

import com.vayolin.miniloader.MiniAddon;
import com.vayolin.miniloader.MiniAddonContext;

public final class MoreEfficient implements MiniAddon {
    @Override public String id() { return "more-efficient"; }
    @Override public String name() { return "More Efficient"; }

    @Override
    public void onLoad(MiniAddonContext context) {
        System.out.println("[Mini Addon Loader] More Efficient loaded.");
    }

    @Override
    public void onUnload(MiniAddonContext context) {
        System.out.println("[Mini Addon Loader] More Efficient unloaded.");
    }
}
