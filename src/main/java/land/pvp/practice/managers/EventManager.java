package land.pvp.practice.managers;

import java.util.EnumMap;
import java.util.Map;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.events.Event;
import land.pvp.practice.events.EventType;
import land.pvp.practice.events.impl.SumoEvent;

public class EventManager {
    private final Map<EventType, Event> availableEvents = new EnumMap<>(EventType.class);

    public EventManager(PracticePlugin plugin) {
        availableEvents.put(EventType.SUMO, new SumoEvent(plugin));
    }

    public boolean isEventActive(EventType type) {
        return getEventByType(type).isActive();
    }

    public Event getEventByType(EventType type) {
        return availableEvents.get(type);
    }
}
