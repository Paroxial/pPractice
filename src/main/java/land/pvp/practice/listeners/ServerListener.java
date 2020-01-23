package land.pvp.practice.listeners;

import land.pvp.core.event.server.ServerShutdownCancelEvent;
import land.pvp.core.event.server.ServerShutdownScheduleEvent;
import land.pvp.practice.PracticePlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class ServerListener implements Listener {
    private final PracticePlugin plugin;

    @EventHandler
    public void onShutdownSchedule(ServerShutdownScheduleEvent event) {
        plugin.getQueueManager().setRankedEnabled(false);
    }

    @EventHandler
    public void onShutdownSchedule(ServerShutdownCancelEvent event) {
        plugin.getQueueManager().setRankedEnabled(true);
    }
}
