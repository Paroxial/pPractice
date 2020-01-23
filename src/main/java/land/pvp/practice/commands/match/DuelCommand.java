package land.pvp.practice.commands.match;

import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.inventory.menu.impl.DuelMenu;
import land.pvp.practice.party.Party;
import land.pvp.practice.player.MatchRequestHandler;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import org.bukkit.entity.Player;

public class DuelCommand extends PlayerCommand {
    private final PracticePlugin plugin;

    public DuelCommand(PracticePlugin plugin) {
        super("duel");
        this.plugin = plugin;
        setUsage(CC.RED + "Usage: /duel <player>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1 || plugin.getServer().getPlayer(args[0]) == null) {
            player.sendMessage(usageMessage);
            return;
        }

        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (profile.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(CC.RED + "You can't do this in your current state.");
            return;
        }

        Player target = plugin.getServer().getPlayer(args[0]);

        if (player.getName().equals(target.getName())) {
            player.sendMessage(CC.RED + "You can't duel yourself.");
            return;
        }

        PracticeProfile targetProfile = plugin.getPlayerManager().getProfile(target.getUniqueId());
        MatchRequestHandler handler = targetProfile.getMatchRequestHandler();

        if (handler.getMatchRequest(player.getUniqueId()) != null) {
            player.sendMessage(CC.RED + "You already sent a match request to that player.\nPlease wait until it expires.");
            return;
        }

        if (targetProfile.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(CC.RED + "That player isn't in spawn.");
            return;
        }

        if (!targetProfile.isAcceptingDuels()) {
            player.sendMessage(CC.RED + "That player isn't accepting duel requests.");
            return;
        }

        Party party = profile.getParty();

        if (party != null && !party.isProfileLeader(profile)) {
            player.sendMessage(CC.RED + "You aren't the leader!");
            return;
        }

        profile.setDuelSelecting(target.getUniqueId());
        plugin.getMenuManager().getMenu(DuelMenu.class).open(player);
    }
}
