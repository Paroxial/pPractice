package land.pvp.practice.commands.toggle;

import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.utils.PlayerUtil;
import org.bukkit.entity.Player;

public class TogglePlayerVisibilityCommand extends PlayerCommand {
    private final PracticePlugin plugin;

    public TogglePlayerVisibilityCommand(PracticePlugin plugin) {
        super("toggleplayervisibility");
        this.plugin = plugin;
        setAliases("toggleplayers", "showplayers", "hideplayers", "tpv");
    }

    @Override
    public void execute(Player player, String[] args) {
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (profile.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(CC.RED + "You must be in spawn to toggle player visibility.");
            return;
        }

        boolean hidingPlayers = !profile.isHidingPlayers();

        profile.setHidingPlayers(hidingPlayers);

        if (hidingPlayers) {
            PlayerUtil.hideAllPlayersFor(player);
        } else {
            PlayerUtil.showAllPlayersFor(player);
        }

        player.sendMessage(hidingPlayers ? CC.GREEN + "You are now hiding players in spawn." : CC.RED + "You are no longer hiding players in spawn.");
    }
}
