package land.pvp.practice.inventory.menu.impl;

import java.util.ArrayList;
import java.util.List;
import land.pvp.core.inventory.menu.Menu;
import land.pvp.core.utils.item.ItemBuilder;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.party.Party;
import land.pvp.practice.player.PracticeProfile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PartyListMenu extends Menu {
    private final PracticePlugin plugin;

    public PartyListMenu(PracticePlugin plugin) {
        super(4, "Select a Party to Duel");
        this.plugin = plugin;
    }

    @Override
    public void setup() {
    }

    @Override
    public void update() {
        if (getInventory().getContents().length != 0) {
            clear();
        }

        int count = 0;

        for (Party party : plugin.getPartyManager().getParties()) {
            List<String> members = new ArrayList<>();

            for (PracticeProfile member : party.getMembers()) {
                members.add(CC.PRIMARY + member.getName());
            }

            ItemStack skull = new ItemBuilder(Material.SKULL_ITEM)
                    .name(CC.PRIMARY + party.getLeader().getName() + CC.SECONDARY + "'s Party "
                            + CC.ACCENT + "(" + party.getMembers().size() + ")")
                    .lore(members.toArray(new String[0]))
                    .build();

            setActionableItem(count++, skull, player -> {
                player.closeInventory();
                player.performCommand("duel " + party.getLeader().getName());
            });
        }
    }
}
