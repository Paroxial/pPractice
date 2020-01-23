package land.pvp.practice.utils.collect.set;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ExpiringHashSet<T> extends HashSet<T> {
    private final Map<T, Long> times = new HashMap<>();
    private final int expiryMs;

    public ExpiringHashSet(int expirySeconds) {
        this.expiryMs = expirySeconds * 1000;
    }

    @Override
    public boolean add(T t) {
        times.put(t, System.currentTimeMillis());
        return super.add(t);
    }

    @Override
    public boolean contains(Object o) {
        boolean contains = super.contains(o);
        if (contains && System.currentTimeMillis() - expiryMs >= times.get(o)) {
            remove(o);
            times.remove(o);
            return false;
        }
        return contains;
    }
}
