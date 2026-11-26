package zarpfinder.module.impl.combat;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import zarpfinder.module.Category;
import zarpfinder.module.Module;
import zarpfinder.module.setting.BooleanSetting;
import zarpfinder.module.setting.NumberSetting;

public class AimAssistModule extends Module {
    private final NumberSetting range =
            addSetting(new NumberSetting("Range", 4.5f, 2.0f, 6.0f).step(0.1f));
    private final NumberSetting fov =
            addSetting(new NumberSetting("FOV", 90f, 10f, 180f).step(5f));
    private final NumberSetting speed =
            addSetting(new NumberSetting("Speed", 0.4f, 0.05f, 1.0f).step(0.05f));
    private final BooleanSetting onlyWhenAttacking =
            addSetting(new BooleanSetting("Only While Attacking", true));
    private final BooleanSetting ignorePlayers =
            addSetting(new BooleanSetting("Ignore Players", false));

    public AimAssistModule() {
        super("Aim Assist", "Smoothly aims toward the nearest target", Category.COMBAT);
    }

    @Override
    public void onTick() {
        var player = mc().player;
        if (player == null || mc().level == null) return;
        if (onlyWhenAttacking.get() && !mc().options.keyAttack.isDown()) return;

        LivingEntity best = null;
        double bestDist = range.get() * range.get();

        for (Entity e : mc().level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity living)) continue;
            if (e == player || !e.isAlive()) continue;
            if (ignorePlayers.get() && e instanceof Player) continue;

            double dx = e.getX() - player.getX();
            double dy = e.getEyeY() - player.getEyeY();
            double dz = e.getZ() - player.getZ();
            double dist = dx * dx + dy * dy + dz * dz;
            if (dist > bestDist) continue;

            float yawTo = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float pitchTo = (float) (-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
            if (Mth.degreesDifferenceAbs(player.getYRot(), yawTo) > fov.get() / 2f) continue;

            best = living;
            bestDist = dist;
        }

        if (best == null) return;

        double dx = best.getX() - player.getX();
        double dy = best.getEyeY() - player.getEyeY();
        double dz = best.getZ() - player.getZ();

        float yawTo = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitchTo = (float) (-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));

        float yawDiff = Mth.degreesDifference(player.getYRot(), yawTo);
        float pitchDiff = pitchTo - player.getXRot();
        float step = speed.get() * 2.0f;

        player.setYRot(player.getYRot() + Mth.clamp(yawDiff, -step, step));
        player.setXRot(Mth.clamp(player.getXRot() + Mth.clamp(pitchDiff, -step, step), -90f, 90f));
    }
}
