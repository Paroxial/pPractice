package land.pvp.practice.inventory.menu.impl;

import land.pvp.core.inventory.menu.Menu;
import land.pvp.core.inventory.menu.action.Action;
import land.pvp.core.utils.item.ItemBuilder;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import org.bukkit.Material;

public class PartyEventMenu extends Menu {
    private final PracticePlugin plugin;

    public PartyEventMenu(PracticePlugin plugin) {
        super(1, "Select a Party Event");
        this.plugin = plugin;
    }

    private Action getAction(Class<? extends Menu> clazz) {
        return player -> {
            player.closeInventory();
            plugin.getMenuManager().getMenu(clazz).open(player);
        };
    }

    @Override
    public void setup() {
        setActionableItem(0, new ItemBuilder(Material.LEASH)
                .name(CC.PRIMARY + "Team Split Fight")
                .lore(CC.SECONDARY + "Start a team fight with party members split randomly.")
                .build(), getAction(PartySplitMenu.class));

        setActionableItem(4, new ItemBuilder(Material.NETHER_STAR)
                .name(CC.PRIMARY + "FFA Fight")
                .lore(CC.SECONDARY + "Start a free-for-all fight with your party members.")
                .build(), getAction(PartyFfaMenu.class));

        setActionableItem(8, new ItemBuilder(Material.REDSTONE)
                .name(CC.PRIMARY + "Redrover Fight")
                .lore(CC.SECONDARY + "Start a redrover fight with two captains",
                        CC.SECONDARY + "who pick team members to fight to the death.")
                .build(), player -> {
            player.closeInventory();
            player.sendMessage(CC.RED + "The Redrover event is disabled for now.");
        });
    }

    @Override
    public void update() {
    }
}
