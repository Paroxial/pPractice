package land.pvp.practice.inventory.menu;

import land.pvp.core.inventory.menu.Menu;
import land.pvp.core.inventory.menu.action.Action;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.kit.Kit;

public abstract class GenericKitMenu extends Menu {
    protected final PracticePlugin plugin;

    protected GenericKitMenu(String name, PracticePlugin plugin) {
        super(2, name);
        this.plugin = plugin;
    }

    @Override
    public void update() {
        int count = 0;

        for (Kit kit : Kit.values()) {
            setActionableItem(count++, kit.getIcon(), getAction(kit));
        }
    }

    protected abstract Action getAction(Kit kit);
}
