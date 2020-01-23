package land.pvp.practice.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    SUMO("Sumo");

    private final String name;

    public static EventType getByName(String name) {
        for (EventType eventType : values()) {
            if (eventType.name().equalsIgnoreCase(name)) {
                return eventType;
            }
        }

        return null;
    }
}
