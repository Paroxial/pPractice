package land.pvp.practice.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.player.PlayerUtil;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.constants.ItemHotbars;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.party.PartyState;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.utils.StringUtil;
import land.pvp.practice.wrapper.EloWrapper;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PlayerManager {
    private final Map<UUID, PracticeProfile> profiles = new HashMap<>();
    private final PracticePlugin plugin;

    public void createProfile(UUID id, String name) {
        PracticeProfile profile = new PracticeProfile(id, name, plugin);
        profiles.put(id, profile);
    }

    public PracticeProfile getProfile(UUID id) {
        return profiles.get(id);
    }

    public void removeProfile(UUID id) {
        profiles.remove(id);
    }

    public void displayStats(Player player) {
        displayStats(player, player);
    }

    public void displayStats(Player player, Player target) {
        PracticeProfile profile = getProfile(target.getUniqueId());

        player.sendMessage(CC.PRIMARY + "Stats for " + target.getName());
        player.sendMessage(CC.PRIMARY + "Wins: " + CC.SECONDARY + StringUtil.formatNumberWithCommas(profile.getWins()));
        player.sendMessage(CC.PRIMARY + "Global ELO: " + CC.SECONDARY + StringUtil.formatNumberWithCommas(profile.getGlobalElo()));

        for (Kit kit : Kit.values()) {
            EloWrapper eloWrapper = profile.getElo(kit);
            String formattedSolo = StringUtil.formatNumberWithCommas(eloWrapper.getSoloRating());
            String formattedParty = StringUtil.formatNumberWithCommas(eloWrapper.getPartyRating());

            player.sendMessage(CC.PRIMARY + kit.getName() + " ELO: "
                    + CC.SECONDARY + formattedSolo + CC.ACCENT + " (Solo) "
                    + CC.SECONDARY + formattedParty + CC.ACCENT + " (Party)");
        }
    }

    public void setupPlayer(Player player) {
        PlayerUtil.clearPlayer(player);
        ItemHotbars.SPAWN_ITEMS.apply(player);

        player.teleport(plugin.getLocationManager().getSpawn());

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            PracticeProfile onlineProfile = getProfile(onlinePlayer.getUniqueId());

            if (onlineProfile.isHidingPlayers()) {
                onlinePlayer.hidePlayer(player);
            }
        }

        land.pvp.practice.utils.PlayerUtil.toggleFlyFor(player);
    }

    void resetPlayerMinimally(Player player, PracticeProfile profile, boolean teleport) {
        profile.setPlayerState(PlayerState.SPAWN);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setScoreboard(plugin.getServer().getScoreboardManager().getNewScoreboard());
        plugin.getCustomScoreboard().forceUpdate(player);

        ItemHotbars.giveSpawnItems(player, profile.isInParty());

        if (teleport) {
            player.teleport(plugin.getLocationManager().getSpawn());
        }
    }

    public void resetPlayer(Player player, boolean teleport) {
        PracticeProfile profile = getProfile(player.getUniqueId());
        boolean inParty = profile.isInParty();

        if (inParty) {
            profile.getParty().setState(PartyState.SPAWN);
        }

        profile.setPlayerState(PlayerState.SPAWN);
        profile.setMatch(null);
        profile.setActiveEvent(null);

        PlayerUtil.clearPlayer(player);
        ItemHotbars.giveSpawnItems(player, inParty);

        if (teleport) {
            player.teleport(plugin.getLocationManager().getSpawn());
        }

        if (profile.isHidingPlayers()) {
            land.pvp.practice.utils.PlayerUtil.hideAllPlayersFor(player);
        } else {
            land.pvp.practice.utils.PlayerUtil.showAllPlayersFor(player);
        }

        land.pvp.practice.utils.PlayerUtil.toggleFlyFor(player);
    }

    public Collection<PracticeProfile> getProfiles() {
        return profiles.values();
    }

    public void saveProfiles() {
        for (PracticeProfile profile : getProfiles()) {
            profile.save(false, plugin);
        }
    }
}
