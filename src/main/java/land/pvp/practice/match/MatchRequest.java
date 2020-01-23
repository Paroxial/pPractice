package land.pvp.practice.match;

import java.util.UUID;
import land.pvp.practice.arena.Arena;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MatchRequest {
    private final UUID requester;
    private final UUID requested;
    private final String kitName;
    private final Arena arena;
    private final boolean party;
}
