package land.pvp.practice.kit;

import land.pvp.core.utils.message.CC;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerKit {
    private final String name;
    private ItemStack[] armor;
    private ItemStack[] contents;
    private String customName;

    public void apply(Player player) {
        player.setHealth(player.getMaxHealth());
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armor);
        player.updateInventory();
        player.sendMessage(CC.PRIMARY + "Giving you " + CC.SECONDARY + customName);
    }
}
