package land.pvp.practice.managers;

import com.google.common.collect.ImmutableMap;
import land.pvp.core.storage.flatfile.Config;
import land.pvp.practice.PracticePlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Setter
public class LocationManager {
    private final Config config;
    @Getter
    private Location spawn, kitEditor, sumoSpawn, sumoFirstTeamSpawn, sumoSecondTeamSpawn;

    public LocationManager(PracticePlugin plugin) {
        this.config = new Config(plugin, "practice");

        Location spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation();

        config.addDefaults(ImmutableMap.<String, Object>builder()
                .put("locations.spawn", spawn)
                .put("locations.kit-editor", spawn)
                .put("locations.sumo-spawn", spawn)
                .put("locations.sumo-first-team-spawn", spawn)
                .put("locations.sumo-second-team-spawn", spawn)
                .build());
        config.copyDefaults();

        this.spawn = config.getLocation("locations.spawn");
        this.kitEditor = config.getLocation("locations.kit-editor");
        this.sumoSpawn = config.getLocation("locations.sumo-spawn");
        this.sumoFirstTeamSpawn = config.getLocation("locations.sumo-first-team-spawn");
        this.sumoSecondTeamSpawn = config.getLocation("locations.sumo-second-team-spawn");
    }

    public void saveLocations() {
        config.set("locations.spawn", spawn);
        config.set("locations.kit-editor", kitEditor);
        config.set("locations.sumo-spawn", sumoSpawn);
        config.set("locations.sumo-first-team-spawn", sumoFirstTeamSpawn);
        config.set("locations.sumo-second-team-spawn", sumoSecondTeamSpawn);
        config.save();
    }
}
