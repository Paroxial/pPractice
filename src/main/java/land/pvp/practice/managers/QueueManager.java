package land.pvp.practice.managers;

import java.util.HashMap;
import java.util.Map;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.constants.ItemHotbars;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.match.MatchBuilder;
import land.pvp.practice.party.Party;
import land.pvp.practice.party.PartyState;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.queue.QueueEntry;
import land.pvp.practice.tasks.QueueSearchTask;
import land.pvp.practice.utils.MathUtil;
import land.pvp.practice.utils.StringUtil;
import land.pvp.practice.wrapper.EloWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class QueueManager {
    private final Map<PracticeProfile, QueueEntry> queueEntries = new HashMap<>();
    private final PracticePlugin plugin;
    @Getter
    @Setter
    private boolean rankedEnabled = true;

    public QueueEntry getEntry(PracticeProfile profile) {
        return queueEntries.get(profile);
    }

    public int playersQueued(String kitName, boolean ranked, boolean party) {
        return (int) queueEntries.values().stream()
                .filter(entry -> entry.isRanked() == ranked)
                .filter(entry -> entry.getKit().getName().equals(kitName))
                .filter(entry -> entry.isParty() == party)
                .count();
    }

    public void enqueueTeam(Player player, PracticeProfile profile, Kit kit, boolean ranked) {
        if (queueEntries.containsKey(profile)) {
            player.sendMessage(CC.RED + "You are already in the queue!", true);
            return;
        }

        boolean isParty = profile.isInParty();

        if (isParty && profile.getParty().getMembers().size() != 2) {
            player.sendMessage(CC.RED + "You can only join the 2v2 queue with 2 players in your party!");
            return;
        }

        EloWrapper eloWrapper = profile.getElo(kit);
        int elo = ranked ? (isParty ? eloWrapper.getPartyRating() : eloWrapper.getSoloRating()) : 0;
        QueueEntry entry = new QueueEntry(profile, kit, ranked, isParty, elo);

        if (queueSearch(entry, 50)) {
            return;
        }

        Party party = profile.getParty();

        if (isParty) {
            party.setState(PartyState.QUEUED);
        } else {
            profile.setPlayerState(PlayerState.QUEUED);
        }

        queueEntries.put(profile, entry);

        plugin.getCustomScoreboard().forceUpdate(player);
        player.closeInventory();
        ItemHotbars.QUEUE_ITEMS.apply(player);

        if (isParty) {
            party.broadcast(ranked
                    ? CC.PRIMARY + "Your party joined the ranked " + CC.SECONDARY + kit.getName()
                    + CC.PRIMARY + " queue with " + CC.SECONDARY + StringUtil.formatNumberWithCommas(elo) + CC.PRIMARY + " ELO."
                    : CC.PRIMARY + "You party joined the unranked " + CC.SECONDARY + kit.getName() + CC.PRIMARY + " queue.");
        } else {
            player.sendMessage(ranked
                            ? CC.PRIMARY + "You joined the ranked " + CC.SECONDARY + kit.getName()
                            + CC.PRIMARY + " queue with " + CC.SECONDARY + StringUtil.formatNumberWithCommas(elo) + CC.PRIMARY + " ELO."
                            : CC.PRIMARY + "You joined the unranked " + CC.SECONDARY + kit.getName() + CC.PRIMARY + " queue.",
                    true
            );
        }

        if (ranked) {
            QueueSearchTask task = new QueueSearchTask(plugin, entry);

            entry.setSearchTask(task);
            task.start();
        }
    }

    public void dequeueTeam(Player player, PracticeProfile profile) {
        plugin.getPlayerManager().resetPlayerMinimally(player, profile, false);

        QueueEntry entry = queueEntries.remove(profile);

        entry.cancelSearchTask();

        if (profile.isInParty()) {
            Party party = profile.getParty();

            party.setState(PartyState.SPAWN);
            party.broadcast(CC.RED + "Your party left the queue.");
        } else {
            plugin.getCustomScoreboard().forceUpdate(player);
            player.sendMessage(CC.RED + "You left the queue.", true);
        }
    }

    public boolean queueSearch(QueueEntry entry, int range) {
        if (queueEntries.isEmpty()) {
            return false;
        }

        QueueEntry found = queueEntries.values().stream()
                .filter(other -> other.getProfile() != entry.getProfile()
                        && other.isParty() == entry.isParty()
                        && other.isRanked() == entry.isRanked()
                        && other.getKit() == entry.getKit()
                        && MathUtil.isWithin(other.getElo(), entry.getElo(), range))
                .findAny()
                .orElse(null);

        if (found == null) {
            return false;
        }

        found.cancelSearchTask();
        queueEntries.remove(found.getProfile());

        entry.cancelSearchTask();

        PracticeProfile entryProfile = entry.getProfile();
        if (queueEntries.containsValue(entry)) {
            queueEntries.remove(entryProfile);
        }

        boolean party = entry.isParty();
        PracticeProfile foundProfile = found.getProfile();
        MatchBuilder matchBuilder = new MatchBuilder(plugin)
                .party(party)
                .ranked(found.isRanked())
                .kit(found.getKit());

        if (party) {
            Party foundParty = foundProfile.getParty();
            Party entryParty = entryProfile.getParty();

            matchBuilder.team(0, foundParty.getMembers());
            matchBuilder.team(1, entryParty.getMembers());

            String message = entry.isRanked()
                    ? CC.PRIMARY + "Ranked match found: " + CC.SECONDARY + foundProfile.getName() + "'s party " + CC.ACCENT + "(" + StringUtil.formatNumberWithCommas(found.getElo()) + " ELO) "
                    + CC.PRIMARY + "vs. " + CC.SECONDARY + entryProfile.getName() + "'s party " + CC.ACCENT + "(" + StringUtil.formatNumberWithCommas(entry.getElo()) + " ELO)"
                    : CC.PRIMARY + "Unranked match found: " + CC.SECONDARY + foundProfile.getName() + "'s party "
                    + CC.PRIMARY + "vs. " + CC.SECONDARY + entryProfile.getName() + "'s party ";

            foundParty.broadcast(message);
            entryParty.broadcast(message);
        } else {
            matchBuilder.team(0, foundProfile);
            matchBuilder.team(1, entryProfile);

            String message = entry.isRanked()
                    ? CC.PRIMARY + "Ranked match found: " + CC.SECONDARY + foundProfile.getName() + CC.ACCENT + " (" + StringUtil.formatNumberWithCommas(found.getElo()) + " ELO) "
                    + CC.PRIMARY + "vs. " + CC.SECONDARY + entryProfile.getName() + CC.ACCENT + " (" + StringUtil.formatNumberWithCommas(entry.getElo()) + " ELO)"
                    : CC.PRIMARY + "Unranked match found: " + CC.SECONDARY + foundProfile.getName()
                    + CC.PRIMARY + " vs. " + CC.SECONDARY + entryProfile.getName();

            Player foundPlayer = foundProfile.asPlayer();
            Player entryPlayer = entryProfile.asPlayer();

            foundPlayer.sendMessage(message, true);
            entryPlayer.sendMessage(message, true);
        }

        plugin.getMatchManager().startMatch(matchBuilder.build());
        return true;
    }
}
