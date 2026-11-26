package zarpfinder.module;

import zarpfinder.module.impl.combat.AimAssistModule;
import zarpfinder.module.impl.combat.AutoTotemModule;
import zarpfinder.module.impl.combat.TriggerbotModule;
import zarpfinder.module.impl.combat.WTapModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    private ModuleManager() {}

    public static void register(Module m) { modules.add(m); }
    public static List<Module> all() { return modules; }

    public static List<Module> byCategory(Category c) {
        return modules.stream().filter(m -> m.category() == c).collect(Collectors.toList());
    }

    public static Module byName(String name) {
        return modules.stream().filter(m -> m.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static void init() {
        register(new WTapModule());
        register(new TriggerbotModule());
        register(new AimAssistModule());
        register(new AutoTotemModule());
    }

    public static void tick() {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try { m.onTick(); } catch (Throwable ignored) {}
            }
        }
    }
}
