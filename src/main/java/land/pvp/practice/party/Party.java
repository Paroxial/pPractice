package land.pvp.practice.party;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.message.ClickableMessage;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.utils.collect.set.ExpiringHashSet;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Setter
public class Party {
    private static final int MAX_PARTY_SIZE = 60;
    private final Set<UUID> invitedIds = new ExpiringHashSet<>(30);
    @Getter
    private final List<PracticeProfile> members = new ArrayList<>();
    @Getter
    private PartyState state = PartyState.SPAWN;
    @Getter
    private PracticeProfile leader;
    @Getter
    private boolean open;

    public Party(PracticeProfile leader) {
        this.leader = leader;
    }

    public void addMember(PracticeProfile profile) {
        members.add(profile);
        profile.setParty(this);
    }

    public void removeMember(PracticeProfile profile) {
        if (profile.isInMatch()) {
            profile.getMatchData().setPlayerInMatch(false);
        }

        members.remove(profile);
        profile.setParty(null);
    }

    public boolean isFull() {
        return members.size() == MAX_PARTY_SIZE;
    }

    public void addInvite(UUID id) {
        invitedIds.add(id);
    }

    public boolean hasInvite(UUID id) {
        return invitedIds.contains(id);
    }

    public void removeInvite(UUID id) {
        invitedIds.remove(id);
    }

    public void broadcast(String msg) {
        for (PracticeProfile member : members) {
            Player player = member.asPlayer();

            if (player != null && player.isOnline()) {
                player.sendMessage(msg, true);
            }
        }
    }


    public void broadcast(ClickableMessage msg) {
        for (PracticeProfile member : members) {
            Player player = member.asPlayer();

            if (player != null && player.isOnline()) {
                msg.sendToPlayer(player, true);
            }
        }
    }

    public void displayPartyInfo(Player player) {
        player.sendMessage(CC.PRIMARY + "Party Info");
        int count = 0;

        for (PracticeProfile profile : members) {
            count++;
            player.sendMessage(isProfileLeader(profile)
                    ? CC.PRIMARY + count + ". " + CC.ACCENT + "(Leader) " + CC.SECONDARY + profile.getName()
                    : CC.PRIMARY + count + ". " + CC.SECONDARY + profile.getName());
        }
    }

    public boolean isProfileLeader(PracticeProfile profile) {
        return leader == profile;
    }

    public List<List<PracticeProfile>> split() {
        List<PracticeProfile> teamA = new ArrayList<>();
        List<PracticeProfile> teamB = new ArrayList<>();

        boolean which = ThreadLocalRandom.current().nextBoolean();

        for (PracticeProfile member : members) {
            if (teamA.size() == teamB.size()) {
                teamA.add(member);
            } else {
                teamB.add(member);
            }
        }

        return ImmutableList.of(teamA, teamB);
    }
}
