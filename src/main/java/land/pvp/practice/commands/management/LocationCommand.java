package land.pvp.practice.commands.management;

import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.player.rank.Rank;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.managers.LocationManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LocationCommand extends PlayerCommand {
    private final PracticePlugin plugin;

    public LocationCommand(PracticePlugin plugin) {
        super("location", Rank.ADMIN);
        this.plugin = plugin;
        setUsage(CC.RED + "Usage: /location <spawn|kiteditor|sumospawn>");
    }

    private static Location fixedLocationOf(Player player) {
        Location location = player.getLocation();

        location.setX(location.getBlockX() + 0.5);
        location.setY(location.getBlockY() + 3.0);
        location.setZ(location.getBlockZ() + 0.5);

        return location;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(usageMessage);
            return;
        }

        LocationManager locationManager = plugin.getLocationManager();

        switch (args[0].toLowerCase()) {
            case "spawn":
                locationManager.setSpawn(fixedLocationOf(player));
                player.sendMessage(CC.GREEN + "Set the spawn!");
                break;
            case "kiteditor":
                locationManager.setKitEditor(fixedLocationOf(player));
                player.sendMessage(CC.GREEN + "Set the kit editor spawn!");
                break;
            case "sumospawn":
                locationManager.setSumoSpawn(fixedLocationOf(player));
                player.sendMessage(CC.GREEN + "Set the sumo event spawn!");
                break;
            case "sumofirstteamspawn":
                locationManager.setSumoFirstTeamSpawn(fixedLocationOf(player));
                player.sendMessage(CC.GREEN + "Set the sumo first team spawn!");
                break;
            case "sumosecondteamspawn":
                locationManager.setSumoSecondTeamSpawn(fixedLocationOf(player));
                player.sendMessage(CC.GREEN + "Set the sumo second team spawn!");
                break;
            default:
                player.sendMessage(usageMessage);
                break;
        }
    }
}
