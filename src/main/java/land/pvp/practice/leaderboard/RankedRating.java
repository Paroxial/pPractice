package land.pvp.practice.leaderboard;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RankedRating {
    private final String name;
    private final String displayName;
    private final UUID id;
    private final int rating;
}
