package zarpfinder.module.impl.combat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import zarpfinder.module.Category;
import zarpfinder.module.Module;
import zarpfinder.module.setting.BooleanSetting;
import zarpfinder.module.setting.ModeSetting;
import zarpfinder.module.setting.NumberSetting;

public class TriggerbotModule extends Module {
    private final NumberSetting range =
            addSetting(new NumberSetting("Range", 4.0f, 1.0f, 6.0f).step(0.1f));
    private final BooleanSetting playersOnly =
            addSetting(new BooleanSetting("Players Only", false));
    private final ModeSetting mode =
            addSetting(new ModeSetting("Mode", "Single", "Single", "Hold"));
    private final NumberSetting cps;
    private int cooldown = 0;

    public TriggerbotModule() {
        super("Triggerbot", "Automatically attacks the entity under your crosshair", Category.COMBAT);
        this.cps = addSetting(new NumberSetting("CPS (Hold)", 8f, 2f, 20f).step(1f)
                .visibleWhen(() -> mode.is("Hold")));
    }

    @Override
    public void onTick() {
        var player = mc().player;
        if (player == null || mc().gameMode == null) return;

        Entity target = mc().crosshairPickEntity;
        if (target == null || !(target instanceof LivingEntity living) || !target.isAlive()) {
            cooldown = 0;
            return;
        }
        if (player.distanceToSqr(target) > range.get() * range.get()) return;
        if (playersOnly.get() && !(target instanceof Player)) return;

        if (mode.is("Single")) {
            if (player.getAttackStrengthScale(0.5f) >= 1.0f) attack(living);
        } else {
            if (cooldown > 0) cooldown--;
            if (cooldown == 0) {
                attack(living);
                cooldown = 20 / Math.max(1, cps.getInt());
            }
        }
    }

    private void attack(Entity target) {
        var player = mc().player;
        player.attack(target);
        player.swing(InteractionHand.MAIN_HAND);
    }
}
