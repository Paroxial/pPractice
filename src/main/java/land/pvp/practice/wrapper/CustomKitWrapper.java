package land.pvp.practice.wrapper;

import java.util.HashMap;
import java.util.Map;
import land.pvp.practice.kit.PlayerKit;
import lombok.Getter;

@Getter
public class CustomKitWrapper {
    private final Map<Integer, PlayerKit> kits = new HashMap<>();

    public void setKit(int index, PlayerKit kit) {
        kits.put(index, kit);
    }

    public boolean hasKit(int index) {
        return kits.get(index) != null;
    }

    public PlayerKit getKit(int index) {
        return kits.get(index);
    }

    public void removeKit(int index) {
        kits.remove(index);
    }
}
