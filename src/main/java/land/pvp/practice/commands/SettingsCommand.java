package land.pvp.practice.commands;

import land.pvp.core.commands.PlayerCommand;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.inventory.menu.impl.SettingsMenu;
import org.bukkit.entity.Player;

public class SettingsCommand extends PlayerCommand {
    private final PracticePlugin plugin;

    public SettingsCommand(PracticePlugin plugin) {
        super("settings");
        this.plugin = plugin;
        setAliases("options");
    }

    @Override
    public void execute(Player player, String[] args) {
        plugin.getMenuManager().getMenu(SettingsMenu.class).open(player);
    }
}
