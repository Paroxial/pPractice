package land.pvp.practice.commands.events;

import java.util.Arrays;
import java.util.stream.Collectors;
import land.pvp.core.CorePlugin;
import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.player.CoreProfile;
import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.timer.Timer;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.events.Event;
import land.pvp.practice.events.EventType;
import land.pvp.practice.managers.EventManager;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import org.bukkit.entity.Player;

public class EventCommand extends PlayerCommand {
    private static final String EVENT_TYPES;

    static {
        String types = String.join(", ", Arrays.stream(EventType.values()).map(EventType::getName).collect(Collectors.toList()));
        EVENT_TYPES = CC.GREEN + "Valid event types: " + types;
    }

    private final PracticePlugin plugin;

    public EventCommand(PracticePlugin plugin) {
        super("event");
        this.plugin = plugin;

        setUsage(
                CC.SECONDARY + "/event host <type>" + CC.GRAY + " - " + CC.PRIMARY + "host an event of the specified type",
//                CC.SECONDARY + "/event status <type>" + CC.GRAY + " - " + CC.PRIMARY + "see the event status of the specified type",
                CC.SECONDARY + "/event spectate <type>" + CC.GRAY + " - " + CC.PRIMARY + "join an event of the specified type",
                CC.SECONDARY + "/event join <type>" + CC.GRAY + " - " + CC.PRIMARY + "join an event of the specified type",
                CC.SECONDARY + "/event leave" + CC.GRAY + " - " + CC.PRIMARY + "leave your current event",
                EVENT_TYPES
        );
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(usageMessage);
            return;
        }

        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
        String arg = args[0].toLowerCase();

        switch (arg) {
            case "host": {
                CoreProfile coreProfile = CorePlugin.getInstance().getProfileManager().getProfile(player.getUniqueId());

                if (coreProfile != null && !coreProfile.hasDonor()) {
                    player.sendMessage(CC.RED + "Only donors can host events! Buy a rank at http://pvp-land.buycraft.net");
                    return;
                }

                if (profile.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "You must be in spawn to host an event!");
                    return;
                }

                if (args.length < 2) {
                    player.sendMessage(CC.RED + "You need to specify a valid event type!");
                    player.sendMessage(EVENT_TYPES);
                    return;
                }

                EventType type = EventType.getByName(args[1]);

                if (type == null) {
                    player.sendMessage(CC.RED + "That's an invalid event type!");
                    player.sendMessage(EVENT_TYPES);
                    return;
                }

                EventManager manager = plugin.getEventManager();

                if (manager.isEventActive(type)) {
                    player.sendMessage(CC.RED + "That event is already running!");
                    return;
                }

                Timer timer = profile.getEventHostTimer();

                if (timer.isActive()) {
                    player.sendMessage(CC.RED + "You can't host events for another " + timer.formattedExpiration() + ".");
                    return;
                }

                Event event = manager.getEventByType(type);

                event.host(player, profile);
                break;
            }
            case "spectate": {
                if (profile.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "You must be in spawn to spectate an event!");
                    return;
                }

                if (args.length < 2) {
                    player.sendMessage(CC.RED + "You need to specify a valid event type!");
                    player.sendMessage(EVENT_TYPES);
                    return;
                }

                EventType type = EventType.getByName(args[1]);

                if (type == null) {
                    player.sendMessage(CC.RED + "That's an invalid event type!");
                    player.sendMessage(EVENT_TYPES);
                    return;
                }

                EventManager manager = plugin.getEventManager();

                if (!manager.isEventActive(type)) {
                    player.sendMessage(CC.RED + "That event isn't running!");
                    return;
                }

                Event event = manager.getEventByType(type);

                event.spectate(player, profile);
                break;
            }
            case "join": {
                if (profile.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "You must be in spawn to join an event!");
                    return;
                }

                if (args.length < 2) {
                    player.sendMessage(CC.RED + "You need to specify a valid event type!");
                    player.sendMessage(EVENT_TYPES);
                    return;
                }

                EventType type = EventType.getByName(args[1]);

                if (type == null) {
                    player.sendMessage(CC.RED + "That's an invalid event type!");
                    player.sendMessage(EVENT_TYPES);
                    return;
                }

                EventManager manager = plugin.getEventManager();

                if (!manager.isEventActive(type)) {
                    player.sendMessage(CC.RED + "That event isn't running!");
                    return;
                }

                Event event = manager.getEventByType(type);

                event.join(player, profile);
                break;
            }
            case "leave": {
                Event event = profile.getActiveEvent();

                if (event == null) {
                    player.sendMessage(CC.RED + "You aren't in an event!");
                    return;
                }

                event.leave(player, profile);
                break;
            }
            default:
                player.sendMessage(usageMessage);
                break;
        }
    }
}
