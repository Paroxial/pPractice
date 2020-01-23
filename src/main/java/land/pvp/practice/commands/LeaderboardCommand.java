package land.pvp.practice.commands;

import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.utils.item.ItemBuilder;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.leaderboard.Leaderboard;
import land.pvp.practice.leaderboard.RankedRating;
import land.pvp.practice.utils.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class LeaderboardCommand extends PlayerCommand {
    private static final int PAGE_SIZE = 5;
    private final PracticePlugin plugin;

    public LeaderboardCommand(PracticePlugin plugin) {
        super("leaderboard");
        this.plugin = plugin;
        setAliases("leaderboards", "lb", "topelo", "elo", "topstats");
        setUsage(CC.RED + "Usage: /leaderboard <kit> [page]");
    }

    private static int validIntegerOf(int max, String arg) {
        try {
            int i = Integer.parseInt(arg);

            if (i < 1) {
                return 1;
            } else if (i > max) {
                return max;
            } else {
                return i;
            }
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(usageMessage);
            return;
        }

        Kit kit = Kit.getByName(args[0]);

        if (kit == null) {
            player.sendMessage(CC.RED + "That's not a kit!");
            return;
        }

        if (!kit.isRanked()) {
            player.sendMessage(CC.RED + "That kit isn't ranked!");
            return;
        }

        Leaderboard leaderboard = plugin.getLeaderboardManager().getLeaderboardByKit(kit);

        if (leaderboard == null || leaderboard.getRatings().isEmpty()) {
            player.sendMessage(CC.RED + "The leaderboards are empty... Please wait until they have been updated.");
            return;
        }

        int size = leaderboard.getRatings().size();
        int maxPages = size % PAGE_SIZE == 0 ? size / PAGE_SIZE : size / PAGE_SIZE + 1;
        int pageIndex = args.length < 2 ? 1 : validIntegerOf(maxPages, args[1]);

        Inventory inventory = plugin.getServer().createInventory(null, 54, kit.getName() + "  (" + pageIndex + " of " + maxPages + ")");

        int index = (pageIndex - 1) * PAGE_SIZE;
        int count = index + 1;

        for (RankedRating rating : leaderboard.getRatings(index)) {
            if (count > index + (PAGE_SIZE + 1)) {
                break;
            }

            inventory.setItem((9 * (count - 1)) + 4, new ItemBuilder(Material.SKULL_ITEM)
                    .name(CC.PRIMARY + rating.getName() + CC.GRAY + " - " + CC.SECONDARY + rating.getRating() + " ELO")
                    .skullOwner(rating.getName())
                    .build());
            player.sendMessage(CC.PRIMARY + StringUtil.formatNumberWithCommas(count) + ". " + CC.PRIMARY
                    + rating.getDisplayName() + CC.GRAY + ": " + CC.SECONDARY + StringUtil.formatNumberWithCommas(rating.getRating()) + " ELO");

            count++;
        }

        player.openInventory(inventory);
    }
}
