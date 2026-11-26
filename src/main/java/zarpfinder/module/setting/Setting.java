package zarpfinder.module.setting;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public abstract class Setting {
    private final String name;
    private final List<Runnable> changeListeners = new ArrayList<>(1);
    private BooleanSupplier visibility;

    protected Setting(String name) { this.name = name; }

    public final String name() { return name; }

    public final Setting onChange(Runnable listener) {
        changeListeners.add(listener);
        return this;
    }

    protected final void notifyChanged() {
        for (Runnable l : changeListeners) {
            try { l.run(); } catch (Throwable ignored) {}
        }
    }

    public final Setting visibleWhen(BooleanSupplier condition) {
        this.visibility = condition;
        return this;
    }

    public final boolean isVisible() {
        return visibility == null || visibility.getAsBoolean();
    }

    public abstract JsonElement serialize();
    public abstract void deserialize(JsonElement element);
}
