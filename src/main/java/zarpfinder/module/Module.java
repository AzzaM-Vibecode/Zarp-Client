package zarpfinder.module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import zarpfinder.ConfigManager;
import zarpfinder.module.setting.BooleanSetting;
import zarpfinder.module.setting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private final List<Setting> settings = new ArrayList<>();
    private final BooleanSetting toggleMessage;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.toggleMessage = addSetting(new BooleanSetting("Toggle Message", true));
    }

    protected final <S extends Setting> S addSetting(S setting) {
        settings.add(setting);
        return setting;
    }

    public final List<Setting> settings() {
        return Collections.unmodifiableList(settings);
    }

    public final String name() { return name; }
    public final String description() { return description; }
    public final Category category() { return category; }
    public final boolean isEnabled() { return enabled; }

    public final void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        try {
            if (value) onEnable(); else onDisable();
        } catch (Throwable ignored) {
        }
        if (toggleMessage.get()) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(Component.literal(
                        "\u00A7bZarpFinder\u00A77 \u00BB \u00A7f" + name
                                + (value ? " \u00A7aenabled" : " \u00A7cdisabled")), true);
            }
        }
        ConfigManager.save();
    }

    public final void toggle() { setEnabled(!enabled); }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}

    protected final Minecraft mc() { return Minecraft.getInstance(); }
}
