package zarpfinder.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
import java.util.List;

public final class ModeSetting extends Setting {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = List.copyOf(Arrays.asList(modes));
        this.index = Math.max(0, this.modes.indexOf(defaultMode));
    }

    public String get() { return modes.get(index); }
    public boolean is(String m) { return get().equals(m); }
    public List<String> modes() { return modes; }

    public void set(String mode) {
        int idx = modes.indexOf(mode);
        if (idx >= 0 && idx != index) { index = idx; notifyChanged(); }
    }

    public void next() {
        if (modes.size() > 1) { index = (index + 1) % modes.size(); notifyChanged(); }
    }

    @Override public JsonElement serialize() { return new JsonPrimitive(get()); }

    @Override public void deserialize(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) {
            int idx = modes.indexOf(e.getAsString());
            if (idx >= 0) index = idx;
        }
    }
}
