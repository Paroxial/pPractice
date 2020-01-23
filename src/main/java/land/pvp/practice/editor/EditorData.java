package land.pvp.practice.editor;

import land.pvp.practice.PracticePlugin;
import land.pvp.practice.inventory.menu.impl.KitLayoutMenu;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.kit.PlayerKit;
import land.pvp.practice.player.PracticeProfile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditorData {
    private final Kit kit;
    private PlayerKit customKit;
    private KitLayoutMenu menu;
    private boolean renaming;

    public EditorData(Kit kit, PlayerKit customKit) {
        this.kit = kit;
        this.customKit = customKit;
    }

    public void unregisterMenu(PracticePlugin plugin) {
        if (menu != null) {
            plugin.getMenuManager().unregisterMenu(menu);
        }
    }

    public KitLayoutMenu registerMenu(PracticeProfile profile, PracticePlugin plugin) {
        KitLayoutMenu menu = new KitLayoutMenu(profile);
        this.menu = menu;
        plugin.getMenuManager().registerMenus(menu);
        return menu;
    }
}
