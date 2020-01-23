package land.pvp.practice.commands.time;

import land.pvp.practice.PracticePlugin;
import land.pvp.practice.player.PlayerTimeType;

public class SunsetCommand extends TimeCommand {
    public SunsetCommand(PracticePlugin plugin) {
        super(PlayerTimeType.SUNSET, plugin);
    }
}
