package land.pvp.practice.commands.time;

import land.pvp.practice.PracticePlugin;
import land.pvp.practice.player.PlayerTimeType;

public class NightCommand extends TimeCommand {
    public NightCommand(PracticePlugin plugin) {
        super(PlayerTimeType.NIGHT, plugin);
    }
}
