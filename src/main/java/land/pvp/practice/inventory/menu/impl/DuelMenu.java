package land.pvp.practice.inventory.menu.impl;

import land.pvp.core.inventory.menu.action.Action;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.inventory.menu.GenericKitMenu;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.party.Party;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import org.bukkit.entity.Player;

public class DuelMenu extends GenericKitMenu {
    public DuelMenu(PracticePlugin plugin) {
        super("Select a Kit to Duel With", plugin);
    }

    @Override
    protected Action getAction(Kit kit) {
        return player -> {
            PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
            Player selected = plugin.getServer().getPlayer(profile.getDuelSelecting());

            if (selected == null) {
                player.sendMessage(CC.RED + "Player is no longer online.");
                return;
            }

            PracticeProfile targetData = plugin.getPlayerManager().getProfile(selected.getUniqueId());

            if (targetData.getPlayerState() != PlayerState.SPAWN) {
                player.sendMessage(CC.RED + "Player is not in spawn.");
                return;
            }

            Party party = profile.getParty();
            Party targetParty = targetData.getParty();
            boolean partyDuel = party != null;

            if (partyDuel && targetParty == null) {
                player.sendMessage(CC.RED + "That player is not in a party.");
                return;
            }

            player.closeInventory();

            if (kit == Kit.SUMO) {
                plugin.getMenuManager().getMenu(SumoArenaMenu.class).open(player);
            } else {
                plugin.getMenuManager().getMenu(ArenaMenu.class).open(player);
            }

            profile.setSelectedKit(kit);
        };
    }

    @Override
    public void setup() {
    }
}
