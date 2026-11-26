package zarpfinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import zarpfinder.module.Module;
import zarpfinder.module.ModuleManager;
import zarpfinder.module.setting.Setting;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean loading = false;

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("zarpfinder.json");
    }

    public static synchronized void save() {
        if (loading) return;
        try {
            JsonObject root = new JsonObject();
            JsonObject mods = new JsonObject();
            for (Module m : ModuleManager.all()) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.isEnabled());
                JsonObject s = new JsonObject();
                for (Setting setting : m.settings()) {
                    s.add(setting.name(), setting.serialize());
                }
                o.add("settings", s);
                mods.add(m.name(), o);
            }
            root.add("modules", mods);
            Files.writeString(file(), GSON.toJson(root));
        } catch (Throwable ignored) {
        }
    }

    public static synchronized void load() {
        loading = true;
        try {
            if (!Files.exists(file())) return;
            JsonObject root = JsonParser.parseString(Files.readString(file())).getAsJsonObject();
            JsonObject mods = root.getAsJsonObject("modules");
            if (mods == null) return;
            for (Module m : ModuleManager.all()) {
                JsonObject o = mods.getAsJsonObject(m.name());
                if (o == null) continue;
                if (o.has("enabled") && o.get("enabled").getAsBoolean()) {
                    m.setEnabled(true);
                }
                JsonObject s = o.getAsJsonObject("settings");
                if (s == null) continue;
                for (Setting setting : m.settings()) {
                    if (s.has(setting.name())) setting.deserialize(s.get(setting.name()));
                }
            }
        } catch (Throwable ignored) {
        } finally {
            loading = false;
        }
    }
}
