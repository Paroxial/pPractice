package land.pvp.practice.inventory.menu.impl;

import com.google.common.collect.ImmutableList;
import land.pvp.core.inventory.menu.action.Action;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.inventory.menu.GenericKitMenu;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.match.Match;
import land.pvp.practice.match.MatchBuilder;
import land.pvp.practice.party.Party;
import land.pvp.practice.player.PracticeProfile;

public class PartyFfaMenu extends GenericKitMenu {
    public PartyFfaMenu(PracticePlugin plugin) {
        super("Select a Kit to Fight With", plugin);
    }

    @Override
    protected Action getAction(Kit kit) {
        return player -> {
            PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
            Party party = profile.getParty();

            if (party == null || !party.isProfileLeader(profile)) {
                return;
            }

            if (party.getMembers().size() < 2) {
                player.sendMessage(CC.RED + "You need more 2 or more players in your party to start an event.");
                return;
            }

            Match match = new MatchBuilder(plugin)
                    .team(0, ImmutableList.copyOf(party.getMembers()))
                    .party(true)
                    .kit(kit)
                    .build();

            match.broadcast(CC.PRIMARY + "Starting a party FFA match with kit " + CC.SECONDARY + kit.getName() + CC.PRIMARY + ".");
            plugin.getMatchManager().startMatch(match);
        };
    }

    @Override
    public void setup() {
    }
}
