package land.pvp.practice.managers;

import land.pvp.core.CorePlugin;
import land.pvp.core.player.CoreProfile;
import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.player.PlayerUtil;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.constants.ItemHotbars;
import land.pvp.practice.match.Match;
import land.pvp.practice.match.MatchTeam;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class SpectatorManager {
    private final PracticePlugin plugin;

    public void addSpectator(Player player, PracticeProfile profile, Player target, Match targetMatch) {
        profile.setPlayerState(PlayerState.SPECTATING);
        profile.setSpectatingMatch(targetMatch);

        targetMatch.getSpectators().add(player.getUniqueId());

        PlayerUtil.clearPlayer(player);

        Location teleportTo = target.getLocation();
        teleportTo.setY(teleportTo.getBlockY() + 3.0);

        player.teleport(teleportTo);
        player.setAllowFlight(true);
        player.setFlying(true);

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.hidePlayer(player);
            player.hidePlayer(online);
        }

        for (MatchTeam team : targetMatch.getTeams()) {
            for (Player alivePlayer : team.alivePlayers()) {
                player.showPlayer(alivePlayer);
            }
        }

        MatchTeam firstTeam = targetMatch.getTeams().get(0);

        ItemHotbars.SPEC_ITEMS.apply(player);

        if (targetMatch.isFfa()) {
            player.sendMessage(CC.PRIMARY + "Now spectating "
                    + CC.ACCENT + firstTeam.getLeader().getName() + CC.PRIMARY + "'s FFA match.");
        } else {
            MatchTeam secondTeam = targetMatch.getTeams().get(1);

            targetMatch.applySpectatorScoreboard(player);

            player.sendMessage(CC.PRIMARY + "Now spectating " + CC.GREEN + firstTeam.getLeader().getName()
                    + CC.SECONDARY + " vs. " + CC.RED + secondTeam.getLeader().getName() + CC.PRIMARY + ".");
        }
    }

    public void removeSpectator(Player player, PracticeProfile profile) {
        Match match = profile.getSpectatingMatch();

        CoreProfile coreProfile = CorePlugin.getInstance().getProfileManager().getProfile(player.getUniqueId());

        if (coreProfile != null && !coreProfile.hasStaff()) {
            match.broadcast(player.getDisplayName() + CC.RED + " is no longer spectating.");
        }

        match.getSpectators().remove(player.getUniqueId());

        profile.setSpectatingMatch(null);

        player.sendMessage(CC.RED + "No longer spectating.");

        PlayerUtil.clearPlayer(player);

        plugin.getPlayerManager().resetPlayerMinimally(player, profile, true);

        if (profile.isHidingPlayers()) {
            land.pvp.practice.utils.PlayerUtil.hideAllPlayersFor(player);
        } else {
            land.pvp.practice.utils.PlayerUtil.showAllPlayersFor(player);
        }

        land.pvp.practice.utils.PlayerUtil.toggleFlyFor(player);
    }

    public void informSpectator(Player player, PracticeProfile profile) {
        Match match = profile.getSpectatingMatch();

        player.sendMessage(CC.PRIMARY + "Match Info");
        player.sendMessage(CC.PRIMARY + "Kit: " + CC.SECONDARY + match.getKit().getName());
        player.sendMessage(CC.PRIMARY + "Ranked: " + CC.SECONDARY + match.isRanked());
    }
}
