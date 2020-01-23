package land.pvp.practice.inventory.menu.impl;

import land.pvp.core.inventory.menu.Menu;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.player.PracticeProfile;

public class KitEditorMenu extends Menu {
    private final PracticePlugin plugin;

    public KitEditorMenu(PracticePlugin plugin) {
        super(2, "Select a Kit to Edit");
        this.plugin = plugin;
    }

    @Override
    public void setup() {
    }

    @Override
    public void update() {
        int count = 0;

        for (Kit kit : Kit.values()) {
            setActionableItem(count++, kit.getIcon(), player -> {
                PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
                plugin.getEditorManager().startEditing(player, profile, kit);
            });
        }
    }
}
