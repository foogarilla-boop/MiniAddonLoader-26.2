package com.vayolin.miniloader;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ScreenImpl::new;
    }

    private static final class ScreenImpl extends Screen {
        private final Screen parent;
        private Button enabled;

        ScreenImpl(Screen parent) {
            super(Component.literal("Mini Addon Loader"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int x = width / 2 - 100;

            enabled = Button.builder(enabledText(), b -> {
                MiniAddonLoader.INSTANCE.config().enabled =
                    !MiniAddonLoader.INSTANCE.config().enabled;
                MiniAddonLoader.INSTANCE.saveConfig();
                b.setMessage(enabledText());
            }).bounds(x, height / 2 - 35, 200, 20).build();

            addRenderableWidget(enabled);

            addRenderableWidget(Button.builder(Component.literal("Reload Addons"), b -> {
                MiniAddonLoader.INSTANCE.loadAddons();
            }).bounds(x, height / 2 - 5, 200, 20).build());

            addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(x, height / 2 + 35, 200, 20).build());
        }

        private Component enabledText() {
            return Component.literal("Loader: " +
                (MiniAddonLoader.INSTANCE.config().enabled ? "ON" : "OFF"));
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            renderBackground(graphics, mouseX, mouseY, delta);
            graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
            super.render(graphics, mouseX, mouseY, delta);
        }
    }
}
