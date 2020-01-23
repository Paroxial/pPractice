package land.pvp.practice.commands.management;

import java.util.Set;
import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.player.rank.Rank;
import land.pvp.core.storage.flatfile.Config;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.kit.Kit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class KitCommand extends PlayerCommand {
    private final PracticePlugin plugin;
    private final Config config;

    public KitCommand(PracticePlugin plugin) {
        super("kit", Rank.ADMIN);
        this.plugin = plugin;
        this.config = new Config(plugin, "kits");
        setUsage(CC.RED + "Usage: /kit <subcommand> <args>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(usageMessage);
            return;
        }

        String subCommand = args[0].toLowerCase();
        Kit kit = Kit.getByName(args[1]);

        if (kit == null) {
            player.sendMessage(CC.RED + "Kit doesn't exist!");
            return;
        }

        switch (subCommand) {
            case "setinv":
                if (player.getGameMode() == GameMode.CREATIVE) {
                    player.sendMessage(CC.RED + "You can't set kit contents in creative mode!");
                } else {
                    ItemStack[] armor = player.getInventory().getArmorContents().clone();
                    ItemStack[] contents = player.getInventory().getContents().clone();

                    config.set(kit.getName() + ".armor", armor);
                    config.set(kit.getName() + ".contents", contents);
                    config.save();

                    player.sendMessage(CC.GREEN + "Successfully set kit contents for " + args[1] + ".");
                }
                break;
            case "setedititems":
                Block block = player.getTargetBlock((Set<Material>) null, 100);

                if (block == null || block.getType() != Material.CHEST) {
                    player.sendMessage(CC.RED + "That's not a chest!");
                } else {
                    BlockState state = block.getState();
                    InventoryHolder holder = ((Chest) state).getInventory().getHolder();

                    if (holder instanceof DoubleChest) {
                        DoubleChest chest = (DoubleChest) holder;

                        config.set(kit.getName() + ".editor-contents", chest.getInventory().getContents());
                        config.save();

                        player.sendMessage(CC.GREEN + "Successfully set editor contents for " + args[1] + ".");
                    } else {
                        player.sendMessage(CC.RED + "That's not a double chest!");
                    }
                }
                break;
            case "getinv":
                player.getInventory().setContents(kit.getContents());
                player.getInventory().setArmorContents(kit.getArmor());
                player.updateInventory();
                player.sendMessage(CC.GREEN + "Successfully retrieved kit contents from " + args[1] + ".");
                break;
            case "icon":
                if (player.getItemInHand().getType() == Material.AIR) {
                    player.sendMessage(CC.RED + "You must be holding an item to set the kit icon!");
                } else {
                    config.set(kit.getName() + ".icon", player.getItemInHand());
                    config.save();

                    player.sendMessage(CC.GREEN + "Successfully set icon for kit " + args[1] + ".");
                }
                break;
            default:
                player.sendMessage(usageMessage);
                break;
        }

        plugin.getMenuManager().updateAllMenus();
    }
}
