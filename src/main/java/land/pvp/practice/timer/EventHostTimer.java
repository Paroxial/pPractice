package land.pvp.practice.timer;

import java.util.concurrent.TimeUnit;
import land.pvp.core.utils.timer.impl.IntegerTimer;

public class EventHostTimer extends IntegerTimer {
    public EventHostTimer() {
        super(TimeUnit.MINUTES, 5);
    }
}
