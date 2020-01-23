package land.pvp.practice.constants;

import land.pvp.core.utils.item.ItemBuilder;
import land.pvp.core.utils.message.CC;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Material.BOOK;

@UtilityClass
public class Items {
    public static final ItemStack DEFAULT_KIT_BOOK = new ItemBuilder(BOOK).name(CC.SECONDARY + "Default Kit").build();
}
