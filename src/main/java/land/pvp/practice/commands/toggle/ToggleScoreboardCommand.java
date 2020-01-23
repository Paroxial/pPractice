package land.pvp.practice.commands.toggle;

import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.player.PracticeProfile;
import org.bukkit.entity.Player;

public class ToggleScoreboardCommand extends PlayerCommand {
    private final PracticePlugin plugin;

    public ToggleScoreboardCommand(PracticePlugin plugin) {
        super("togglescoreboard");
        this.plugin = plugin;
        setAliases("sb", "sidebar", "scoreboard", "togglesidebar", "tsb");
    }

    @Override
    public void execute(Player player, String[] args) {
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
        boolean scoreboardEnabled = !profile.isScoreboardEnabled();

        profile.setScoreboardEnabled(scoreboardEnabled);
        player.sendMessage(scoreboardEnabled ? CC.GREEN + "Toggled scoreboard on." : CC.RED + "Toggled scoreboard off.");
    }
}
