package land.pvp.practice.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.message.ClickableMessage;
import land.pvp.practice.arena.Arena;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.match.MatchRequest;
import land.pvp.practice.party.Party;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MatchRequestHandler {
    private static final Object DUMMY = new Object();
    private final Cache<MatchRequest, Object> requests = CacheBuilder
            .newBuilder()
            .expireAfterWrite(15L, TimeUnit.SECONDS)
            .build();

    public MatchRequest getMatchRequest(UUID requester) {
        return requests.asMap().keySet().stream().filter(request -> request.getRequester().equals(requester)).findAny().orElse(null);
    }

    public MatchRequest getMatchRequest(UUID requester, String kitName) {
        return requests.asMap().keySet().stream().filter(req -> req.getRequester().equals(requester) && req.getKitName().equals(kitName)).findAny().orElse(null);
    }

    public void removeMatchRequest(MatchRequest request) {
        requests.asMap().remove(request);
    }

    public void sendMatchRequest(Player sender, Player selected, Kit kit, boolean partyDuel, Party party, Party targetParty, Arena arena) {
        MatchRequest request = new MatchRequest(sender.getUniqueId(), selected.getUniqueId(), kit.getName(), arena, partyDuel);
        requests.put(request, DUMMY);
        sender.closeInventory();

        ClickableMessage requestMsg = new ClickableMessage(sender.getName())
                .color(ChatColor.SECONDARY)
                .add(" has sent you a " + (partyDuel ? "party " : "") + "duel request on arena ")
                .color(ChatColor.PRIMARY)
                .add(arena.getName())
                .color(ChatColor.SECONDARY)
                .add(" with kit ")
                .color(ChatColor.PRIMARY)
                .add(kit.getName())
                .color(ChatColor.SECONDARY)
                .add(". ")
                .color(ChatColor.PRIMARY)
                .add("(Accept)")
                .color(ChatColor.ACCENT)
                .hover(ChatColor.ACCENT + "Click to Accept")
                .command("/accept " + sender.getName() + " " + kit.getName());

        if (partyDuel) {
            targetParty.broadcast(requestMsg);
            party.broadcast(CC.PRIMARY + "Sent a duel request to " + CC.SECONDARY + selected.getName() + CC.PRIMARY + "'s party with kit "
                    + CC.SECONDARY + kit.getName() + CC.PRIMARY + " on arena " + CC.SECONDARY + arena.getName() + CC.PRIMARY + ".");
        } else {
            requestMsg.sendToPlayer(selected, true);
            sender.sendMessage(CC.PRIMARY + "Sent a duel request to " + CC.SECONDARY + selected.getName() + CC.PRIMARY + " with kit "
                    + CC.SECONDARY + kit.getName() + CC.PRIMARY + " on arena " + CC.SECONDARY + arena.getName() + CC.PRIMARY + ".", true);
        }
    }
}
