package land.pvp.practice.managers;

import java.util.ArrayList;
import java.util.List;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.match.Match;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MatchManager {
    private final List<Match> ongoingMatches = new ArrayList<>();
    private final PracticePlugin plugin;

    public int playersFighting(String kitName, boolean rankedMatch) {
        int playersFighting = 0;

        for (Match match : ongoingMatches) {
            if (match.getKit().getName().equals(kitName) && match.isRanked() == rankedMatch) {
                playersFighting += match.playersFighting();
            }
        }

        return playersFighting;
    }

    public int playersFighting() {
        int playersFighting = 0;

        for (Match match : ongoingMatches) {
            playersFighting += match.playersFighting();
        }

        return playersFighting;
    }

    public void startMatch(Match match) {
        ongoingMatches.add(match);
        match.start(plugin);
    }

    public void removeMatch(Match match) {
        ongoingMatches.remove(match);
    }
}
