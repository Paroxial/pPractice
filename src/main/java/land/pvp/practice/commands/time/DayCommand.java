package land.pvp.practice.commands.time;

import land.pvp.practice.PracticePlugin;
import land.pvp.practice.player.PlayerTimeType;

public class DayCommand extends TimeCommand {
    public DayCommand(PracticePlugin plugin) {
        super(PlayerTimeType.DAY, plugin);
    }
}
