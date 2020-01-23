package land.pvp.practice.inventory;

import java.util.Arrays;
import land.pvp.core.utils.item.ItemBuilder;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.match.MatchData;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.utils.MathUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class InventorySnapshot {
    private final Inventory inventory;

    public InventorySnapshot(Player player, PracticePlugin plugin, boolean dead, Kit kit) {
        this.inventory = plugin.getServer().createInventory(null, 54, player.getName() + "'s Inventory");

        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i + 27, contents[i]);
            inventory.setItem(i + 18, contents[i + 27]);
            inventory.setItem(i + 9, contents[i + 18]);
            inventory.setItem(i, contents[i + 9]);
        }

        for (int i = 36; i < 40; i++) {
            inventory.setItem(i, armor[39 - i]);
        }

        double health = dead ? 0.0 : MathUtil.roundToHalves(player.getHealth());
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
        MatchData matchData = profile.getMatchData();

        int longestCombo = matchData.getLongestCombo();

        ItemStack healthInfo = new ItemBuilder(Material.SPECKLED_MELON)
                .name(CC.ACCENT + "Player Info")
                .lore(CC.PRIMARY + "Hearts: " + CC.SECONDARY + health)
                .build();
        ItemStack healingInfo;

        if (kit == Kit.SOUP) {
            int remainingSoups = (int) Arrays.stream(contents).filter(i -> i != null && i.getType() == Material.MUSHROOM_SOUP).count();

            healingInfo = new ItemBuilder(Material.MUSHROOM_SOUP)
                    .name(CC.ACCENT + "Soup Info")
                    .lore(CC.PRIMARY + "Remaining: " + CC.SECONDARY + remainingSoups)
                    .build();
        } else {
            int missedPots = matchData.getMissedPots();
            int remainingPots = (int) Arrays.stream(contents).filter(i -> i != null && i.getDurability() == 16421).count();

            healingInfo = new ItemBuilder(Material.POTION)
                    .durability(16421)
                    .name(CC.ACCENT + "Potion Info")
                    .lore(
                            CC.PRIMARY + "Missed: " + CC.SECONDARY + missedPots,
                            CC.PRIMARY + "Remaining: " + CC.SECONDARY + remainingPots
                    )
                    .flags(ItemFlag.HIDE_POTION_EFFECTS)
                    .build();
        }

        ItemStack comboInfo = new ItemBuilder(Material.DIAMOND_SWORD)
                .name(CC.ACCENT + "Match Info")
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .lore(
                        CC.PRIMARY + "Longest combo: " + CC.SECONDARY + longestCombo,
                        CC.PRIMARY + "Hits: " + CC.SECONDARY + matchData.getHits()
                )
                .build();

        inventory.setItem(47, healthInfo);
        inventory.setItem(49, healingInfo);
        inventory.setItem(51, comboInfo);
    }

    public void openTo(Player player) {
        player.openInventory(inventory);
    }
}
