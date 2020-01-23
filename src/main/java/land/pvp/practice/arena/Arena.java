package land.pvp.practice.arena;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
@RequiredArgsConstructor
public class Arena {
    private final String name;
    private ArenaType arenaType = ArenaType.STANDARD;
    private int yVal;
    private boolean enabled;
    private Location firstTeamSpawn;
    private Location secondTeamSpawn;
}
