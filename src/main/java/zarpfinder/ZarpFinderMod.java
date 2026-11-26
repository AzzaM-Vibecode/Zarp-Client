package zarpfinder;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zarpfinder.gui.ClickGuiScreen;
import zarpfinder.module.ModuleManager;

public class ZarpFinderMod implements ClientModInitializer {
    public static final String MOD_ID = "zarpfinder";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger("ZarpFinder");
    public static KeyMapping OPEN_GUI;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        ConfigManager.load();

        OPEN_GUI = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.zarpfinder.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.zarpfinder"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI.consumeClick()) {
                client.setScreen(new ClickGuiScreen());
            }
            if (client.player != null && client.screen == null) {
                ModuleManager.tick();
            }
        });
    }
}
