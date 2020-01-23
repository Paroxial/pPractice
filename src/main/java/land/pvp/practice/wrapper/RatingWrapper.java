package land.pvp.practice.wrapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RatingWrapper {
    private static final int K_FACTOR = 32;
    private final int newWinnerRating;
    private final int newLoserRating;
    private final int difference;

    private static double calculateExpectedScore(int rating, int opponentRating) {
        return 1.0 / (1.0 + Math.pow(10.0, (opponentRating - rating) / 400.0));
    }

    private static int calculateNewRating(int rating, double expectedScore) {
        return rating + (int) Math.round(K_FACTOR * (1.0 - expectedScore));
    }

    public static RatingWrapper getRatingsFrom(int winnerElo, int loserElo) {
        double expectedScore = calculateExpectedScore(winnerElo, loserElo);

        int newWinnerRating = calculateNewRating(winnerElo, expectedScore);
        int difference = newWinnerRating - winnerElo;
        int newLoserRating = loserElo - difference;

        return new RatingWrapper(newWinnerRating, newLoserRating, difference);
    }
}
