package land.pvp.practice.kit;

import org.bukkit.entity.Player;

public interface IKit {
    default void applyAttributes(Player player) {
    }

    void apply(Player player, boolean sendMessage);
}
