package land.pvp.practice.managers;

import java.util.EnumMap;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.leaderboard.Leaderboard;

public class LeaderboardManager {
    private final EnumMap<Kit, Leaderboard> leaderboards = new EnumMap<>(Kit.class);

    public LeaderboardManager(PracticePlugin plugin) {
        for (Kit kit : Kit.values()) {
            if (kit.isRanked()) {
                leaderboards.put(kit, new Leaderboard(plugin, kit.getName()));
            }
        }
    }

    public void updateALl() {
        for (Leaderboard leaderboard : leaderboards.values()) {
            leaderboard.update();
        }
    }

    public Leaderboard getLeaderboardByKit(Kit kit) {
        return leaderboards.get(kit);
    }
}
