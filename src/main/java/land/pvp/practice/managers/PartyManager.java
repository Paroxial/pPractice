package land.pvp.practice.managers;

import java.util.ArrayList;
import java.util.List;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.constants.ItemHotbars;
import land.pvp.practice.inventory.menu.impl.PartyListMenu;
import land.pvp.practice.party.Party;
import land.pvp.practice.player.PracticeProfile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PartyManager {
    private final PracticePlugin plugin;
    @Getter
    private final List<Party> parties = new ArrayList<>();

    public void createParty(Player player, PracticeProfile profile) {
        if (profile.isInParty()) {
            player.sendMessage(CC.RED + "You're already in a party!");
            return;
        }

        Party party = new Party(profile);

        party.addMember(profile);
        parties.add(party);

        ItemHotbars.PARTY_ITEMS.apply(player);

        player.sendMessage(CC.GREEN + "Your party has been created.");

        plugin.getMenuManager().getMenu(PartyListMenu.class).update();
    }

    public void joinParty(Player player, PracticeProfile profile, Party party) {
        if (profile.isInParty()) {
            player.sendMessage(CC.RED + "You are already in a party.");
            return;
        }

        if (party.isFull()) {
            player.sendMessage(CC.RED + "This party is full and can't be joined.");
            return;
        }

        if (!party.isOpen() && !party.hasInvite(player.getUniqueId())) {
            player.sendMessage(CC.RED + "You don't have an invitation to join this party.");
            return;
        }

        if (!party.isOpen()) {
            party.removeInvite(player.getUniqueId());
        }

        party.addMember(profile);
        party.broadcast(player.getDisplayName() + CC.GREEN + " has joined your party.");

        ItemHotbars.PARTY_ITEMS.apply(player);

        plugin.getMenuManager().getMenu(PartyListMenu.class).update();
    }

    public void leaveParty(Player leaver, PracticeProfile leaverProfile) {
        if (leaverProfile.getMatch() != null) {
            leaverProfile.getMatchData().setPlayerInMatch(false);
        }

        Party party = leaverProfile.getParty();

        if (party.isProfileLeader(leaverProfile)) {
            if (party.getMembers().size() == 1) {
                leaver.sendMessage(CC.RED + "Your party has been disbanded.");

                party.removeMember(leaverProfile);

                ItemHotbars.SPAWN_ITEMS.apply(leaver);

                parties.remove(party);
            } else {
                leaver.sendMessage(CC.RED + "Your party has been disbanded.");

                party.removeMember(leaverProfile);

                ItemHotbars.SPAWN_ITEMS.apply(leaver);

                PracticeProfile newLeader = party.getMembers().get(0);
                party.setLeader(newLeader);

                party.broadcast(CC.RED + "The leader has left the party. " + CC.SECONDARY
                        + newLeader.getName() + CC.PRIMARY + " has been given leadership.");

                List<PracticeProfile> oldMembers = party.getMembers();
                oldMembers.remove(newLeader);

                List<PracticeProfile> newMembers = new ArrayList<>();

                newMembers.add(newLeader);
                newMembers.addAll(oldMembers);

                party.getMembers().clear();

                party.getMembers().addAll(newMembers);
            }
        } else {
            party.broadcast(leaver.getDisplayName() + CC.RED + " has left your party.");
            party.removeMember(leaverProfile);

            ItemHotbars.SPAWN_ITEMS.apply(leaver);
        }

        plugin.getMenuManager().getMenu(PartyListMenu.class).update();
    }

    public void kickMember(Player player, PracticeProfile profile) {
        Party party = profile.getParty();

        party.broadcast(CC.GREEN + "Kicked " + player.getDisplayName() + CC.GREEN + " from the party.");
        party.removeMember(profile);
        ItemHotbars.giveSpawnItems(player, false);
        plugin.getMenuManager().getMenu(PartyListMenu.class).update();
    }
}
