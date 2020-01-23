package land.pvp.practice.leaderboard;

import com.mongodb.client.MongoCursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import land.pvp.core.CorePlugin;
import land.pvp.core.player.CoreProfile;
import land.pvp.core.player.rank.Rank;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.wrapper.EloWrapper;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

@RequiredArgsConstructor
public class Leaderboard {
    private final List<RankedRating> ratings = new ArrayList<>();
    private final PracticePlugin plugin;
    private final String kitName;

    public void update() {
        List<RankedRating> newRatings = new ArrayList<>();
        List<UUID> addedPlayerIds = new ArrayList<>();

        for (PracticeProfile profile : plugin.getPlayerManager().getProfiles()) {
            for (Map.Entry<Kit, EloWrapper> entry : profile.getElo().entrySet()) {
                String kitName = entry.getKey().getName();

                if (!kitName.equals(this.kitName)) {
                    continue;
                }

                UUID id = profile.getId();
                CoreProfile coreProfile = CorePlugin.getInstance().getProfileManager().getProfile(id);

                if (coreProfile == null) {
                    continue;
                }

                String displayName = coreProfile.getRank().getFormat() + profile.getName();

                addedPlayerIds.add(id);
                newRatings.add(new RankedRating(profile.getName(), displayName, id, entry.getValue().getSoloRating()));
            }
        }

        MongoCursor<Document> practiceStats = CorePlugin.getInstance().getMongoStorage().getAllDocuments("practice");

        while (practiceStats.hasNext()) {
            Document practicePlayerDocument = practiceStats.next();
            UUID id = practicePlayerDocument.get("_id", UUID.class);

            if (addedPlayerIds.contains(id)) {
                continue;
            }

            Document eloDocument = practicePlayerDocument.get("elo", Document.class);

            if (eloDocument == null) {
                continue;
            }

            for (Map.Entry<String, Object> entry : eloDocument.entrySet()) {
                String kitName = entry.getKey();

                if (!kitName.equals(this.kitName)) {
                    continue;
                }

                Document kitEloDocument = eloDocument.get(kitName, Document.class);

                if (kitEloDocument == null) {
                    continue;
                }

                Document document = CorePlugin.getInstance().getMongoStorage().getDocument("players", id);

                if (document == null) {
                    continue;
                }

                String name = document.getString("name");

                if (name == null) {
                    continue;
                }

                String rankName = document.getString("rank_name");
                Rank rank = Rank.getByName(rankName);

                if (rank != null) {
                    String displayName = rank.getFormat() + name;
                    int rating = kitEloDocument.getInteger("solo_rating");

                    newRatings.add(new RankedRating(name, displayName, id, rating));
                }
            }
        }

        newRatings.sort(RatingComparator.getInstance());

        ratings.clear();
        ratings.addAll(newRatings);
    }

    public List<RankedRating> getRatings() {
        synchronized (ratings) {
            return ratings;
        }
    }

    public List<RankedRating> getRatings(int index) {
        synchronized (ratings) {
            if (ratings.isEmpty()) {
                return Collections.emptyList();
            }

            if (ratings.size() < index) {
                return ratings;
            }

            return ratings.subList(index, Math.min(index + 20, ratings.size()));
        }
    }
}
