package land.pvp.practice.inventory.menu.impl;

import java.util.List;
import land.pvp.core.inventory.menu.action.Action;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.inventory.menu.GenericKitMenu;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.match.Match;
import land.pvp.practice.match.MatchBuilder;
import land.pvp.practice.party.Party;
import land.pvp.practice.player.PracticeProfile;

public class PartySplitMenu extends GenericKitMenu {
    public PartySplitMenu(PracticePlugin plugin) {
        super("Select a Kit to Fight With", plugin);
    }

    @Override
    public Action getAction(Kit kit) {
        return player -> {
            PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
            Party party = profile.getParty();

            if (party == null || !party.isProfileLeader(profile)) {
                return;
            }

            player.closeInventory();

            if (party.getMembers().size() < 2) {
                player.sendMessage(CC.RED + "You need more 2 or more players in your party to start an event.");
                return;
            }

            List<List<PracticeProfile>> teams = party.split();

            Match match = new MatchBuilder(plugin)
                    .team(0, teams.get(0))
                    .team(1, teams.get(1))
                    .party(true)
                    .kit(kit)
                    .build();

            match.broadcast(CC.PRIMARY + "Starting a party split match with kit " + CC.SECONDARY + kit.getName()
                    + CC.PRIMARY + " between " + match.getTeams().get(0).getLeader().getName() + " and " + match.getTeams().get(1).getLeader().getName() + ".");
            plugin.getMatchManager().startMatch(match);
        };
    }

    @Override
    public void setup() {
    }
}
