package zarpfinder.module.impl.combat;

import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import zarpfinder.module.Category;
import zarpfinder.module.Module;
import zarpfinder.module.setting.BooleanSetting;
import zarpfinder.module.setting.NumberSetting;

public class AutoTotemModule extends Module {
    private final NumberSetting health =
            addSetting(new NumberSetting("Health Threshold", 12f, 2f, 36f).step(1f));
    private final BooleanSetting always =
            addSetting(new BooleanSetting("Always Keep Totem", true));

    public AutoTotemModule() {
        super("Auto Totem", "Automatically puts a totem in your offhand", Category.COMBAT);
    }

    @Override
    public void onTick() {
        var player = mc().player;
        if (player == null || mc().gameMode == null) return;
        if (!player.getOffhandItem().isEmpty()) return;
        if (!always.get() && player.getHealth() > health.get()) return;

        var menu = player.inventoryMenu;
        for (int slot = 9; slot <= 44; slot++) {
            if (menu.getSlot(slot).getItem().is(Items.TOTEM_OF_UNDYING)) {
                mc().gameMode.handleInventoryMouseClick(
                        menu.containerId, slot, 40, ClickType.SWAP, player);
                return;
            }
        }
    }
}
