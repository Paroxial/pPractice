package land.pvp.practice;

import land.pvp.practice.commands.InventoryCommand;
import land.pvp.practice.commands.LeaderboardCommand;
import land.pvp.practice.commands.PartyCommand;
import land.pvp.practice.commands.SettingsCommand;
import land.pvp.practice.commands.StatsCommand;
import land.pvp.practice.commands.events.EventCommand;
import land.pvp.practice.commands.management.ArenaCommand;
import land.pvp.practice.commands.management.KitCommand;
import land.pvp.practice.commands.management.LocationCommand;
import land.pvp.practice.commands.match.AcceptCommand;
import land.pvp.practice.commands.match.DuelCommand;
import land.pvp.practice.commands.match.SpectateCommand;
import land.pvp.practice.commands.time.DayCommand;
import land.pvp.practice.commands.time.NightCommand;
import land.pvp.practice.commands.time.SunsetCommand;
import land.pvp.practice.commands.toggle.ToggleDuelRequestsCommand;
import land.pvp.practice.commands.toggle.TogglePlayerVisibilityCommand;
import land.pvp.practice.commands.toggle.ToggleScoreboardCommand;
import land.pvp.practice.listeners.EntityListener;
import land.pvp.practice.listeners.EventListener;
import land.pvp.practice.listeners.InventoryListener;
import land.pvp.practice.listeners.PlayerListener;
import land.pvp.practice.listeners.ServerListener;
import land.pvp.practice.listeners.WorldListener;
import land.pvp.practice.managers.ArenaManager;
import land.pvp.practice.managers.EditorManager;
import land.pvp.practice.managers.EventManager;
import land.pvp.practice.managers.LeaderboardManager;
import land.pvp.practice.managers.LocationManager;
import land.pvp.practice.managers.MatchManager;
import land.pvp.practice.managers.MenuManager;
import land.pvp.practice.managers.PartyManager;
import land.pvp.practice.managers.PlayerManager;
import land.pvp.practice.managers.QueueManager;
import land.pvp.practice.managers.SnapshotManager;
import land.pvp.practice.managers.SpectatorManager;
import land.pvp.scoreboardapi.api.CustomScoreboard;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class PracticePlugin extends JavaPlugin {

    @Getter
    private static PracticePlugin instance;

    private LocationManager locationManager;
    private ArenaManager arenaManager;
    private EditorManager editorManager;
    private MatchManager matchManager;
    private PartyManager partyManager;
    private PlayerManager playerManager;
    private QueueManager queueManager;
    private SnapshotManager snapshotManager;
    private SpectatorManager spectatorManager;
    private MenuManager menuManager;
    private LeaderboardManager leaderboardManager;
    private EventManager eventManager;

    private CustomScoreboard customScoreboard;

    @Override
    public void onEnable() {
        instance = this;

        locationManager = new LocationManager(this);
        arenaManager = new ArenaManager(this);
        editorManager = new EditorManager(this);
        matchManager = new MatchManager(this);
        partyManager = new PartyManager(this);
        playerManager = new PlayerManager(this);
        queueManager = new QueueManager(this);
        snapshotManager = new SnapshotManager(this);
        spectatorManager = new SpectatorManager(this);
        menuManager = new MenuManager(this);
        leaderboardManager = new LeaderboardManager(this);
        eventManager = new EventManager(this);

        registerCommands(
                new DayCommand(this),
                new NightCommand(this),
                new SunsetCommand(this),
                new StatsCommand(this),
                new KitCommand(this),
                new ArenaCommand(this),
                new LocationCommand(this),
                new PartyCommand(this),
                new InventoryCommand(snapshotManager),
                new DuelCommand(this),
                new AcceptCommand(this),
                new SpectateCommand(this),
                new ToggleDuelRequestsCommand(this),
                new ToggleScoreboardCommand(this),
                new TogglePlayerVisibilityCommand(this),
                new LeaderboardCommand(this),
                new SettingsCommand(this),
                new EventCommand(this)
        );

        registerListeners(
                new EntityListener(this),
                new InventoryListener(this),
                new PlayerListener(this),
                new WorldListener(),
                new ServerListener(this),
                new EventListener(this)
        );

        World world = getServer().getWorlds().get(0);

        world.setTime(6000L);

        disableGameRules(world,
                "doDaylightCycle",
                "doEntityDrops",
                "doMobSpawning",
                "doFireTick",
                "showDeathMessages"
        );

        customScoreboard = new CustomScoreboard(this, 20);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> leaderboardManager.updateALl(), 20 * 5L, 20 * 60L);
    }

    private void disableGameRules(World world, String... gameRules) {
        for (String gameRule : gameRules) {
            world.setGameRuleValue(gameRule, "false");
        }
    }

    @Override
    public void onDisable() {
        playerManager.saveProfiles();
        locationManager.saveLocations();
        arenaManager.saveArenas();

        World world = getServer().getWorlds().get(0);

        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player || entity instanceof ItemFrame) {
                continue;
            }

            entity.remove();
        }
    }
}
