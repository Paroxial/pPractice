package land.pvp.practice.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.arena.Arena;
import land.pvp.practice.arena.ArenaType;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.player.PracticeProfile;

public class MatchBuilder {
    private final PracticePlugin plugin;
    private final List<MatchTeam> teams = new ArrayList<>();
    private boolean party;
    private boolean ranked;
    private Kit kit;
    private Arena arena;

    public MatchBuilder(PracticePlugin plugin) {
        this.plugin = plugin;
        this.arena = plugin.getArenaManager().getRandomArena();
    }

    public MatchBuilder party(boolean party) {
        this.party = party;
        return this;
    }

    public MatchBuilder ranked(boolean ranked) {
        this.ranked = ranked;
        return this;
    }

    public MatchBuilder kit(Kit kit) {
        this.kit = kit;
        return this;
    }

    public MatchBuilder arena(Arena arena) {
        this.arena = arena;
        return this;
    }

    public MatchBuilder team(int teamId, List<PracticeProfile> members) {
        List<PracticeProfile> temp = new ArrayList<>(members);
        teams.add(new MatchTeam(teamId, members.get(0), temp));
        return this;
    }

    public MatchBuilder team(int teamId, PracticeProfile... members) {
        List<PracticeProfile> temp = new ArrayList<>(Arrays.asList(members));
        teams.add(new MatchTeam(teamId, members[0], temp));
        return this;
    }

    public Match build() {
        if (kit == Kit.SUMO && arena.getArenaType() != ArenaType.SUMO) {
            arena = plugin.getArenaManager().getRandomArena(ArenaType.SUMO);
        }

        Match match = new Match(teams, kit, arena, party, ranked);

        for (MatchTeam matchTeam : match.getTeams()) {
            matchTeam.setMatch(match);
        }

        return match;
    }
}
