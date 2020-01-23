package land.pvp.practice.constants;

import land.pvp.core.utils.item.ItemBuilder;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static land.pvp.core.utils.message.CC.AQUA;
import static land.pvp.core.utils.message.CC.GOLD;
import static land.pvp.core.utils.message.CC.GRAY;
import static land.pvp.core.utils.message.CC.GREEN;
import static land.pvp.core.utils.message.CC.PINK;
import static land.pvp.core.utils.message.CC.RED;
import static land.pvp.core.utils.message.CC.YELLOW;
import static org.bukkit.Material.BOOK;
import static org.bukkit.Material.DIAMOND_SWORD;
import static org.bukkit.Material.GOLD_SWORD;
import static org.bukkit.Material.INK_SACK;
import static org.bukkit.Material.IRON_SWORD;
import static org.bukkit.Material.NAME_TAG;
import static org.bukkit.Material.PAPER;
import static org.bukkit.Material.SKULL_ITEM;
import static org.bukkit.Material.WATCH;

@RequiredArgsConstructor
public enum ItemHotbars {
    SPAWN_ITEMS(
            new ItemStack[]{
                    new ItemBuilder(DIAMOND_SWORD).name(GREEN + "Join a Ranked Queue").build(),
                    new ItemBuilder(IRON_SWORD).name(GRAY + "Join an Unranked Queue").build(),
                    null,
                    null,
                    new ItemBuilder(NAME_TAG).name(PINK + "Create a Party").build(),
                    null,
                    null,
                    new ItemBuilder(WATCH).name(YELLOW + "Edit Your Settings").build(),
                    new ItemBuilder(BOOK).name(GOLD + "Edit Your Kits").build()
            }
    ),
    PARTY_ITEMS(
            new ItemStack[]{
                    new ItemBuilder(IRON_SWORD).name(GRAY + "Join a 2v2 Queue").build(),
                    new ItemBuilder(GOLD_SWORD).name(YELLOW + "Start a Party Event").build(),
                    null,
                    null,
                    new ItemBuilder(SKULL_ITEM).name(PINK + "Fight Other Parties").build(),
                    null,
                    new ItemBuilder(PAPER).name(AQUA + "View Party Information").build(),
                    null,
                    new ItemBuilder(INK_SACK).durability(1).name(RED + "Leave Your Party").build()
            }
    ),
    SPEC_ITEMS(
            new ItemStack[]{
                    new ItemBuilder(BOOK).name(GOLD + "View Player Inventory").build(),
                    new ItemBuilder(PAPER).name(AQUA + "View Match Info").build(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    new ItemBuilder(INK_SACK).durability(1).name(RED + "Leave Spectator Mode").build()
            }
    ),
    QUEUE_ITEMS(
            new ItemStack[]{
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    new ItemBuilder(INK_SACK).durability(1).name(RED + "Leave Queue").build()
            }
    );

    private final ItemStack[] hotbar;

    public static void giveSpawnItems(Player player, boolean party) {
        if (party) {
            ItemHotbars.PARTY_ITEMS.apply(player);
        } else {
            ItemHotbars.SPAWN_ITEMS.apply(player);
        }

        player.getInventory().setHeldItemSlot(2);
    }

    public void apply(Player player) {
        for (int i = 0; i < hotbar.length; i++) {
            ItemStack item = hotbar[i];
            player.getInventory().setItem(i, item);
        }

        player.updateInventory();
    }
}
