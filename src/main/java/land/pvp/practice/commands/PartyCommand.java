package land.pvp.practice.commands;

import land.pvp.core.commands.PlayerCommand;
import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.message.ClickableMessage;
import land.pvp.core.utils.message.Messages;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.party.Party;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PartyCommand extends PlayerCommand {
    private static final String NOT_IN_PARTY = CC.RED + "You aren't in a party!";
    private static final String ALREADY_IN_PARTY = CC.RED + "You're already in a party!";
    private static final String NOT_LEADER = CC.RED + "Only the party leader can do this!";
    private final PracticePlugin plugin;

    public PartyCommand(PracticePlugin plugin) {
        super("party");
        this.plugin = plugin;
        setAliases("p");
        setUsage(CC.RED + "Usage: /party <subcommand> [args]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(usageMessage);
            return;
        }

        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (profile.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(CC.RED + "You must be in spawn to use party commands!");
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (profile.isInParty()) {
                    player.sendMessage(ALREADY_IN_PARTY);
                } else {
                    plugin.getPartyManager().createParty(player, profile);
                }
                break;
            case "leave":
            case "disband":
                if (profile.isInParty()) {
                    plugin.getPartyManager().leaveParty(player, profile);
                } else {
                    player.sendMessage(NOT_IN_PARTY);
                }
                break;
            case "invite":
                if (profile.isInParty()) {
                    Party party = profile.getParty();

                    if (party.isProfileLeader(profile)) {
                        if (args.length < 2) {
                            player.sendMessage(CC.RED + "You must specify a player to invite.");
                            return;
                        }

                        Player target = plugin.getServer().getPlayer(args[1]);

                        if (target == null) {
                            player.sendMessage(Messages.PLAYER_NOT_FOUND);
                            return;
                        }

                        PracticeProfile targetProfile = plugin.getPlayerManager().getProfile(target.getUniqueId());

                        if (targetProfile.getPlayerState() != PlayerState.SPAWN) {
                            player.sendMessage(CC.RED + target.getDisplayName() + CC.RED + " is not in spawn!");
                            return;
                        }

                        if (targetProfile.isInParty()) {
                            player.sendMessage(CC.RED + target.getDisplayName() + CC.RED + " is already in a party!");
                            return;
                        }

                        if (!targetProfile.isAcceptingPartyInvites()) {
                            player.sendMessage(CC.RED + target.getDisplayName() + CC.RED + " is not accepting party invites!");
                            return;
                        }

                        if (party.getMembers().contains(targetProfile)) {
                            player.sendMessage(CC.RED + target.getDisplayName() + CC.RED + " is already a member of your party!");
                            return;
                        }

                        party.addInvite(target.getUniqueId());
                        party.broadcast(CC.GREEN + "Sent a party invite to " + target.getDisplayName() + CC.GREEN + ".");

                        new ClickableMessage("You have been invited to " + player.getDisplayName())
                                .color(ChatColor.PRIMARY)
                                .add("'s party. ").color(ChatColor.PRIMARY)
                                .add("(Join)")
                                .color(ChatColor.ACCENT)
                                .command("/party join " + player.getName())
                                .hover(CC.ACCENT + "Click to Join")
                                .sendToPlayer(target);
                    } else {
                        player.sendMessage(NOT_LEADER);
                    }
                } else {
                    player.sendMessage(NOT_IN_PARTY);
                }
                break;
            case "join":
                if (profile.isInParty()) {
                    player.sendMessage(ALREADY_IN_PARTY);
                    return;
                }

                if (profile.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "You're not in spawn.");
                    return;
                }

                if (args.length < 2) {
                    player.sendMessage(CC.RED + "You must specify a party leader's name to join.");
                    return;
                }

                Player target = plugin.getServer().getPlayer(args[1]);

                if (target == null) {
                    player.sendMessage(Messages.PLAYER_NOT_FOUND);
                    return;
                }

                PracticeProfile targetProfile = plugin.getPlayerManager().getProfile(target.getUniqueId());

                if (targetProfile.isInParty()) {
                    plugin.getPartyManager().joinParty(player, profile, targetProfile.getParty());
                } else {
                    player.sendMessage(target.getDisplayName() + CC.RED + " is not in a party!");
                }

                break;
            case "kick":
                if (profile.isInParty()) {
                    Party party = profile.getParty();

                    if (party.isProfileLeader(profile)) {
                        if (args.length < 2) {
                            player.sendMessage(CC.RED + "You must specify a player to kick.");
                            return;
                        }

                        Player kickTarget = plugin.getServer().getPlayer(args[1]);

                        if (kickTarget == null) {
                            player.sendMessage(Messages.PLAYER_NOT_FOUND);
                            return;
                        }

                        PracticeProfile targetKickProfile = plugin.getPlayerManager().getProfile(kickTarget.getUniqueId());

                        if (party.isProfileLeader(targetKickProfile)) {
                            player.sendMessage(CC.RED + "You can't kick yourself.");
                            return;
                        }

                        plugin.getPartyManager().kickMember(kickTarget, targetKickProfile);
                    } else {
                        player.sendMessage(NOT_LEADER);
                    }
                } else {
                    player.sendMessage(NOT_IN_PARTY);
                }
                break;
            case "open":
            case "close":
                if (profile.isInParty()) {
                    if (profile.getParty().isProfileLeader(profile)) {
                        Party party = profile.getParty();
                        boolean open = !party.isOpen();
                        party.setOpen(open);
                        party.broadcast(CC.PRIMARY + "Your party is now " + (open ? CC.GREEN + "open" : CC.RED + "closed") + CC.PRIMARY + ".");
                    } else {
                        player.sendMessage(NOT_LEADER);
                    }
                } else {
                    player.sendMessage(NOT_IN_PARTY);
                }
                break;
            case "info":
            case "list":
                if (profile.isInParty()) {
                    profile.getParty().displayPartyInfo(player);
                } else {
                    player.sendMessage(NOT_IN_PARTY);
                }
                break;
            default:
                player.sendMessage(usageMessage);
                break;
        }
    }
}
