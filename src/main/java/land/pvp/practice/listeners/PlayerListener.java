package land.pvp.practice.listeners;

import java.util.List;
import java.util.UUID;
import land.pvp.core.CorePlugin;
import land.pvp.core.inventory.menu.Menu;
import land.pvp.core.player.CoreProfile;
import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.message.Messages;
import land.pvp.core.utils.timer.Timer;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.events.Event;
import land.pvp.practice.events.EventStage;
import land.pvp.practice.inventory.menu.impl.KitEditorMenu;
import land.pvp.practice.inventory.menu.impl.PartyEventMenu;
import land.pvp.practice.inventory.menu.impl.PartyListMenu;
import land.pvp.practice.inventory.menu.impl.RankedMenu;
import land.pvp.practice.inventory.menu.impl.SettingsMenu;
import land.pvp.practice.inventory.menu.impl.UnrankedMenu;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.kit.PlayerKit;
import land.pvp.practice.match.Match;
import land.pvp.practice.match.MatchData;
import land.pvp.practice.match.MatchState;
import land.pvp.practice.match.MatchTeam;
import land.pvp.practice.party.Party;
import land.pvp.practice.party.PartyState;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.queue.QueueEntry;
import land.pvp.practice.utils.StringUtil;
import land.pvp.practice.wrapper.CustomKitWrapper;
import land.pvp.scoreboardapi.PlayerScoreboardUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

@RequiredArgsConstructor
public class PlayerListener implements Listener {
    private final PracticePlugin plugin;

    private boolean handlePartyClick(Player player, PracticeProfile profile) {
        if (profile.isInParty()) {
            Party party = profile.getParty();

            if (!party.isProfileLeader(profile)) {
                player.sendMessage(CC.RED + "You can't do this as you aren't the party leader!");
                return false;
            } else if (party.getMembers().size() == 1) {
                player.sendMessage(CC.RED + "You need at least 2 players in your party to do this!");
                return false;
            } else if (party.getState() == PartyState.FIGHTING) {
                player.sendMessage(CC.RED + "You can't start a party event when one is currently going!");
                return false;
            }
        }

        return true;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            plugin.getPlayerManager().createProfile(event.getUniqueId(), event.getName());
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(id);

        if (profile == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Messages.DATA_LOAD_FAIL);
            return;
        } else if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            plugin.getPlayerManager().removeProfile(id);
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            profile.getCurrentTimeType().apply(player);

            if (profile.isHidingPlayers()) {
                land.pvp.practice.utils.PlayerUtil.hideAllPlayersFor(player);
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getPlayerManager().setupPlayer(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (!profile.isInMatch()) {
            return;
        }

        Match match = profile.getMatch();

        if (match.getKit() != Kit.SUMO) {
            return;
        }

        Location to = event.getTo();
        Location from = event.getFrom();

        if ((to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ())
                && match.getMatchState() == MatchState.STARTING) {
            from.setX(from.getBlockX() + 0.5);
            from.setZ(from.getBlockZ() + 0.5);
            event.setTo(from);
        } else if (to.getBlockY() != from.getBlockY() && match.getMatchState() == MatchState.FIGHTING
                && to.getBlockY() < match.getArena().getYVal() - 1) {
            player.setHealth(0.0);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        onDisconnect(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        onDisconnect(event.getPlayer());
    }

    private void onDisconnect(Player player) {
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (profile == null) {
            return;
        }

        switch (profile.getPlayerState()) {
            case FIGHTING: {
                MatchData matchData = profile.getMatchData();
                matchData.setPlayerInMatch(false);

                Match match = profile.getMatch();
                match.killPlayer(player, profile, plugin);

                break;
            }
            case EDITING:
                plugin.getEditorManager().stopEditing(player, profile);
                break;
            case QUEUED:
                plugin.getQueueManager().dequeueTeam(player, profile);
                break;
            case SPECTATING:
                Match match = profile.getMatch();

                if (match != null) {
                    MatchData matchData = profile.getMatchData();
                    matchData.setPlayerInMatch(false);
                } else {
                    plugin.getSpectatorManager().removeSpectator(player, profile);
                }
                break;
            case EVENT:
                Event event = profile.getActiveEvent();
                event.leave(player, profile);
                break;
        }

        if (profile.isInParty()) {
            Party party = profile.getParty();

            if (party.getState() == PartyState.QUEUED) {
                PracticeProfile leader = party.getLeader();
                Player leaderPlayer = leader.asPlayer();

                plugin.getQueueManager().dequeueTeam(leaderPlayer, leader);
            }

            plugin.getPartyManager().leaveParty(player, profile);
        }

        profile.save(plugin);
        plugin.getPlayerManager().removeProfile(player.getUniqueId());
    }

    @EventHandler
    public void onScoreboardUpdate(PlayerScoreboardUpdateEvent event) {
        Player player = event.getPlayer();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (profile == null || !profile.isScoreboardEnabled()) {
            return;
        }

        event.setTitle(CC.PRIMARY + CC.B + "PvP Land " + CC.GRAY + CC.SPLITTER + CC.SECONDARY + " Practice");
        event.setSeparator(CC.BOARD_SEPARATOR);

        switch (profile.getPlayerState()) {
            case SPAWN:
                event.writeLine(CC.PRIMARY + "Online: " + CC.SECONDARY + plugin.getServer().getOnlinePlayers().size());
                event.writeLine(CC.PRIMARY + "Playing: " + CC.SECONDARY + plugin.getMatchManager().playersFighting());
                break;
            case QUEUED:
                QueueEntry entry = plugin.getQueueManager().getEntry(profile);

                event.writeLine(CC.PRIMARY + "Queued For: " + CC.SECONDARY + entry.getKit().getName());

                if (entry.isRanked()) {
                    int[] range = entry.getCurrentRange();
                    event.writeLine(CC.PRIMARY + "ELO Range: "
                            + CC.SECONDARY + StringUtil.formatNumberWithCommas(range[0])
                            + CC.PRIMARY + " - " + CC.SECONDARY + StringUtil.formatNumberWithCommas(range[1]));
                }
                break;
            case EVENT:
                Event activeEvent = profile.getActiveEvent();

                if (activeEvent == null) {
                    return;
                }

                if (activeEvent.getCurrentStage() == EventStage.FIGHTING) {
                    Player first = Bukkit.getPlayer(activeEvent.remainingFighterIds().get(0));

                    if (first == null) {
                        return;
                    }

                    Player second = Bukkit.getPlayer(activeEvent.remainingFighterIds().get(1));

                    if (second == null) {
                        return;
                    }

                    event.writeLine(CC.PRIMARY + first.getName() + CC.SECONDARY + " (" + first.spigot().getPing() + " ms)");
                    event.writeLine(CC.ACCENT + "vs.");
                    event.writeLine(CC.PRIMARY + second.getName() + CC.SECONDARY + " (" + second.spigot().getPing() + " ms)");
                } else if (activeEvent.getCurrentStage() == EventStage.INTERMISSION) {
                    event.writeLine(CC.PRIMARY + "Remaining: " + CC.SECONDARY + (activeEvent.remainingPlayerIds().size() + activeEvent.remainingFighterIds().size()));
                } else if (activeEvent.getCurrentStage() == EventStage.WAITING_FOR_PLAYERS) {
                    event.writeLine(CC.PRIMARY + "Playing: " + CC.SECONDARY + activeEvent.remainingPlayerIds().size());
                }
                break;
            case SPECTATING: {
                Match match = profile.getSpectatingMatch();

                if (match == null || match.getMatchState() == MatchState.ENDED) {
                    return;
                }

                List<MatchTeam> teams = match.getTeams();

                if (match.isFfa()) {
                    event.writeLine(CC.PRIMARY + "Duration: " + CC.SECONDARY + match.formattedTime());
                    event.writeLine("");

                    MatchTeam teamA = teams.get(0);

                    event.writeLine(CC.PRIMARY + "Remaining: " + CC.SECONDARY + teamA.aliveProfiles().size()
                            + "/" + teamA.getPlayers().size());
                } else if (match.isParty()) {
                    event.writeLine(CC.PRIMARY + "Duration: " + CC.SECONDARY + match.formattedTime());
                    event.writeLine("");

                    MatchTeam teamA = teams.get(0);
                    MatchTeam teamB = teams.get(1);

                    event.writeLine(CC.GREEN + "Team A Remaining: " + CC.GRAY + teamA.aliveProfiles().size()
                            + "/" + teamA.getPlayers().size());
                    event.writeLine(CC.RED + "Team B Remaining: " + CC.GRAY + teamB.aliveProfiles().size()
                            + "/" + teamB.getPlayers().size());
                } else {
                    MatchTeam teamA = teams.get(0);
                    MatchTeam teamB = teams.get(1);

                    Player teamALeader = teamA.getLeader().asPlayer();
                    Player teamBLeader = teamB.getLeader().asPlayer();

                    if (teamALeader == null || !teamALeader.isOnline() || teamBLeader == null || !teamBLeader.isOnline()) {
                        return;
                    }

                    event.writeLine(CC.PRIMARY + "Duration: " + CC.SECONDARY + match.formattedTime());
                    event.writeLine(CC.GREEN + teamALeader.getName() + CC.GRAY + " (" + teamALeader.spigot().getPing() + " ms)");
                    event.writeLine(CC.RED + teamBLeader.getName() + CC.GRAY + " (" + teamBLeader.spigot().getPing() + " ms)");
                }
                break;
            }
            case FIGHTING:
                Match match = profile.getMatch();

                if (match == null || match.getMatchState() == MatchState.ENDED) {
                    return;
                }

                List<MatchTeam> teams = match.getTeams();
                int playerTeamId = profile.getMatchData().getTeamId();
                MatchTeam playerTeam = teams.get(playerTeamId);

                if (match.isFfa()) {
                    if (match.getMatchState() == MatchState.FIGHTING) {
                        event.writeLine(CC.GREEN + "Players Left: " + CC.GRAY + playerTeam.aliveProfiles().size()
                                + "/" + playerTeam.getPlayers().size());
                    }
                } else if (match.isParty()) {
                    MatchTeam opponentTeam = playerTeamId == 0 ? teams.get(1) : teams.get(0);

                    if (match.getMatchState() == MatchState.STARTING) {
                        Player opponent = opponentTeam.getLeader().asPlayer();

                        if (opponent == null) {
                            return;
                        }

                        event.writeLine(CC.PRIMARY + "Opponent Party: " + CC.SECONDARY + opponent.getName());
                    } else if (match.getMatchState() == MatchState.FIGHTING) {
                        event.writeLine(CC.PRIMARY + "Duration: " + CC.SECONDARY + match.formattedTime());
                        event.writeLine(CC.GREEN + "Teammates Alive: " + CC.GRAY + playerTeam.aliveProfiles().size()
                                + "/" + playerTeam.getPlayers().size());
                        event.writeLine(CC.RED + "Opponents Alive: " + CC.GRAY + opponentTeam.aliveProfiles().size()
                                + "/" + opponentTeam.getPlayers().size());
                    }
                } else {
                    MatchTeam opponentTeam = playerTeamId == 0 ? teams.get(1) : teams.get(0);
                    Player opponent = opponentTeam.getLeader().asPlayer();

                    if (opponent == null) {
                        return;
                    }

                    event.writeLine(CC.PRIMARY + "Opponent: " + CC.SECONDARY + opponent.getName());

                    if (match.getMatchState() == MatchState.FIGHTING) {
                        event.writeLine(CC.PRIMARY + "Duration: " + CC.SECONDARY + match.formattedTime());
                        event.writeLine(CC.PRIMARY + "Ping: " + CC.SECONDARY + player.spigot().getPing() + " ms "
                                + CC.GRAY + CC.SPLITTER + " " + CC.SECONDARY + opponent.spigot().getPing() + " ms");
                    }
                }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());
        String message = event.getMessage();

        if (profile.isInParty() && (message.startsWith("!") || message.startsWith("@"))) {
            event.setCancelled(true);

            String partyMessage = CC.ACCENT + "(Party) " + player.getDisplayName() + CC.R + ": " + CC.PRIMARY + message.substring(1).trim();

            profile.getParty().broadcast(partyMessage);
        } else if (profile.getPlayerState() == PlayerState.EDITING && profile.getEditorData().isRenaming()) {
            event.setCancelled(true);

            String name = CC.SECONDARY + ChatColor.translateAlternateColorCodes('&', event.getMessage());

            profile.getEditorData().getCustomKit().setCustomName(name);
            player.sendMessage(CC.GREEN + "Set kit name to " + name + CC.GREEN + ".");
            profile.getEditorData().setRenaming(false);
        }
    }

    @EventHandler
    public void onInteractWithPlayer(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (event.getRightClicked() instanceof Player && (profile.getPlayerState() == PlayerState.SPECTATING)
                && player.getItemInHand() != null && player.getItemInHand().getType() == Material.BOOK) {
            player.openInventory(((Player) event.getRightClicked()).getInventory());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR
                || player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        switch (profile.getPlayerState()) {
            case SPAWN:
                if (!event.hasItem()) {
                    return;
                }

                event.setCancelled(true);

                switch (event.getItem().getType()) {
                    case DIAMOND_SWORD:
                        CoreProfile coreProfile = CorePlugin.getInstance().getProfileManager().getProfile(player.getUniqueId());

                        if (!coreProfile.hasDonor() && profile.getWins() < 10) {
                            int neededWins = 10 - profile.getWins();

                            player.sendMessage(CC.RED + "You need to win " + CC.YELLOW + neededWins + CC.RED
                                    + " more fights before you can play ranked matches!");
                            return;
                        }

                        if (plugin.getQueueManager().isRankedEnabled()) {
                            plugin.getMenuManager().getMenu(RankedMenu.class).open(player);
                        } else {
                            player.sendMessage(CC.RED + "Ranked matches are disabled while a server restart is in progress.");
                        }
                        break;
                    case IRON_SWORD:
                        if (handlePartyClick(player, profile)) {
                            plugin.getMenuManager().getMenu(UnrankedMenu.class).open(player);
                        }
                        break;
                    case GOLD_SWORD:
                        if (handlePartyClick(player, profile)) {
                            plugin.getMenuManager().getMenu(PartyEventMenu.class).open(player);
                        }
                        break;
                    case NAME_TAG:
                        plugin.getPartyManager().createParty(player, profile);
                        break;
                    case SKULL_ITEM:
                        if (handlePartyClick(player, profile)) {
                            plugin.getMenuManager().getMenu(PartyListMenu.class).open(player);
                        }
                        break;
                    case INK_SACK:
                        if (profile.getParty().getState() == PartyState.QUEUED) {
                            PracticeProfile leaderProfile = profile.getParty().getLeader();
                            Player leaderPlayer = leaderProfile.asPlayer();

                            plugin.getQueueManager().dequeueTeam(leaderPlayer, leaderProfile);
                        } else {
                            plugin.getPartyManager().leaveParty(player, profile);
                        }
                        break;
                    case PAPER:
                        if (profile.isInParty()) {
                            profile.getParty().displayPartyInfo(player);
                        } else {
                            plugin.getPlayerManager().displayStats(player);
                        }
                        break;
                    case BOOK:
                        plugin.getMenuManager().getMenu(KitEditorMenu.class).open(player);
                        break;
                    case DIAMOND:
                        UUID rematcher = profile.getRematcher();

                        if (rematcher == null) {
                            player.setItemInHand(null);
                            player.sendMessage(CC.RED + "The person you are trying to rematch has logged off.");
                        } else {
                            Player rematcherPlayer = plugin.getServer().getPlayer(rematcher);

                            if (rematcherPlayer == null || !rematcherPlayer.isOnline()) {
                                player.setItemInHand(null);
                                player.sendMessage(CC.RED + "The person you are trying to rematch has logged off.");
                            } else {
                                player.performCommand("duel " + rematcherPlayer.getName());
                            }
                        }
                        break;
                    case WATCH:
                        plugin.getMenuManager().getMenu(SettingsMenu.class).open(player);
                        break;
                }

                player.updateInventory();
                break;
            case QUEUED:
                if (event.hasItem() && event.getItem().getType() == Material.INK_SACK
                        && (!profile.isInParty() || (profile.isInParty() && profile.getParty().isProfileLeader(profile)))) {
                    plugin.getQueueManager().dequeueTeam(player, profile);
                }
                break;
            case FIGHTING:
                if (!event.hasItem()) {
                    return;
                }

                switch (event.getItem().getType()) {
                    case ENDER_PEARL: {
                        Match match = profile.getMatch();

                        if (match.getMatchState() != MatchState.FIGHTING) {
                            event.setCancelled(true);
                            player.updateInventory();
                            player.sendMessage(CC.RED + "You can't use pearls before the match has started.");
                            return;
                        }

                        MatchData matchData = profile.getMatchData();
                        Timer timer = matchData.getPearlTimer();

                        if (timer.isActive(false)) {
                            event.setCancelled(true);
                            player.updateInventory();
                            player.sendMessage(CC.PRIMARY + "You can't throw pearls for another " + CC.SECONDARY + timer.formattedExpiration() + CC.PRIMARY + ".");
                        }
                        break;
                    }
                    case MUSHROOM_SOUP:
                        if (!player.isDead() && player.getHealth() > 0.0 && player.getHealth() <= 19.0) {
                            event.setCancelled(true);
                            double health = player.getHealth() + 7.0;

                            player.setHealth(health > 20.0 ? 20.0 : health);
                            player.getItemInHand().setType(Material.BOWL);
                            player.updateInventory();
                        }
                        break;
                    case ENCHANTED_BOOK: {
                        Match match = profile.getMatch();

                        if (match == null) {
                            return;
                        }

                        Kit kit = match.getKit();
                        CustomKitWrapper kits = profile.getKitWrapper(kit);

                        if (kits == null) {
                            return;
                        }

                        int index = player.getInventory().getHeldItemSlot();
                        PlayerKit playerKit = kits.getKit(index);

                        if (kits.getKit(index) != null) {
                            playerKit.apply(player);
                        }
                        break;
                    }
                    case BOOK: {
                        Match match = profile.getMatch();

                        if (match == null) {
                            return;
                        }

                        Kit kit = match.getKit();

                        kit.apply(player, true);
                        break;
                    }
                    case POTION:
                        if (profile.getMatch().getMatchState() == MatchState.STARTING) {
                            Potion potion = Potion.fromItemStack(event.getItem());

                            if (potion.isSplash()) {
                                event.setCancelled(true);
                                player.updateInventory();
                            }
                        }
                        break;
                }
                break;
            case SPECTATING:
                if (!event.hasItem()) {
                    return;
                }

                event.setCancelled(true);

                switch (event.getItem().getType()) {
                    case PAPER:
                        plugin.getSpectatorManager().informSpectator(player, profile);
                        break;
                    case INK_SACK:
                        Match match = profile.getMatch();

                        if (match != null) {
                            plugin.getPartyManager().leaveParty(player, profile);
                            plugin.getPlayerManager().resetPlayer(player, true);
                        } else {
                            plugin.getSpectatorManager().removeSpectator(player, profile);
                        }
                        break;
                }
                break;
            case EVENT:
                if (!event.hasItem()) {
                    return;
                }

                event.setCancelled(true);

                switch (event.getItem().getType()) {
                    case INK_SACK:
                        Event activeEvent = profile.getActiveEvent();

                        if (activeEvent != null) {
                            activeEvent.leave(player, profile);
                        }
                        break;
                }
                break;
            case EDITING:
                event.setCancelled(true);
                player.updateInventory();

                if (event.getClickedBlock() == null) {
                    return;
                }

                switch (event.getClickedBlock().getType()) {
                    case WOODEN_DOOR:
                    case SIGN:
                    case SIGN_POST:
                        plugin.getEditorManager().stopEditing(player, profile);
                        break;
                    case CHEST: {
                        Kit kit = profile.getEditorData().getKit();

                        boolean hasEditorContents = kit.getEditorContents() == null;

                        if (!hasEditorContents) {
                            for (ItemStack item : kit.getEditorContents()) {
                                if (item != null && item.getType() != Material.AIR) {
                                    hasEditorContents = true;
                                    break;
                                }
                            }
                        }

                        if (hasEditorContents) {
                            Inventory kitInventory = plugin.getServer().createInventory(null, 54, "Kit Layout");

                            kitInventory.setContents(kit.getEditorContents());

                            player.openInventory(kitInventory);
                        } else {
                            player.sendMessage(CC.RED + "This kit doesn't have any editable contents.");
                        }
                        break;
                    }
                    case ANVIL:
                        Menu menu = profile.getEditorData().registerMenu(profile, plugin);
                        menu.open(player);
                        break;
                }
                break;
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (profile.getPlayerState() != PlayerState.FIGHTING || (profile.getMatch() != null && !profile.getMatchData().isPlayerInMatch())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        Item drop = event.getItemDrop();
        Material dropType = drop.getItemStack().getType();

        if (dropType == Material.BOWL) {
            drop.remove();
        }

        if (profile.getPlayerState() != PlayerState.FIGHTING || (profile.getPlayerState() == PlayerState.FIGHTING &&
                dropType == Material.BOOK) || dropType == Material.ENCHANTED_BOOK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (profile.getPlayerState() == PlayerState.FIGHTING
                && profile.getMatch().getKit() != Kit.SOUP
                && profile.getMatch().getKit() != Kit.SUMO) {
            return;
        }

        if (event.getFoodLevel() < 20) {
            event.setFoodLevel(20);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (profile.getPlayerState() == PlayerState.EVENT) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            if (profile.isInMatch()) {
                player.teleport(profile.getMatch().getArena().getFirstTeamSpawn());
            } else {
                player.teleport(plugin.getLocationManager().getSpawn());
            }
        }

        if (!profile.isInMatch()) {
            event.setCancelled(true);
            return;
        }

        Match match = profile.getMatch();

        if (match.getMatchState() != MatchState.FIGHTING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (profile.isInMatch()) {
            if (!profile.getMatch().killPlayer(player, profile, plugin)) {
                event.getDrops().clear();
            }
        }
    }

    @EventHandler
    public void onPearlLand(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Match match = profile.getMatch();

            if (match == null || match.getMatchState() != MatchState.FIGHTING) {
                event.setCancelled(true);
            } else {
                Location pearlLocation = event.getTo();
                Location playerLocation = event.getFrom();

                if (playerLocation.getBlockY() < pearlLocation.getBlockY()) {
                    Block block = pearlLocation.getBlock();

                    for (BlockFace face : BlockFace.values()) {
                        Material type = block.getRelative(face).getType();

                        if (type == Material.GLASS || type == Material.BARRIER) {
                            pearlLocation.setY(pearlLocation.getBlockY() - 1.0);
                            break;
                        }
                    }
                } else {
                    pearlLocation.setY(pearlLocation.getBlockY() + 1.0);
                }

                event.setTo(pearlLocation);
            }
        }
    }
}
