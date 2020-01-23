package land.pvp.practice.events.impl;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import land.pvp.core.utils.message.CC;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.events.Event;
import land.pvp.practice.events.EventStage;
import land.pvp.practice.events.EventType;
import land.pvp.practice.events.ParticipantState;
import land.pvp.practice.managers.LocationManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SumoEvent extends Event implements Listener {
    private static final Random random = ThreadLocalRandom.current();

    public SumoEvent(PracticePlugin plugin) {
        super(plugin, EventType.SUMO, plugin.getLocationManager().getSumoSpawn());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private UUID getRandomPlayerId() {
        List<UUID> remaining = remainingPlayerIds();
        return remaining.get(random.nextInt(remaining.size()));
    }

    private UUID getRandomPlayerIdExcludingId(UUID excludedId) {
        List<UUID> remaining = remainingPlayerIds();
        UUID found = remaining.get(random.nextInt(remaining.size()));
        return found.equals(excludedId) ? getRandomPlayerIdExcludingId(excludedId) : found;
    }

    private void matchup() {
        UUID first = getRandomPlayerId();
        UUID second = getRandomPlayerIdExcludingId(first);

        setState(first, ParticipantState.FIGHTING);
        setState(second, ParticipantState.FIGHTING);

        Player firstPlayer = plugin.getServer().getPlayer(first);
        Player secondPlayer = plugin.getServer().getPlayer(second);

        firstPlayer.getInventory().clear();
        firstPlayer.setAllowFlight(false);
        secondPlayer.getInventory().clear();
        secondPlayer.setAllowFlight(false);

        LocationManager locationManager = plugin.getLocationManager();

        firstPlayer.teleport(locationManager.getSumoFirstTeamSpawn());
        secondPlayer.teleport(locationManager.getSumoSecondTeamSpawn());

        broadcast(CC.PRIMARY + "Starting a new match between " + CC.SECONDARY + firstPlayer.getName()
                + CC.PRIMARY + " and " + CC.SECONDARY + secondPlayer.getName() + CC.PRIMARY + "!");
        setStage(EventStage.INTERMISSION);
    }

    @Override
    public void onStart() {
        matchup();
    }

    @Override
    public void onEnd() {
        UUID winnerId = remainingPlayerIds().get(0);
        Player winnerPlayer = plugin.getServer().getPlayer(winnerId);

        broadcast(winnerPlayer.getDisplayName() + CC.GREEN + " won the event!");
    }

    @Override
    public void onTick() {
        switch (currentStage) {
            case INTERMISSION:
                int remaining = 5 - duration;

                if (remaining != 0) {
                    String time = remaining + CC.PRIMARY + (remaining == 1 ? " second!" : " seconds!");
                    broadcast(CC.PRIMARY + "The match will start in " + CC.SECONDARY + time);
                } else {
                    broadcast(CC.GREEN + "The match has started!");
                    setStage(EventStage.FIGHTING);
                }
                break;
        }
    }

    @Override
    public void onDeath(Player victim) {
        for (UUID id : remainingFighterIds()) {
            Player player = plugin.getServer().getPlayer(id);
            resetToLobby(player);
        }

        setState(victim.getUniqueId(), ParticipantState.DEAD);

        for (UUID id : remainingFighterIds()) {
            setState(id, ParticipantState.WAITING);
        }

        int remaining = remainingPlayerIds().size();

        if (remaining != 1) {
            broadcast(victim.getDisplayName() + CC.RED + " was defeated! Only " + remaining + "/" + MAX_PLAYERS + " remain.");
            matchup();
        } else {
            end();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!isEventApplicable(player) || getParticipantState(player.getUniqueId()) != ParticipantState.FIGHTING) {
            return;
        }

        Location to = event.getTo();
        Location from = event.getFrom();

        if (currentStage == EventStage.INTERMISSION
                && (to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ())) {
            from.setX(from.getBlockX() + 0.5);
            from.setZ(from.getBlockZ() + 0.5);
            event.setTo(from);
        } else if (currentStage == EventStage.FIGHTING && to.getBlockY() != from.getBlockY()
                && to.getBlockY() < plugin.getLocationManager().getSumoFirstTeamSpawn().getY() - 4) {
            onDeath(player);
        }
    }

    @EventHandler
    public void onHit(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!isEventApplicable(player) || currentStage != EventStage.FIGHTING
                || getParticipantState(player.getUniqueId()) != ParticipantState.FIGHTING) {
            return;
        }

        event.setDamage(0);
    }
}
