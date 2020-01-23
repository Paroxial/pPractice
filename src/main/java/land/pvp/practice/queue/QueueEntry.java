package land.pvp.practice.queue;

import land.pvp.practice.kit.Kit;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.tasks.QueueSearchTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class QueueEntry {
    @Getter
    private final PracticeProfile profile;
    @Getter
    private final Kit kit;
    @Getter
    private final boolean ranked;
    @Getter
    private final boolean party;
    @Getter
    private final int elo;
    @Setter
    private QueueSearchTask searchTask;

    public void cancelSearchTask() {
        if (searchTask != null) {
            searchTask.cancel();
        }
    }

    public int[] getCurrentRange() {
        return searchTask == null ? new int[]{elo - 50, elo + 50} : searchTask.getRange();
    }
}
