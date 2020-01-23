package land.pvp.practice.leaderboard;

import java.util.Comparator;
import lombok.Getter;

public class RatingComparator implements Comparator<RankedRating> {
    @Getter
    private static final RatingComparator instance = new RatingComparator();

    @Override
    public int compare(RankedRating a, RankedRating b) {
        return -Integer.compare(a.getRating(), b.getRating());
    }
}
