package land.pvp.practice.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.inventory.InventorySnapshot;
import land.pvp.practice.kit.Kit;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class SnapshotManager {
    private final PracticePlugin plugin;
    private final Cache<String, InventorySnapshot> snapshots = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1L, TimeUnit.MINUTES)
            .build();

    public void cacheSnapshot(Player player, boolean dead, Kit kit) {
        snapshots.put(player.getName(), new InventorySnapshot(player, plugin, dead, kit));
    }

    public InventorySnapshot getSnapshot(String name) {
        return snapshots.getIfPresent(name);
    }
}
