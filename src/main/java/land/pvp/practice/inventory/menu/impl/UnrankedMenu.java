package land.pvp.practice.inventory.menu.impl;

import land.pvp.core.inventory.menu.Menu;
import land.pvp.core.utils.item.ItemBuilder;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.kit.Kit;
import org.bukkit.inventory.ItemStack;

public class UnrankedMenu extends Menu {
    private final PracticePlugin plugin;

    public UnrankedMenu(PracticePlugin plugin) {
        super(2, "Select an Unranked Kit");
        this.plugin = plugin;
    }

    @Override
    public void setup() {
    }

    @Override
    public void update() {
        int count = 0;

        for (Kit kit : Kit.values()) {
            int queueCount = plugin.getQueueManager().playersQueued(kit.getName(), false, false);
            int fighting = plugin.getMatchManager().playersFighting(kit.getName(), false);

            ItemStack icon = ItemBuilder
                    .from(kit.getIcon())
                    .lore(CC.PRIMARY + "Playing: " + CC.SECONDARY + fighting,
                            CC.PRIMARY + "Queued: " + CC.SECONDARY + queueCount)
                    .build();

            setActionableItem(count++, icon, player -> plugin.getQueueManager().enqueueTeam(player, plugin.getPlayerManager().getProfile(player.getUniqueId()), kit, false));
        }
    }
}
