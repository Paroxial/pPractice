package land.pvp.practice.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import land.pvp.core.utils.item.ItemBuilder;
import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.message.ClickableMessage;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public abstract class Event implements Runnable {
    protected static final int MAX_PLAYERS = 150;
    private static final int START_DURATION = 60;
    private static final int MIN_PLAYER_COUNT = 10;
    protected final PracticePlugin plugin;
    private final Map<UUID, ParticipantState> playerStates = new HashMap<>();
    private final Set<UUID> spectators = new HashSet<>();
    private final EventType type;
    private final Location spawnLocation;
    protected int duration;
    @Getter
    protected EventStage currentStage = EventStage.WAITING_FOR_PLAYERS;
    private BukkitTask currentTask;
    private String announcer;

    @Override
    public final void run() {
        switch (currentStage) {
            case WAITING_FOR_PLAYERS:
                if (playerStates.size() == 0) {
                    reset();
                    return;
                }

                if (duration != 0 && duration % 15 == 0 && duration != START_DURATION) {
                    announce();
                }

                int remaining = START_DURATION - duration;

                if (duration == START_DURATION) {
                    broadcast(CC.GREEN + "The event has started!");
                    start();
                } else if (duration % 15 == 0 || remaining == 10 || remaining <= 5) {
                    String time = remaining + CC.PRIMARY + (remaining == 1 ? " second!" : " seconds!");
                    broadcast(CC.PRIMARY + "The event will start in " + CC.SECONDARY + time);
                }
                break;
        }

        onTick();

        duration++;
    }

    public final boolean isActive() {
        return currentTask != null;
    }

    public final ParticipantState getParticipantState(UUID id) {
        return playerStates.get(id);
    }

    public final List<UUID> remainingPlayerIds() {
        return playerStates.entrySet().stream()
                .filter(entry -> entry.getValue() == ParticipantState.WAITING)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public final List<UUID> remainingFighterIds() {
        return playerStates.entrySet().stream()
                .filter(entry -> entry.getValue() == ParticipantState.FIGHTING)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    protected final void setStage(EventStage stage) {
        currentStage = stage;
        duration = 0;
    }

    protected final void setState(UUID id, ParticipantState state) {
        playerStates.put(id, state);
    }

    public final void host(Player player, PracticeProfile profile) {
        if (plugin.getServer().getOnlinePlayers().size() < MIN_PLAYER_COUNT) {
            player.sendMessage(CC.RED + "There aren't enough players online to host an event!");
            return;
        }

        currentStage = EventStage.WAITING_FOR_PLAYERS;
        announcer = player.getName();
        announce();
        currentTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 0L, 20L);
        join(player, profile);
    }

    public final void join(Player player, PracticeProfile profile) {
        if (playerStates.size() == MAX_PLAYERS) {
            player.sendMessage(CC.RED + "The event is already full!");
            return;
        } else if (currentStage != EventStage.WAITING_FOR_PLAYERS) {
            player.sendMessage(CC.RED + "The event has already started!");
            return;
        }

        profile.setPlayerState(PlayerState.EVENT);
        profile.setActiveEvent(this);
        playerStates.put(player.getUniqueId(), ParticipantState.WAITING);
        resetToLobby(player);
        broadcast(player.getDisplayName() + CC.GREEN + " has joined the event "
                + CC.GRAY + "(" + playerStates.size() + "/" + MAX_PLAYERS + ")" + CC.GREEN + "!");
    }

    public final void spectate(Player player, PracticeProfile profile) {
        profile.setPlayerState(PlayerState.EVENT);
        profile.setActiveEvent(this);
        spectators.add(player.getUniqueId());
        resetToLobby(player);
        broadcast(player.getDisplayName() + CC.GREEN + " is now spectating the event!");
    }

    public final void leave(Player player, PracticeProfile profile) {
        if (getParticipantState(player.getUniqueId()) == ParticipantState.FIGHTING) {
            onDeath(player);
        }

        profile.setActiveEvent(null);
        plugin.getPlayerManager().resetPlayer(player, true);

        if (playerStates.containsKey(player.getUniqueId())) {
            broadcast(player.getDisplayName() + CC.RED + " has left the event!");
            playerStates.remove(player.getUniqueId());
        } else {
            broadcast(player.getDisplayName() + CC.RED + " is no longer spectating the event!");
            spectators.remove(player.getUniqueId());
        }
    }

    protected final void resetToLobby(Player player) {
        showAll(player);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.teleport(spawnLocation);
        player.getInventory().clear();
        player.getInventory().setItem(8, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Event").build());
    }

    protected final boolean isEventApplicable(Player player) {
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
        return profile.getActiveEvent() == this;
    }

    protected final void broadcast(String msg) {
        eventAction(player -> player.sendMessage(msg));
    }

    protected final void end() {
        onEnd();
        eventAction(player -> {
            plugin.getPlayerManager().resetPlayer(player, true);
            player.sendMessage(CC.RED + "The event has ended.");
        });
        reset();
    }


    private void start() {
        if (playerStates.size() < MIN_PLAYER_COUNT) {
            duration = -1;
            broadcast(CC.RED + "There weren't enough players to start the event! Restarting...");
            return;
        }

        onStart();
    }

    private void showAll(Player player) {
        eventAction(other -> {
            player.showPlayer(other);
            other.showPlayer(player);
        });
    }

    private void reset() {
        currentTask.cancel();
        playerStates.clear();
        spectators.clear();
        currentStage = EventStage.WAITING_FOR_PLAYERS;
        currentTask = null;
        duration = 0;
        announcer = null;
    }

    private void eventAction(Consumer<Player> action) {
        for (UUID id : playerStates.keySet()) {
            Player player = plugin.getServer().getPlayer(id);

            if (player != null) {
                action.accept(player);
            }
        }

        for (UUID id : spectators) {
            Player player = plugin.getServer().getPlayer(id);

            if (player != null) {
                action.accept(player);
            }
        }
    }

    private void announce() {
        plugin.getServer().broadcastMessage("");

        ClickableMessage message = new ClickableMessage(announcer)
                .color(CC.SECONDARY)
                .add(" is hosting a ")
                .color(CC.PRIMARY)
                .add(type.getName())
                .color(CC.SECONDARY)
                .add(" event!")
                .color(CC.PRIMARY)
                .add(" [Click to Join]")
                .color(CC.GREEN)
                .hover(CC.GREEN + "Click to Join")
                .command("/event join " + type.getName());

        message.broadcast();

        plugin.getServer().broadcastMessage("");
    }

    public abstract void onStart();

    public abstract void onEnd();

    public abstract void onTick();

    public abstract void onDeath(Player victim);
}
