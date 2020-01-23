package land.pvp.practice.commands.toggle;

import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.player.PracticeProfile;
import org.bukkit.entity.Player;

public class ToggleDuelRequestsCommand extends PlayerCommand {
    private final PracticePlugin plugin;

    public ToggleDuelRequestsCommand(PracticePlugin plugin) {
        super("toggleduelrequests");
        this.plugin = plugin;
        setAliases("tdr", "toggleduels", "toggleduel");
    }

    @Override
    public void execute(Player player, String[] args) {
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
        boolean acceptingDuels = !profile.isAcceptingDuels();

        profile.setAcceptingDuels(acceptingDuels);
        player.sendMessage(acceptingDuels ? CC.GREEN + "You are now accepting duel requests." : CC.RED + "You are no longer accepting duel requests.");
    }
}
