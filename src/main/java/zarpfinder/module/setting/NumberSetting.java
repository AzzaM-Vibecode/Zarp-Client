package zarpfinder.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;

public final class NumberSetting extends Setting {
    private final float min, max;
    private float step;
    private float value;

    public NumberSetting(String name, float defaultValue, float min, float max) {
        super(name);
        this.min = min; this.max = max;
        this.value = clamp(defaultValue);
    }

    public NumberSetting step(float step) {
        this.step = step;
        set(this.value);
        return this;
    }

    public float get()    { return value; }
    public int   getInt() { return Math.round(value); }
    public float min()    { return min; }
    public float max()    { return max; }

    public void set(float v) {
        float snapped = snap(v);
        if (this.value != snapped) { this.value = snapped; notifyChanged(); }
    }

    public void setSilent(float v) { this.value = snap(v); }

    public float snap(float v) {
        float c = clamp(v);
        if (step <= 0) return c;
        float snapped = Math.round((c - min) / step) * step + min;
        return Math.max(min, Math.min(max, snapped));
    }

    public String formatted() {
        return step > 0 && step < 1
                ? String.format(Locale.ROOT, "%.1f", value)
                : Integer.toString(Math.round(value));
    }

    private float clamp(float v) {
        if (Float.isNaN(v)) return value;
        return Math.max(min, Math.min(max, v));
    }

    @Override public JsonElement serialize() { return new JsonPrimitive(value); }

    @Override public void deserialize(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) {
            try { value = snap(e.getAsFloat()); } catch (RuntimeException ignored) {}
        }
    }
}
