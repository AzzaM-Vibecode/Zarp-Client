package zarpfinder.module.impl.combat;

import net.minecraft.client.player.LocalPlayer;
import zarpfinder.module.Category;
import zarpfinder.module.Module;
import zarpfinder.module.setting.BooleanSetting;
import zarpfinder.module.setting.NumberSetting;

public class WTapModule extends Module {
    private final NumberSetting pauseTicks =
            addSetting(new NumberSetting("Sprint Pause Ticks", 2f, 1f, 6f).step(1f));
    private final BooleanSetting onlyWhenSprinting =
            addSetting(new BooleanSetting("Only When Sprinting", true));

    private int counter = 0;

    public WTapModule() {
        super("W-Tap", "Resets sprint knockback while attacking", Category.COMBAT);
    }

    @Override
    public void onTick() {
        LocalPlayer p = mc().player;
        if (p == null) return;

        boolean attacking = mc().options.keyAttack.isDown();
        boolean moving = mc().options.keyUp.isDown();

        if (!attacking || !moving) {
            counter = 0;
            mc().options.keySprint.setDown(true);
            return;
        }
        if (onlyWhenSprinting.get() && !p.isSprinting()) return;

        counter++;
        if (counter >= pauseTicks.getInt()) {
            counter = 0;
            mc().options.keySprint.setDown(true);
        } else {
            mc().options.keySprint.setDown(false);
        }
    }

    @Override
    protected void onDisable() {
        if (mc().options != null) mc().options.keySprint.setDown(true);
    }
}
