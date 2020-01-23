package land.pvp.practice.managers;

import java.util.HashMap;
import java.util.Map;
import land.pvp.core.inventory.menu.Menu;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.inventory.menu.impl.ArenaMenu;
import land.pvp.practice.inventory.menu.impl.DuelMenu;
import land.pvp.practice.inventory.menu.impl.KitEditorMenu;
import land.pvp.practice.inventory.menu.impl.PartyEventMenu;
import land.pvp.practice.inventory.menu.impl.PartyFfaMenu;
import land.pvp.practice.inventory.menu.impl.PartyListMenu;
import land.pvp.practice.inventory.menu.impl.PartySplitMenu;
import land.pvp.practice.inventory.menu.impl.RankedMenu;
import land.pvp.practice.inventory.menu.impl.SettingsMenu;
import land.pvp.practice.inventory.menu.impl.SumoArenaMenu;
import land.pvp.practice.inventory.menu.impl.UnrankedMenu;
import org.bukkit.inventory.Inventory;

public class MenuManager {
    private final Map<Class<? extends Menu>, Menu> menus = new HashMap<>();

    public MenuManager(PracticePlugin plugin) {
        registerMenus(
                new RankedMenu(plugin),
                new UnrankedMenu(plugin),
                new DuelMenu(plugin),
                new ArenaMenu(plugin),
                new SumoArenaMenu(plugin),
                new KitEditorMenu(plugin),
                new PartyListMenu(plugin),
                new PartyEventMenu(plugin),
                new PartySplitMenu(plugin),
                new PartyFfaMenu(plugin),
                new SettingsMenu(plugin)
                //  new RedroverMenu()
        );

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            getMenu(RankedMenu.class).update();
            getMenu(UnrankedMenu.class).update();
            getMenu(PartyListMenu.class).update();
        }, 20L, 40L);
    }

    public Menu getMenu(Class<? extends Menu> clazz) {
        return menus.get(clazz);
    }

    public Menu getMatchingMenu(Inventory other) {
        for (Menu menu : menus.values()) {
            if (menu.getInventory().equals(other)) {
                return menu;
            }
        }

        return null;
    }

    public void registerMenus(Menu... menus) {
        for (Menu menu : menus) {
            menu.setup();
            menu.update();
            this.menus.put(menu.getClass(), menu);
        }
    }

    public void unregisterMenu(Menu menu) {
        menus.remove(menu.getClass());
    }

    public void updateAllMenus() {
        for (Menu menu : menus.values()) {
            menu.update();
        }
    }
}
