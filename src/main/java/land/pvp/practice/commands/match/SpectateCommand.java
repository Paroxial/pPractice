package land.pvp.practice.commands.match;

import land.pvp.core.CorePlugin;
import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.player.CoreProfile;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.match.Match;
import land.pvp.practice.match.MatchState;
import land.pvp.practice.match.MatchTeam;
import land.pvp.practice.party.Party;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import org.bukkit.entity.Player;

public class SpectateCommand extends PlayerCommand {
    private final PracticePlugin plugin;

    public SpectateCommand(PracticePlugin plugin) {
        super("spectate");
        this.plugin = plugin;
        setAliases("sp", "spec", "spect");
        setUsage(CC.RED + "Usage: /spectate <player>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1 || plugin.getServer().getPlayer(args[0]) == null) {
            player.sendMessage(usageMessage);
            return;
        }

        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
        Party party = profile.getParty();

        if (party != null || profile.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(CC.RED + "You can't do this in your current state.");
            return;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        PracticeProfile targetProfile = plugin.getPlayerManager().getProfile(target.getUniqueId());

        if (targetProfile.getPlayerState() != PlayerState.FIGHTING) {
            player.sendMessage(CC.RED + "Player is not in a match.");
            return;
        }

        boolean isNotStaff = !CorePlugin.getInstance().getProfileManager().getProfile(player.getUniqueId()).hasStaff();

        if (!targetProfile.isAllowingSpectators() && isNotStaff) {
            player.sendMessage(CC.RED + "Player isn't allowing spectators");
            return;
        }

        Match targetMatch = targetProfile.getMatch();

        if (targetMatch.getMatchState() == MatchState.ENDED) {
            player.sendMessage(CC.RED + "That match just finished!");
            return;
        }

        for (MatchTeam team : targetMatch.getTeams()) {
            for (PracticeProfile practiceProfile : team.getPlayers()) {
                if (!practiceProfile.isAllowingSpectators() && isNotStaff) {
                    player.sendMessage(CC.RED + "One of the players in this match isn't allowing spectators");
                    return;
                }
            }
        }

        CoreProfile coreProfile = CorePlugin.getInstance().getProfileManager().getProfile(player.getUniqueId());

        for (MatchTeam team : targetMatch.getTeams()) {
            for (Player teamPlayer : team.alivePlayers()) {
                if (!teamPlayer.isOnline()) {
                    continue;
                }

                CoreProfile teamProfile = CorePlugin.getInstance().getProfileManager().getProfile(teamPlayer.getUniqueId());

                if (teamProfile == null) {
                    continue;
                }

                if (!coreProfile.hasStaff() || (coreProfile.hasStaff() && teamProfile.hasStaff())) {
                    teamPlayer.sendMessage(player.getDisplayName() + CC.GREEN + " is now spectating your match.");
                }
            }
        }

        plugin.getSpectatorManager().addSpectator(player, profile, target, targetMatch);
    }
}
