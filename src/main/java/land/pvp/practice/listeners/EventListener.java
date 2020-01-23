package land.pvp.practice.listeners;

import land.pvp.practice.PracticePlugin;
import land.pvp.practice.events.Event;
import land.pvp.practice.events.EventStage;
import land.pvp.practice.events.ParticipantState;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

@RequiredArgsConstructor
public class EventListener implements Listener {
    private final PracticePlugin plugin;

    @EventHandler
    public void onHit(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getEntity();
        PracticeProfile damagerProfile = plugin.getPlayerManager().getProfile(damager.getUniqueId());

        if (damagerProfile.getPlayerState() != PlayerState.EVENT) {
            return;
        }

        Event activeEvent = damagerProfile.getActiveEvent();

        if (activeEvent.getCurrentStage() != EventStage.FIGHTING) {
            event.setCancelled(true);
        }

        if (activeEvent.getParticipantState(damagerProfile.getId()) != ParticipantState.FIGHTING) {
            event.setCancelled(true);
        }
    }
}
