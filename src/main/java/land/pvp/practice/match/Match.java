package land.pvp.practice.match;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import land.pvp.core.utils.item.ItemBuilder;
import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.message.ClickableMessage;
import land.pvp.core.utils.time.TimeUtil;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.arena.Arena;
import land.pvp.practice.constants.ItemHotbars;
import land.pvp.practice.constants.Items;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.kit.PlayerKit;
import land.pvp.practice.party.PartyState;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.utils.PlayerUtil;
import land.pvp.practice.wrapper.EloWrapper;
import land.pvp.practice.wrapper.RatingWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@RequiredArgsConstructor
public class Match extends BukkitRunnable {
    private final Map<PracticeProfile, MatchData> matchData = new HashMap<>();
    @Getter
    private final List<MatchTeam> teams;
    @Getter
    private final Set<UUID> spectators = new HashSet<>();
    @Getter
    private final Kit kit;
    @Getter
    private final Arena arena;
    @Getter
    private final boolean party;
    @Getter
    private final boolean ranked;
    @Getter
    private MatchState matchState = MatchState.STARTING;
    private int secondsElapsed;

    public MatchData getMatchData(PracticeProfile profile) {
        return matchData.get(profile);
    }

    public String formattedTime() {
        return TimeUtil.formatTimeSecondsToClock(secondsElapsed);
    }

    public boolean isFfa() {
        return teams.size() == 1;
    }

    public int playersFighting() {
        int count = 0;

        for (MatchTeam team : teams) {
            count += team.aliveProfiles().size();
        }

        return count;
    }

    public void applySpectatorScoreboard(Player spectator) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Team teamA = scoreboard.registerNewTeam("teamA");
        Team teamB = scoreboard.registerNewTeam("teamB");

        teamA.setPrefix(CC.GREEN);
        teamB.setPrefix(CC.RED);

        MatchTeam matchTeamA = teams.get(0);
        MatchTeam matchTeamB = teams.get(1);

        for (PracticeProfile teamAProfile : matchTeamA.getPlayers()) {
            teamA.addEntry(teamAProfile.getName());
        }

        for (PracticeProfile teamBProfile : matchTeamB.getPlayers()) {
            teamB.addEntry(teamBProfile.getName());
        }

        spectator.setScoreboard(scoreboard);
    }

    private void setScoreboard(Player player, boolean first) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Team teammates = scoreboard.registerNewTeam("teammates");
        Team opponents = scoreboard.registerNewTeam("opponents");

        teammates.setPrefix(CC.GREEN);
        opponents.setPrefix(CC.RED);

        MatchTeam playerTeam = teams.get(first ? 0 : 1);
        MatchTeam opponentTeam = teams.get(first ? 1 : 0);

        for (PracticeProfile teammateProfile : playerTeam.getPlayers()) {
            teammates.addEntry(teammateProfile.getName());
        }

        for (PracticeProfile opponentProfile : opponentTeam.getPlayers()) {
            opponents.addEntry(opponentProfile.getName());
        }

        player.setScoreboard(scoreboard);
    }

    public void start(PracticePlugin plugin) {
        Set<Player> fighters = new HashSet<>();

        for (MatchTeam team : teams) {
            if (party) {
                team.getLeader().getParty().setState(PartyState.FIGHTING);
            }

            for (PracticeProfile profile : team.getPlayers()) {
                profile.setPlayerState(PlayerState.FIGHTING);
                profile.setMatch(this);

                matchData.put(profile, new MatchData(this, team.getTeamId()));

                Player player = profile.asPlayer();

                fighters.add(player);

                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    online.hidePlayer(player);
                    player.hidePlayer(online);
                }

                PlayerUtil.resetPlayerForMatch(player);

                kit.applyAttributes(player);

                if (!profile.hasCustomKits(kit)) {
                    kit.apply(player, true);
                } else {
                    player.getInventory().setItem(0, Items.DEFAULT_KIT_BOOK);

                    for (Map.Entry<Integer, PlayerKit> entry : profile.getKitWrapper(kit).getKits().entrySet()) {
                        int index = entry.getKey();
                        PlayerKit customKit = entry.getValue();

                        player.getInventory().setItem(index, new ItemBuilder(Material.ENCHANTED_BOOK)
                                .name(customKit.getCustomName())
                                .build());
                    }
                }

                player.updateInventory();

                boolean firstTeam = team.getTeamId() == 0;

                if (party && !isFfa()) {
                    setScoreboard(player, firstTeam);
                }

                player.teleport(firstTeam ? arena.getFirstTeamSpawn() : arena.getSecondTeamSpawn());
            }
        }

        for (Player fighter : fighters) {
            for (Player otherFighter : fighters) {
                fighter.showPlayer(otherFighter);
                otherFighter.showPlayer(fighter);
            }
        }

        runTaskTimer(plugin, 0L, 20L);
    }

    private void end(PracticePlugin plugin, MatchTeam winningTeam, MatchTeam losingTeam) {
        secondsElapsed = 0;
        matchState = MatchState.ENDED;

        if (isFfa()) {
            PracticeProfile winnerProfile = winningTeam.aliveProfiles().get(0);

            winnerProfile.setWins(winnerProfile.getWins() + 1);

            Player winner = winnerProfile.asPlayer();
            String winnerMessage = CC.PRIMARY + "Winner" + CC.GRAY + ": " + CC.GREEN + winner.getName();

            int size = winningTeam.getPlayers().size();
            int count = 0;

            ClickableMessage inventoriesMessage = new ClickableMessage("Inventories")
                    .color(ChatColor.PRIMARY)
                    .add(": ")
                    .color(ChatColor.GRAY);

            for (PracticeProfile profile : winningTeam.getPlayers()) {
                Player profilePlayer = profile.asPlayer();

                if (matchData.get(profile).isPlayerInMatch() && profile.getPlayerState() != PlayerState.SPECTATING && profilePlayer != null) {
                    plugin.getSnapshotManager().cacheSnapshot(profilePlayer, false, kit);
                }

                inventoriesMessage
                        .add(profile.getName())
                        .color(ChatColor.GREEN)
                        .hover(ChatColor.PRIMARY + "Click to view inventory")
                        .command("/inv " + profile.getName());

                inventoriesMessage.add(++count == size ? "" : ", ").color(ChatColor.GRAY);
            }

            broadcast(winnerMessage);
            broadcast(inventoriesMessage);
        } else {
            ClickableMessage globalWinnersMessage = new ClickableMessage(party ? "Winners" : "Winner")
                    .color(ChatColor.PRIMARY)
                    .add(": ")
                    .color(ChatColor.GRAY);
            ClickableMessage globalLosersMessage = new ClickableMessage(party ? "Losers" : "Loser")
                    .color(ChatColor.PRIMARY)
                    .add(": ")
                    .color(ChatColor.GRAY);

            int globalSize = winningTeam.getPlayers().size();
            int globalCount = 0;

            for (MatchTeam team : teams) {
                boolean winner = team == winningTeam;

                for (PracticeProfile profile : team.getPlayers()) {
                    if (winner) {
                        profile.setWins(profile.getWins() + 1);

                        globalWinnersMessage
                                .add(profile.getName())
                                .color(ChatColor.GREEN)
                                .hover(ChatColor.PRIMARY + "Click to view inventory")
                                .command("/inv " + profile.getName());
                        globalWinnersMessage.add(++globalCount == globalSize ? "" : ", ").color(ChatColor.GRAY);
                    } else {
                        globalLosersMessage
                                .add(profile.getName())
                                .color(ChatColor.RED)
                                .hover(ChatColor.PRIMARY + "Click to view inventory")
                                .command("/inv " + profile.getName());

                        globalLosersMessage.add(++globalCount == globalSize ? "" : ", ").color(ChatColor.GRAY);
                    }

                    Player profilePlayer = profile.asPlayer();
                    MatchData matchData = this.matchData.get(profile);

                    if (matchData.isPlayerInMatch() && profile.getPlayerState() != PlayerState.SPECTATING && profilePlayer != null && profilePlayer.isOnline()) {
                        plugin.getSnapshotManager().cacheSnapshot(profilePlayer, false, kit);
                    }

                    if (profilePlayer != null) {
                        int size = winningTeam.getPlayers().size();
                        int count = 0;

                        ClickableMessage winnersMessage = new ClickableMessage(party ? "Winners" : "Winner")
                                .color(ChatColor.PRIMARY)
                                .add(": ")
                                .color(ChatColor.GRAY);
                        for (PracticeProfile innerProfile : winningTeam.getPlayers()) {
                            winnersMessage
                                    .add(innerProfile.getName())
                                    .color(ChatColor.GREEN)
                                    .hover(ChatColor.PRIMARY + "Click to view inventory")
                                    .command("/inv " + innerProfile.getName());

                            if (!profile.getName().equals(innerProfile.getName()) && !winner) {
                                winnersMessage.add(" (Send \"gf\")")
                                        .color(ChatColor.ACCENT)
                                        .hover(ChatColor.ACCENT + "Send message")
                                        .command("/msg " + innerProfile.getName() + " gf");
                            }

                            winnersMessage.add(++count == size ? "" : ", ").color(ChatColor.GRAY);
                        }

                        size = losingTeam.getPlayers().size();
                        count = 0;

                        ClickableMessage losersMessage = new ClickableMessage(party ? "Losers" : "Loser")
                                .color(ChatColor.PRIMARY)
                                .add(": ")
                                .color(ChatColor.GRAY);

                        for (PracticeProfile innerProfile : losingTeam.getPlayers()) {
                            losersMessage
                                    .add(innerProfile.getName())
                                    .color(ChatColor.RED)
                                    .hover(ChatColor.PRIMARY + "Click to view inventory")
                                    .command("/inv " + innerProfile.getName());

                            if (!profile.getName().equals(innerProfile.getName()) && winner) {
                                losersMessage.add(" (Send \"gf\")")
                                        .color(ChatColor.ACCENT)
                                        .hover(ChatColor.ACCENT + "Send message")
                                        .command("/msg " + innerProfile.getName() + " gf");
                            }

                            losersMessage.add(++count == size ? "" : ", ").color(ChatColor.GRAY);
                        }

                        if (matchData.isPlayerInMatch()) {
                            winnersMessage.sendToPlayer(profilePlayer, true);
                            losersMessage.sendToPlayer(profilePlayer, true);
                        }
                    }
                }

                globalSize = losingTeam.getPlayers().size();
                globalCount = 0;
            }

            sendSpectators(globalWinnersMessage);
            sendSpectators(globalLosersMessage);

            if (ranked) {
                StringBuilder eloChangesMessageBuilder = new StringBuilder(CC.PRIMARY + "ELO Changes " + CC.SECONDARY);

                if (party) {

                } else {
                    PracticeProfile winnerProfile = winningTeam.getLeader();
                    PracticeProfile loserProfile = losingTeam.getLeader();

                    EloWrapper winnerElo = winnerProfile.getElo(kit);
                    EloWrapper loserElo = loserProfile.getElo(kit);

                    RatingWrapper wrapper = RatingWrapper.getRatingsFrom(winnerElo.getSoloRating(), loserElo.getSoloRating());

                    int newLoserElo = wrapper.getNewLoserRating();

                    if (newLoserElo < EloWrapper.MININMUM_ELO) {
                        newLoserElo = EloWrapper.MININMUM_ELO;
                    }

                    winnerElo.updateRating(wrapper.getNewWinnerRating(), false);
                    loserElo.updateRating(newLoserElo, false);

                    eloChangesMessageBuilder.append("(±").append(wrapper.getDifference()).append(")").append(CC.GRAY).append(": ");

                    eloChangesMessageBuilder.append(CC.GREEN).append(winnerProfile.getName()).append(" (")
                            .append(winnerElo.getSoloRating()).append(")").append(CC.GRAY).append(", ").append(CC.RED).append(loserProfile.getName())
                            .append(" (").append(loserElo.getSoloRating()).append(")");
                }

                broadcast(eloChangesMessageBuilder.toString());
            }
        }
    }

    private void postEnd(PracticePlugin plugin) {
        sendAllPlayersToSpawn(plugin);

        if (!isFfa() && !party) {
            for (MatchTeam team : teams) {
                for (PracticeProfile profile : team.getPlayers()) {
                    MatchData data = matchData.get(profile);

                    if (data != null && !data.isPlayerInMatch()) {
                        continue;
                    }

                    Player player = profile.asPlayer();

                    if (player == null) {
                        continue;
                    }

                    MatchTeam otherTeam = teams.get(team.getTeamId() == 0 ? 1 : 0);
                    Player other = otherTeam.getLeader().asPlayer();

                    if (other != null && other.isOnline()) {
                        profile.setRematcher(other.getUniqueId());
                        player.getInventory().setItem(2, new ItemBuilder(Material.DIAMOND).name(CC.PINK + "Rematch " + other.getName()).build());

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline()
                                        && profile.getPlayerState() == PlayerState.SPAWN
                                        && player.getInventory().getItem(2) != null
                                        && player.getInventory().getItem(2).getType() == Material.DIAMOND) {
                                    player.getInventory().setItem(2, new ItemStack(Material.AIR));
                                    profile.setRematcher(null);
                                }
                            }
                        }.runTaskLater(plugin, 20L * 30);
                    }
                }
            }
        }

        plugin.getMatchManager().removeMatch(this);
    }

    public boolean killPlayer(Player victim, PracticeProfile profile, PracticePlugin plugin) {
        if (profile.getPlayerState() != PlayerState.FIGHTING || matchState == MatchState.ENDED) {
            return false;
        }

        profile.setPlayerState(PlayerState.SPECTATING);
        plugin.getSnapshotManager().cacheSnapshot(victim, true, kit);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (victim.isOnline()) {
                Location deathLocation = victim.getLocation();
                hide(victim);
                victim.spigot().respawn();
                victim.teleport(deathLocation);
            }
        }, 16L);

        Player killer = victim.getKiller();

        String deathMessage = CC.SECONDARY + victim.getName() + CC.PRIMARY + " was "
                + (killer == null ? "killed" : "slain by " + CC.SECONDARY + killer.getName() + CC.PRIMARY)
                + ".";

        broadcast(deathMessage);

        MatchTeam victimTeam = teams.get(matchData.get(profile).getTeamId());
        int remaining = victimTeam.remainingPlayers();

        if (remaining == 0 || (isFfa() && remaining == 1)) {
            MatchTeam winningTeam = isFfa() ? victimTeam : teams.get(victimTeam.getTeamId() == 0 ? 1 : 0);

            end(plugin, winningTeam, victimTeam);
            return false;
        } else {
            profile.setSpectatingMatch(this);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline()) {
                    victim.setAllowFlight(true);
                    victim.setFlying(true);

                    for (Player online : plugin.getServer().getOnlinePlayers()) {
                        online.hidePlayer(victim);
                        victim.hidePlayer(online);
                    }

                    for (MatchTeam team : teams) {
                        for (Player alivePlayer : team.alivePlayers()) {
                            victim.showPlayer(alivePlayer);
                        }
                    }

                    ItemHotbars.SPEC_ITEMS.apply(victim);
                }
            }, 17L);

            return true;
        }
    }

    public void broadcast(String msg) {
        matchAction(player -> player.sendMessage(msg, true));
    }

    public void broadcast(ClickableMessage msg) {
        matchAction(player -> msg.sendToPlayer(player, true));
    }

    private void hide(Player toHide) {
        matchAction(player -> player.hidePlayer(toHide));
    }

    private void sendAllPlayersToSpawn(PracticePlugin plugin) {
        matchAction(player -> plugin.getPlayerManager().resetPlayer(player, true));
    }

    private void sendSpectators(ClickableMessage msg) {
        for (UUID spectator : spectators) {
            Player player = Bukkit.getPlayer(spectator);

            if (player == null) {
                continue;
            }

            msg.sendToPlayer(player);
        }
    }

    private void broadcastWithPling(String msg, boolean highPitch) {
        matchAction(player -> {
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, highPitch ? 2.0F : 1.0F);
            player.sendMessage(msg, true);
        });
    }

    private void matchAction(Consumer<? super Player> action) {
        for (MatchTeam team : teams) {
            for (PracticeProfile profile : team.getPlayers()) {
                MatchData data = matchData.get(profile);

                if (data != null && !data.isPlayerInMatch()) {
                    continue;
                }

                Player player = profile.asPlayer();

                if (player == null) {
                    continue;
                }

                action.accept(player);
            }
        }

        for (UUID spectator : spectators) {
            Player player = Bukkit.getPlayer(spectator);

            if (player == null) {
                continue;
            }

            action.accept(player);
        }
    }

    @Override
    public void run() {
        secondsElapsed++;

        if (matchState == MatchState.ENDED && secondsElapsed == 4) {
            postEnd(PracticePlugin.getInstance());
            cancel();
        } else if (matchState == MatchState.STARTING) {
            if (secondsElapsed == 6) {
                secondsElapsed = 0;
                matchState = MatchState.FIGHTING;
                broadcastWithPling(CC.GREEN + "The match has started!", true);
            } else {
                int count = 6 - secondsElapsed;

                broadcastWithPling(CC.PRIMARY + "The match will start in " + CC.SECONDARY + count
                        + CC.PRIMARY + (count == 1 ? " second" : " seconds") + ".", false);
            }
        }
    }
}
