package land.pvp.practice.wrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EloWrapper {
    public static final int MININMUM_ELO = 500;
    private int soloRating;
    private int partyRating;

    public void updateRating(int newRating, boolean party) {
        if (party) {
            this.partyRating = newRating;
        } else {
            this.soloRating = newRating;
        }
    }
}
