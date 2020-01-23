package land.pvp.practice.managers;

import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.player.PlayerUtil;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.editor.EditorData;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.kit.PlayerKit;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class EditorManager {
    private final PracticePlugin plugin;

    public void startEditing(Player player, PracticeProfile profile, Kit kit) {
        profile.setPlayerState(PlayerState.EDITING);
        profile.setEditorData(new EditorData(kit, new PlayerKit(kit.getName())));

        PlayerUtil.clearPlayer(player);

        kit.apply(player, false);

        player.teleport(plugin.getLocationManager().getKitEditor());
        player.sendMessage(CC.PRIMARY + "Now editing kit " + CC.SECONDARY + kit.getName() + CC.PRIMARY + ".");
    }

    public void stopEditing(Player player, PracticeProfile profile) {
        profile.getEditorData().unregisterMenu(plugin);
        profile.setEditorData(null);

        PlayerUtil.clearPlayer(player);
        land.pvp.practice.utils.PlayerUtil.toggleFlyFor(player);

        plugin.getPlayerManager().resetPlayerMinimally(player, profile, true);

        player.sendMessage(CC.GREEN + "Finished editing kit.");
    }
}
