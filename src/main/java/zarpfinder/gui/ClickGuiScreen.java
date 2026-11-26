package zarpfinder.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import zarpfinder.ConfigManager;
import zarpfinder.Theme;
import zarpfinder.ZarpFinderMod;
import zarpfinder.module.Category;
import zarpfinder.module.Module;
import zarpfinder.module.ModuleManager;
import zarpfinder.module.setting.BooleanSetting;
import zarpfinder.module.setting.ModeSetting;
import zarpfinder.module.setting.NumberSetting;
import zarpfinder.module.setting.Setting;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {
    private enum Tab { COMBAT, MOVEMENT, DONUTSMP, VISUALS, MISC, SETTINGS, CONFIGS, THEME, SOCIALS }

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static final ResourceLocation LOGO =
            ResourceLocation.fromNamespaceAndPath("zarpfinder", "textures/logo.png");
    private static final int CARD_H = 26;
    private static final int ROW_H = 22;
    private static final int PAD = 8;

    private int px, py, pw, ph, sidebarW, headerH, contentX, contentY, contentW, contentH;

    private Tab tab = Tab.COMBAT;
    private final Map<Module, Boolean> expanded = new HashMap<>();
    private String search = "";
    private boolean searchFocus = false;
    private int scroll = 0;
    private NumberSetting dragging = null;

    private int saveBtnX, saveBtnY, loadBtnX, loadBtnY;
    private int[] swatchRects = new int[0];

    public ClickGuiScreen() {
        super(Component.literal("Zarp Finder"));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private void layout() {
        pw = Mth.clamp((int) (this.width * 0.62), 320, this.width - 20);
        ph = Mth.clamp((int) (this.height * 0.68), 240, this.height - 20);
        px = (this.width - pw) / 2;
        py = (this.height - ph) / 2;
        sidebarW = Mth.clamp(pw / 4, 120, 190);
        headerH = 44;
        contentX = px + sidebarW + PAD;
        contentY = py + headerH + PAD;
        contentW = pw - sidebarW - PAD * 2;
        contentH = ph - headerH - PAD * 2;
    }

    private boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private boolean isModuleTab() {
        return tab == Tab.COMBAT || tab == Tab.MOVEMENT || tab == Tab.DONUTSMP
                || tab == Tab.VISUALS || tab == Tab.MISC;
    }

    private Category currentCategory() {
        return switch (tab) {
            case COMBAT -> Category.COMBAT;
            case MOVEMENT -> Category.MOVEMENT;
            case DONUTSMP -> Category.DONUTSMP;
            case VISUALS -> Category.VISUALS;
            default -> Category.MISC;
        };
    }

    private List<Module> visibleModules() {
        List<Module> list = ModuleManager.byCategory(currentCategory());
        if (search != null && !search.isEmpty()) {
            String q = search.toLowerCase();
            list = list.stream()
                    .filter(m -> m.name().toLowerCase().contains(q))
                    .toList();
        }
        return list;
    }

    private int moduleHeight(Module m) {
        int rows = (int) m.settings().stream().filter(Setting::isVisible).count();
        boolean open = expanded.getOrDefault(m, false);
        return CARD_H + (open ? rows * ROW_H + 4 : 0);
    }

    private int contentHeightNeeded() {
        int h = 0;
        for (Module m : visibleModules()) h += moduleHeight(m) + 6;
        return h;
    }

    private Module cardAt(double mx, double my) {
        if (!in(mx, my, contentX, contentY, contentW, contentH)) return null;
        int y = contentY - scroll;
        for (Module m : visibleModules()) {
            int h = moduleHeight(m);
            if (my >= y && my <= y + h) return m;
            y += h + 6;
        }
        return null;
    }

    private Setting settingRowAt(Module card, double mx, double my) {
        int y = contentY - scroll;
        for (Module m : visibleModules()) {
            int h = moduleHeight(m);
            if (m == card) {
                int rowsTop = y + CARD_H + 4;
                if (my >= rowsTop) {
                    int rowIdx = (int) ((my - rowsTop) / ROW_H);
                    int i = 0;
                    for (Setting s : card.settings()) {
                        if (!s.isVisible()) continue;
                        if (i == rowIdx) return s;
                        i++;
                    }
                }
                return null;
            }
            y += h + 6;
        }
        return null;
    }

    private int[] sliderTrack() {
        int tw = contentW / 2 - 46;
        return new int[]{contentX + contentW - 38 - tw, tw};
    }

    private void updateSliderFromMouse(NumberSetting n, double mx) {
        int[] t = sliderTrack();
        float frac = Mth.clamp((float) ((mx - t[0]) / t[1]), 0f, 1f);
        n.setSilent(n.min() + frac * (n.max() - n.min()));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        layout();
        g.fill(0, 0, this.width, this.height, 0x88000000);
        g.fill(px, py, px + pw, py + ph, 0xF0101526);
        border(g, px, py, pw, ph, 0xFF1E2A45);

        drawSidebar(g, mouseX, mouseY);
        drawHeader(g, mouseX, mouseY);
        drawContent(g, mouseX, mouseY);
        super.render(g, mouseX, mouseY, delta);
    }

    private void drawLogo(GuiGraphics g, int x, int y, int size) {
        g.blit(RenderType::guiTextured, LOGO, x, y, 0.0f, 0.0f, size, size, size, size);
    }

    private void drawSidebar(GuiGraphics g, int mouseX, int mouseY) {
        int x = px, y = py;
        g.fill(x, y, x + sidebarW, y + 54, 0x30000000);
        drawLogo(g, x + 8, y + 8, 32);
        g.drawString(this.font, "ZARP FINDER", x + 46, y + 10, 0xFFFFFFFF, true);
        g.drawString(this.font, "v" + ZarpFinderMod.VERSION, x + 46, y + 24, Theme.accent, true);
        y += 54;

        Tab[] moduleTabs = {Tab.COMBAT, Tab.MOVEMENT, Tab.DONUTSMP, Tab.VISUALS, Tab.MISC};
        Tab[] extraTabs = {Tab.SETTINGS, Tab.CONFIGS, Tab.THEME, Tab.SOCIALS};

        y = drawTabList(g, mouseX, mouseY, moduleTabs, y, true);
        g.fill(x + 10, y + 4, x + sidebarW - 10, y + 5, 0xFF1E2A45);
        y = drawTabList(g, mouseX, mouseY, extraTabs, y + 14, false);

        int pcy = py + ph - 58;
        g.fill(x, pcy, x + sidebarW, pcy + 58, 0x22000000);
        String name = Minecraft.getInstance().getUser().getName();
        int hsz = 32;
        g.fill(x + 8, pcy + 8, x + 8 + hsz, pcy + 8 + hsz, withAlpha(Theme.accent, 0x40));
        border(g, x + 8, pcy + 8, hsz, hsz, withAlpha(Theme.accent, 0x90));
        g.drawString(this.font, name.substring(0, 1).toUpperCase(), x + 8 + hsz / 2 - 4,
                pcy + 8 + hsz / 2 - 4, 0xFFFFFFFF, true);
        g.drawString(this.font, name, x + 8, pcy + 44, 0xFFE8EAF0, true);
    }

    private int drawTabList(GuiGraphics g, int mx, int my, Tab[] tabs, int y, boolean moduleTabs) {
        for (Tab t : tabs) {
            String label = switch (t) {
                case COMBAT -> "Combat";
                case MOVEMENT -> "Movement";
                case DONUTSMP -> "DonutSMP";
                case VISUALS -> "Visuals";
                case MISC -> "Misc";
                case SETTINGS -> "Settings";
                case CONFIGS -> "Configs";
                case THEME -> "Theme";
                case SOCIALS -> "Socials";
            };
            int h = 24;
            boolean selected = tab == t;
            boolean hovered = in(mx, my, px, y, sidebarW, h);
            int bg = selected ? withAlpha(Theme.accent, 0x30) : hovered ? 0x16FFFFFF : 0x00000000;
            g.fill(px, y, px + sidebarW, y + h, bg);
            if (selected) g.fill(px, y, px + 3, y + h, Theme.accent);
            g.drawString(this.font, label, px + 14, y + 8, 0xFFE8EAF0, true);
            if (moduleTabs) {
                long count = ModuleManager.byCategory(categoryOf(t)).size();
                if (count > 0) {
                    String c = Long.toString(count);
                    g.drawString(this.font, c, px + sidebarW - 10 - this.font.width(c), y + 8, 0xFF667085, true);
                }
            }
            y += h;
        }
        return y;
    }

    private Category categoryOf(Tab t) {
        return switch (t) {
            case COMBAT -> Category.COMBAT;
            case MOVEMENT -> Category.MOVEMENT;
            case DONUTSMP -> Category.DONUTSMP;
            case VISUALS -> Category.VISUALS;
            default -> Category.MISC;
        };
    }

    private Tab tabAt(double mx, double my) {
        Tab[] moduleTabs = {Tab.COMBAT, Tab.MOVEMENT, Tab.DONUTSMP, Tab.VISUALS, Tab.MISC};
        Tab[] extraTabs = {Tab.SETTINGS, Tab.CONFIGS, Tab.THEME, Tab.SOCIALS};
        int y = py + 54;
        for (Tab t : moduleTabs) {
            if (in(mx, my, px, y, sidebarW, 24)) return t;
            y += 24;
        }
        y += 18;
        for (Tab t : extraTabs) {
            if (in(mx, my, px, y, sidebarW, 24)) return t;
            y += 24;
        }
        return null;
    }

    private void drawHeader(GuiGraphics g, int mouseX, int mouseY) {
        int hx = contentX, hy = py + 8;
        String hello = "Hello, " + Minecraft.getInstance().getUser().getName();
        g.drawString(this.font, hello, hx, hy + 6, 0xFFE8EAF0, true);

        String time = LocalTime.now().format(CLOCK);
        int sx = contentX + contentW - 120;
        int tw = this.font.width(time);
        g.drawString(this.font, time, sx - tw - 10, hy + 6, Theme.accent, true);

        g.fill(sx, hy, sx + 120, hy + 20, 0x33000000);
        border(g, sx, hy, 120, 20, searchFocus ? Theme.accent : 0xFF2A3450);
        String shown = search.isEmpty() && !searchFocus ? "Search... Ctrl+K"
                : search + (searchFocus ? "_" : "");
        g.drawString(this.font, shown, sx + 6, hy + 6,
                search.isEmpty() && !searchFocus ? 0xFF667085 : 0xFFE8EAF0, true);
    }

    private void drawContent(GuiGraphics g, int mouseX, int mouseY) {
        if (isModuleTab()) {
            drawModuleCards(g, mouseX, mouseY);
        } else {
            int y = contentY;
            switch (tab) {
                case SETTINGS -> {
                    g.drawString(this.font, "Zarp Finder v" + ZarpFinderMod.VERSION, contentX, y, 0xFFE8EAF0, true);
                    g.drawString(this.font, "Minecraft 1.21.11 / Fabric Loader 0.20.1", contentX, y + 14, 0xFF8892A6, true);
                    g.drawString(this.font, "Open GUI: Right Shift (Controls > Zarp Finder)", contentX, y + 28, 0xFF8892A6, true);
                    g.drawString(this.font, "Modules loaded: " + ModuleManager.all().size(), contentX, y + 42, 0xFF8892A6, true);
                    g.drawString(this.font, "Right-click a module card to open settings.", contentX, y + 56, Theme.accent, true);
                }
                case CONFIGS -> {
                    saveBtnX = contentX; saveBtnY = y;
                    loadBtnX = contentX + 130; loadBtnY = y;
                    drawButton(g, saveBtnX, saveBtnY, 120, 20, "Save Config", mouseX, mouseY);
                    drawButton(g, loadBtnX, loadBtnY, 120, 20, "Load Config", mouseX, mouseY);
                    g.drawString(this.font, "Config file: config/zarpfinder.json", contentX, y + 30, 0xFF667085, true);
                }
                case THEME -> {
                    g.drawString(this.font, "Accent Color", contentX, y, 0xFFE8EAF0, true);
                    swatchRects = new int[Theme.SWATCHES.length];
                    for (int i = 0; i < Theme.SWATCHES.length; i++) {
                        int sxp = contentX + i * 30, syp = y + 12;
                        swatchRects[i] = sxp;
                        g.fill(sxp, syp, sxp + 22, syp + 22, Theme.SWATCHES[i]);
                        if (Theme.accent == Theme.SWATCHES[i]) {
                            border(g, sxp - 1, syp - 1, 24, 24, 0xFFFFFFFF);
                        }
                    }
                }
                case SOCIALS -> {
                    g.drawString(this.font, "Discord: coming soon", contentX, y, 0xFF8892A6, true);
                    g.drawString(this.font, "YouTube: coming soon", contentX, y + 14, 0xFF8892A6, true);
                }
                default -> {}
            }
        }
    }

    private void drawButton(GuiGraphics g, int x, int y, int w, int h, String label, int mx, int my) {
        boolean hovered = in(mx, my, x, y, w, h);
        g.fill(x, y, x + w, y + h, hovered ? withAlpha(Theme.accent, 0x40) : 0x22FFFFFF);
        border(g, x, y, w, h, Theme.accent);
        g.drawString(this.font, label, x + (w - this.font.width(label)) / 2, y + 6, 0xFFE8EAF0, true);
    }

    private void drawModuleCards(GuiGraphics g, int mouseX, int mouseY) {
        List<Module> mods = visibleModules();
        int maxScroll = Math.max(0, contentHeightNeeded() - contentH);
        scroll = Mth.clamp(scroll, 0, maxScroll);

        if (mods.isEmpty()) {
            g.drawString(this.font, "No modules yet", contentX + 10, contentY + 4, 0xFF667085, true);
            return;
        }

        int y = contentY - scroll;
        for (Module m : mods) {
            int h = moduleHeight(m);
            if (y > contentY + contentH || y + h < contentY) {
                y += h + 6;
                continue;
            }
            boolean hovered = in(mouseX, mouseY, contentX, y, contentW, CARD_H);
            g.fill(contentX, y, contentX + contentW, y + CARD_H, hovered ? 0x26FFFFFF : 0x1AFFFFFF);
            border(g, contentX, y, contentW, CARD_H, m.isEnabled() ? Theme.accent : 0xFF2A3450);

            g.drawString(this.font, m.name(), contentX + 10, y + 9,
                    m.isEnabled() ? 0xFFFFFFFF : 0xFFB8C0D0, true);
            g.drawString(this.font, "KeyBind: None", contentX + 20 + this.font.width(m.name()),
                    y + 9, 0xFF667085, true);

            int cx = contentX + contentW - 16, cy = y + CARD_H / 2;
            g.fill(cx - 8, cy - 8, cx + 8, cy + 8, m.isEnabled() ? Theme.accent : 0xFF3A4256);
            g.fill(cx - 4, cy - 4, cx + 4, cy + 4, 0xF0101526);

            if (expanded.getOrDefault(m, false)) {
                int ry = y + CARD_H + 4;
                for (Setting s : m.settings()) {
                    if (!s.isVisible()) continue;
                    drawSettingRow(g, s, ry, mouseX, mouseY);
                    ry += ROW_H;
                }
            }
            y += h + 6;
        }
    }

    private void drawSettingRow(GuiGraphics g, Setting s, int ry, int mx, int my) {
        boolean rHover = in(mx, my, contentX + 2, ry, contentW - 4, ROW_H);
        g.fill(contentX + 2, ry, contentX + contentW - 2, ry + ROW_H, rHover ? 0x20FFFFFF : 0x12FFFFFF);

        if (s instanceof BooleanSetting b) {
            g.drawString(this.font, s.name(), contentX + 14, ry + 7, 0xFFE8EAF0, true);
            int tx = contentX + contentW - 34, ty = ry + 5;
            g.fill(tx, ty, tx + 24, ty + 12, b.get() ? Theme.accent : 0xFF3A4256);
            int knob = b.get() ? tx + 14 : tx + 2;
            g.fill(knob, ty + 2, knob + 8, ty + 10, 0xFFFFFFFF);
        } else if (s instanceof NumberSetting n) {
            g.drawString(this.font, s.name(), contentX + 14, ry + 7, 0xFFE8EAF0, true);
            g.drawString(this.font, n.formatted(), contentX + contentW - 14 - this.font.width(n.formatted()),
                    ry + 7, Theme.accent, true);
            int[] t = sliderTrack();
            int tx = t[0], tw = t[1];
            g.fill(tx, ry + ROW_H / 2 - 1, tx + tw, ry + ROW_H / 2 + 1, 0xFF3A4256);
            float frac = (n.get() - n.min()) / (n.max() - n.min());
            int handle = tx + (int) (tw * frac);
            g.fill(tx, ry + ROW_H / 2 - 1, handle, ry + ROW_H / 2 + 1, Theme.accent);
            g.fill(handle - 3, ry + ROW_H / 2 - 4, handle + 3, ry + ROW_H / 2 + 4, 0xFFFFFFFF);
        } else if (s instanceof ModeSetting ms) {
            g.drawString(this.font, s.name(), contentX + 14, ry + 7, 0xFFE8EAF0, true);
            String v = ms.get();
            g.drawString(this.font, v, contentX + contentW - 14 - this.font.width(v), ry + 7, Theme.accent, true);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        layout();
        int sx = contentX + contentW - 120;
        if (button == 0 && in(mx, my, sx, py + 8, 120, 20)) {
            searchFocus = true;
            return true;
        }

        Tab t = tabAt(mx, my);
        if (t != null) {
            tab = t;
            searchFocus = false;
            scroll = 0;
            return true;
        }

        if (button == 0 && tab == Tab.THEME) {
            for (int i = 0; i < swatchRects.length; i++) {
                if (in(mx, my, swatchRects[i], contentY + 12, 22, 22)) {
                    Theme.accent = Theme.SWATCHES[i];
                    return true;
                }
            }
        }

        if (button == 0 && tab == Tab.CONFIGS) {
            if (in(mx, my, saveBtnX, saveBtnY, 120, 20)) { ConfigManager.save(); return true; }
            if (in(mx, my, loadBtnX, loadBtnY, 120, 20)) { ConfigManager.load(); return true; }
        }

        if (isModuleTab()) {
            Module card = cardAt(mx, my);
            if (card != null) {
                if (button == 1) {
                    if (card.settings().stream().anyMatch(Setting::isVisible)) {
                        expanded.merge(card, Boolean.TRUE, (a, b) -> !a);
                    }
                    return true;
                }
                if (button == 0) {
                    Setting row = settingRowAt(card, mx, my);
                    if (row != null) {
                        if (row instanceof BooleanSetting b) b.toggle();
                        else if (row instanceof ModeSetting m) m.next();
                        else if (row instanceof NumberSetting n) {
                            dragging = n;
                            updateSliderFromMouse(n, mx);
                        }
                        ConfigManager.save();
                        return true;
                    }
                    card.toggle();
                    return true;
                }
            }
        }

        if (button == 0) searchFocus = false;
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            updateSliderFromMouse(dragging, mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging != null) {
            dragging.set(dragging.get());
            dragging = null;
            ConfigManager.save();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (isModuleTab()) {
            int maxScroll = Math.max(0, contentHeightNeeded() - contentH);
            scroll = Mth.clamp(scroll - (int) scrollY * 14, 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_K && hasControlDown()) {
            searchFocus = !searchFocus;
            return true;
        }
        if (searchFocus) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocus = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!search.isEmpty()) search = search.substring(0, search.length() - 1);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (searchFocus && !Character.isISOControl(c) && search.length() < 32) {
            search += c;
            return true;
        }
        return super.charTyped(c, modifiers);
    }
}
