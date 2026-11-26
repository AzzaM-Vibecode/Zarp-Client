package zarpfinder.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BooleanSetting extends Setting {
    private boolean value;

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    public boolean get() { return value; }

    public void set(boolean newValue) {
        if (value != newValue) { value = newValue; notifyChanged(); }
    }

    public void toggle() { set(!value); }

    @Override public JsonElement serialize() { return new JsonPrimitive(value); }

    @Override public void deserialize(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) {
            try { value = e.getAsBoolean(); } catch (RuntimeException ignored) {}
        }
    }
}
