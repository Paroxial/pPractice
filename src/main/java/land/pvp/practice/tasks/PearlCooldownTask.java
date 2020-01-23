package land.pvp.practice.tasks;

import java.lang.ref.WeakReference;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.match.Match;
import land.pvp.practice.match.MatchState;
import land.pvp.practice.player.PracticeProfile;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PearlCooldownTask extends BukkitRunnable {
    private static final int PEARL_SECONDS = 16;
    private final WeakReference<Player> playerRef;
    private final PracticeProfile profile;
    private int countdown = PEARL_SECONDS - 1;
    private int ticksElapsed = 0;

    public PearlCooldownTask(Player player, PracticeProfile profile) {
        this.profile = profile;
        this.playerRef = new WeakReference<>(player);
    }

    @Override
    public void run() {
        Player player = playerRef.get();

        if (player == null || !player.isOnline()) {
            cancel();
            return;
        }

        Match match = profile.getMatch();

        if (match == null || match.getMatchState() == MatchState.ENDED) {
            player.setLevel(0);
            player.setExp(0.0F);
            cancel();
            return;
        }

        ticksElapsed += 2;

        if (ticksElapsed == PEARL_SECONDS * 20) {
            player.setExp(0.0F);
            player.setLevel(0);
            player.sendMessage(CC.GREEN + "Your pearl cooldown has expired!");
            cancel();
        } else if (ticksElapsed % 20 == 0) {
            player.setLevel(countdown--);
        } else {
            player.setExp(1F * (1F - ticksElapsed / (PEARL_SECONDS * 20F)));
        }
    }
}
