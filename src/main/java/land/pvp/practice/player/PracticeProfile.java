package land.pvp.practice.player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.pvp.core.CorePlugin;
import land.pvp.core.storage.database.MongoRequest;
import land.pvp.core.storage.flatfile.Config;
import land.pvp.core.utils.timer.Timer;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.editor.EditorData;
import land.pvp.practice.events.Event;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.kit.PlayerKit;
import land.pvp.practice.match.Match;
import land.pvp.practice.match.MatchData;
import land.pvp.practice.party.Party;
import land.pvp.practice.timer.EventHostTimer;
import land.pvp.practice.wrapper.CustomKitWrapper;
import land.pvp.practice.wrapper.EloWrapper;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Setter
public class PracticeProfile {
    @Getter
    private final Map<Kit, EloWrapper> elo = new HashMap<>();
    private final Map<Kit, CustomKitWrapper> kits = new HashMap<>();
    @Getter
    private final UUID id;
    @Getter
    private final String name;
    @Getter
    private final MatchRequestHandler matchRequestHandler = new MatchRequestHandler();
    @Getter
    private final Timer eventHostTimer = new EventHostTimer();
    @Getter
    private PlayerState playerState = PlayerState.SPAWN;
    @Getter
    private PlayerTimeType currentTimeType = PlayerTimeType.DAY;
    @Getter
    private UUID duelSelecting;
    @Getter
    private UUID rematcher;
    @Getter
    private Kit selectedKit;
    @Getter
    private Party party;
    @Getter
    private Match match;
    @Getter
    private Match spectatingMatch;
    @Getter
    private Event activeEvent;
    @Getter
    private EditorData editorData;
    @Getter
    private boolean acceptingDuels = true;
    @Getter
    private boolean acceptingPartyInvites = true;
    @Getter
    private boolean scoreboardEnabled = true;
    @Getter
    private boolean allowingSpectators = true;
    @Getter
    private boolean hidingPlayers;
    @Getter
    private long lastEventHostTime;
    @Getter
    private int wins;

    public PracticeProfile(UUID id, String name, PracticePlugin plugin) {
        this.id = id;
        this.name = name;

        CorePlugin.getInstance().getMongoStorage().getOrCreateDocument("practice", id, (document, documentExists) -> {
            if (documentExists) {
                this.currentTimeType = PlayerTimeType.valueOf(document.get("player_time", "DAY"));
                this.acceptingDuels = document.getBoolean("accepting_duels", true);
                this.acceptingPartyInvites = document.getBoolean("accepting_party_invites", true);
                this.scoreboardEnabled = document.getBoolean("scoreboard_enabled", true);
                this.allowingSpectators = document.getBoolean("allowing_spectators", true);
                this.hidingPlayers = document.getBoolean("hiding_players", false);
                this.wins = document.getInteger("wins", 0);

                Document eloDocument = document.get("elo", Document.class);

                if (eloDocument == null) {
                    return;
                }

                for (Kit kit : Kit.values()) {
                    Document kitDocument = eloDocument.get(kit.getName(), Document.class);

                    if (kitDocument == null) {
                        continue;
                    }

                    EloWrapper elo = this.getElo(kit);
                    int soloRating = kitDocument.getInteger("solo_rating", 1000);
                    int partyRating = kitDocument.getInteger("party_rating", 1000);

                    elo.updateRating(soloRating, false);
                    elo.updateRating(partyRating, true);
                }
            }

            Config config = new Config(plugin, "players/" + id);

            for (String kitName : config.getKeys()) {
                CustomKitWrapper customKitWrapper = getKitWrapper(Kit.getByName(kitName));
                ConfigurationSection section = config.getSection(kitName);

                for (String indexString : section.getKeys(false)) {
                    PlayerKit kit = new PlayerKit(kitName);

                    kit.setCustomName(config.getString(kitName + "." + indexString + ".name"));
                    kit.setArmor(config.getItemArray(kitName + "." + indexString + ".armor"));
                    kit.setContents(config.getItemArray(kitName + "." + indexString + ".contents"));

                    customKitWrapper.setKit(Integer.parseInt(indexString), kit);
                }
            }
        });
    }

    public void save(PracticePlugin plugin) {
        save(true, plugin);
    }

    public void save(boolean async, PracticePlugin plugin) {
        Runnable runnable = () -> {
            Config config = new Config(plugin, "players/" + id);

            config.clear();

            for (Kit kit : Kit.values()) {
                CustomKitWrapper customKitWrapper = getKitWrapper(kit);

                for (Map.Entry<Integer, PlayerKit> entry : customKitWrapper.getKits().entrySet()) {
                    int index = entry.getKey();
                    PlayerKit value = entry.getValue();
                    String kitName = value.getName();

                    config.set(kitName + "." + index + ".name", value.getCustomName());
                    config.set(kitName + "." + index + ".armor", value.getArmor());
                    config.set(kitName + "." + index + ".contents", value.getContents());
                }
            }

            config.save();

            MongoRequest request = MongoRequest.newRequest("practice", id)
                    .put("accepting_duels", acceptingDuels)
                    .put("accepting_party_invites", acceptingPartyInvites)
                    .put("scoreboard_enabled", scoreboardEnabled)
                    .put("allowing_specators", allowingSpectators)
                    .put("hiding_players", hidingPlayers)
                    .put("player_time", currentTimeType.name())
                    .put("wins", wins);

            Document document = new Document();

            elo.forEach(((kit, eloWrapper) -> document.append(kit.getName(),
                    new Document()
                            .append("solo_rating", eloWrapper.getSoloRating())
                            .append("party_rating", eloWrapper.getPartyRating()))));

            request.put("elo", document);
            request.run();
        };

        if (async) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        } else {
            runnable.run();
        }
    }

    public boolean isInParty() {
        return party != null;
    }

    public boolean isInMatch() {
        return playerState == PlayerState.FIGHTING;
    }

    public boolean hasCustomKits(Kit kit) {
        return !getKitWrapper(kit).getKits().isEmpty();
    }

    public CustomKitWrapper getKitWrapper(Kit kit) {
        if (kits.get(kit) == null) {
            kits.put(kit, new CustomKitWrapper());
        }

        return kits.get(kit);
    }

    public int getGlobalElo() {
        Collection<EloWrapper> eloWrappers = elo.values();
        int size = eloWrappers.size();

        if (size == 0) {
            return 1000;
        }

        int elo = 0;

        for (EloWrapper wrapper : eloWrappers) {
            elo += wrapper.getSoloRating();
        }

        return elo / size;
    }

    public EloWrapper getElo(Kit kit) {
        if (elo.get(kit) == null) {
            elo.put(kit, new EloWrapper(1000, 1000));
        }

        return elo.get(kit);
    }

    public MatchData getMatchData() {
        return match == null ? null : match.getMatchData(this);
    }

    public Player asPlayer() {
        return Bukkit.getPlayer(id);
    }
}
