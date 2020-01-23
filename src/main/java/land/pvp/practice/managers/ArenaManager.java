package land.pvp.practice.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import land.pvp.core.storage.flatfile.Config;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.arena.Arena;
import land.pvp.practice.arena.ArenaType;

public class ArenaManager {
    private final List<Arena> arenas = new ArrayList<>();
    private final Config config;

    public ArenaManager(PracticePlugin plugin) {
        this.config = new Config(plugin, "arenas");

        for (String arenaName : config.getKeys()) {
            Arena arena = new Arena(arenaName);

            String type = config.getString(arenaName + ".type");
            try {
                arena.setYVal(config.getInt(arenaName + ".yval"));
            } catch (Exception e) {
                arena.setYVal(0);
            }
            arena.setArenaType(type == null ? ArenaType.STANDARD : ArenaType.valueOf(type));
            arena.setFirstTeamSpawn(config.getLocation(arenaName + ".first-team-spawn"));
            arena.setSecondTeamSpawn(config.getLocation(arenaName + ".second-team-spawn"));

            arenas.add(arena);
        }
    }

    public List<Arena> getArenas() {
        List<Arena> typeArenas = getArenas(ArenaType.STANDARD);
        return typeArenas;
    }

    public List<Arena> getArenas(ArenaType type) {
        return arenas.stream().filter(arena -> arena.getArenaType() == type).collect(Collectors.toList());
    }

    public Arena getRandomArena(ArenaType type) {
        List<Arena> typeArenas = getArenas(type);
        return typeArenas.get(ThreadLocalRandom.current().nextInt(typeArenas.size()));
    }

    public Arena getRandomArena() {
        List<Arena> typeArenas = getArenas(ArenaType.STANDARD);
        return typeArenas.get(ThreadLocalRandom.current().nextInt(typeArenas.size()));
    }

    public void createArena(String arenaName) {
        arenas.add(new Arena(arenaName));
    }

    public Arena getArena(String arenaName) {
        for (Arena arena : arenas) {
            if (arena.getName().equals(arenaName)) {
                return arena;
            }
        }

        return null;
    }

    public void removeArena(Arena arena) {
        arenas.remove(arena);
    }

    public void saveArenas() {
        for (Arena arena : arenas) {
            String name = arena.getName();

            config.set(name + ".yval", arena.getYVal());
            config.set(name + ".type", arena.getArenaType().name());
            config.set(name + ".first-team-spawn", arena.getFirstTeamSpawn());
            config.set(name + ".second-team-spawn", arena.getSecondTeamSpawn());
        }

        config.save();
    }
}
