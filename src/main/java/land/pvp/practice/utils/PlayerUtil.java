package land.pvp.practice.utils;

import land.pvp.core.CorePlugin;
import land.pvp.core.player.CoreProfile;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

@UtilityClass
public class PlayerUtil {
    public static void showAllPlayersFor(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            player.showPlayer(online);
        }
    }

    public static void hideAllPlayersFor(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(online);
        }
    }

    public static void toggleFlyFor(Player player) {
        CoreProfile coreProfile = CorePlugin.getInstance().getProfileManager().getProfile(player.getUniqueId());

        if (coreProfile != null && coreProfile.hasDonor()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            player.setAllowFlight(false);
        }
    }

    public static void resetPlayerForMatch(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.setMaximumNoDamageTicks(20);
        player.setFallDistance(0.0F);
        player.setFoodLevel(20);
        player.setSaturation(5.0F);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFallDistance(-1.0F);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
    }
}
