package land.pvp.practice.commands.management;

import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.player.rank.Rank;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.arena.Arena;
import land.pvp.practice.arena.ArenaType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ArenaCommand extends PlayerCommand {
    private final PracticePlugin plugin;

    public ArenaCommand(PracticePlugin plugin) {
        super("arena", Rank.ADMIN);
        this.plugin = plugin;
        setUsage(CC.RED + "Usage: /arena <subcommand> <args>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(usageMessage);
            return;
        }

        String name = args[1];
        Arena arena = plugin.getArenaManager().getArena(name);
        String subCommand = args[0].toLowerCase();
        Location location = player.getLocation();

        location.setX(location.getBlockX() + 0.5);
        location.setY(location.getBlockY() + 3.0);
        location.setZ(location.getBlockZ() + 0.5);

        switch (subCommand) {
            case "create":
                if (arena == null) {
                    plugin.getArenaManager().createArena(name);
                    player.sendMessage(CC.GREEN + "Successfully created arena " + name + ".");
                } else {
                    player.sendMessage(CC.RED + "That arena already exists.");
                }
                break;
            case "setsumo":
                if (arena == null) {
                    player.sendMessage(CC.RED + "That arena doesn't exist.");
                } else {
                    arena.setArenaType(ArenaType.SUMO);
                    player.sendMessage(CC.GREEN + "Successfully set arena " + name + " to Sumo.");
                }
                break;
            case "setyval":
                if (arena == null) {
                    player.sendMessage(CC.RED + "That arena doesn't exist.");
                } else {
                    if (arena.getArenaType() != ArenaType.SUMO) {
                        player.sendMessage(CC.RED + "This is only for Sumo arenas.");
                    } else {
                        arena.setYVal(player.getLocation().getBlockY());
                        player.sendMessage(CC.GREEN + "Set y-value for the arena!");
                    }
                }
                break;
            case "remove":
                if (arena == null) {
                    player.sendMessage(CC.RED + "That arena doesn't exist.");
                } else {
                    plugin.getArenaManager().removeArena(arena);
                    player.sendMessage(CC.GREEN + "Successfully removed arena " + name + ".");
                }
                break;
            case "first":
                if (arena == null) {
                    player.sendMessage(CC.RED + "That arena doesn't exist.");
                } else {
                    arena.setFirstTeamSpawn(location);
                    player.sendMessage(CC.GREEN + "Set the first team spawn to your location.");
                }
                break;
            case "second":
                if (arena == null) {
                    player.sendMessage(CC.RED + "That arena doesn't exist.");
                } else {
                    arena.setSecondTeamSpawn(location);
                    player.sendMessage(CC.GREEN + "Set the second team spawn to your location.");
                }
                break;
            case "tp":
                if (arena == null) {
                    player.sendMessage(CC.RED + "That arena doesn't exist.");
                } else {
                    player.teleport(arena.getFirstTeamSpawn());
                    player.sendMessage(CC.GREEN + "Teleported to the arena.");
                }
                break;
            default:
                player.sendMessage(usageMessage);
                break;
        }
    }
}
