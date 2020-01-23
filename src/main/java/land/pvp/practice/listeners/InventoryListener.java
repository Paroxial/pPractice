package land.pvp.practice.listeners;

import land.pvp.core.inventory.menu.Menu;
import land.pvp.core.inventory.menu.action.Action;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

@RequiredArgsConstructor
public class InventoryListener implements Listener {
    private final PracticePlugin plugin;

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (player.getGameMode() != GameMode.SURVIVAL || event.getClickedInventory() == null
                || event.getClickedInventory() == player.getInventory() || event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Menu menu = plugin.getMenuManager().getMatchingMenu(event.getClickedInventory());

        if (menu != null) {
            Action action = menu.getAction(event.getSlot());

            if (action != null) {
                event.setCancelled(true);
                action.onClick(player);
            }
        }
    }

    @EventHandler
    public void onItemMove(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (player.getGameMode() == GameMode.CREATIVE && player.isOp()) {
            return;
        }

        if (event.getClickedInventory() != null && event.getClickedInventory().getName().contains("'s Inventory")) {
            event.setCancelled(true);
            return;
        }

        if (profile.getPlayerState() != PlayerState.FIGHTING && profile.getPlayerState() != PlayerState.EDITING) {
            event.setCancelled(true);
        }
    }
}
