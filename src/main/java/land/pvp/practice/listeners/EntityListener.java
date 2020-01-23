package land.pvp.practice.listeners;

import land.pvp.core.utils.message.CC;
import land.pvp.core.utils.timer.Timer;
import land.pvp.practice.PracticePlugin;
import land.pvp.practice.kit.Kit;
import land.pvp.practice.match.Match;
import land.pvp.practice.match.MatchData;
import land.pvp.practice.match.MatchState;
import land.pvp.practice.player.PlayerState;
import land.pvp.practice.player.PracticeProfile;
import land.pvp.practice.tasks.PearlCooldownTask;
import land.pvp.practice.utils.MathUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public class EntityListener implements Listener {
    private final PracticePlugin plugin;

    @EventHandler
    public void onShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity().getShooter();

        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

        if (!profile.isInMatch()) {
            return;
        }

        if (event.getEntityType() == EntityType.SPLASH_POTION) {
            Match match = profile.getMatch();

            if (match.getKit() != Kit.STRAFE) {
                return;
            }

            Projectile projectile = event.getEntity();

            if (player.isSprinting()) {
                Vector velocity = projectile.getVelocity();

                velocity.setY(-3.0);
                projectile.setVelocity(velocity);
            }
        } else if (event.getEntity() instanceof EnderPearl) {
            MatchData matchData = profile.getMatchData();
            Timer timer = matchData.getPearlTimer();

            if (!timer.isActive()) {
                player.setLevel(15);
                new PearlCooldownTask(player, profile).runTaskTimer(plugin, 2L, 2L);
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player entity = (Player) event.getEntity();
            PracticeProfile victimProfile = plugin.getPlayerManager().getProfile(entity.getUniqueId());

            if (victimProfile.getPlayerState() == PlayerState.EVENT) {
                return;
            }

            if (victimProfile.getPlayerState() != PlayerState.FIGHTING) {
                event.setCancelled(true);
                return;
            }

            if (event.getDamager() instanceof Player) {
                PracticeProfile damagerProfile = plugin.getPlayerManager().getProfile(event.getDamager().getUniqueId());

                if (damagerProfile.getPlayerState() == PlayerState.EVENT) {
                    return;
                }

                if (damagerProfile.getPlayerState() != PlayerState.FIGHTING) {
                    event.setCancelled(true);
                    return;
                }

                Match match = damagerProfile.getMatch();

                if (match == null) {
                    return;
                }

                MatchData damagerMatchData = damagerProfile.getMatchData();
                MatchData victimMatchData = victimProfile.getMatchData();

                if (match.getMatchState() != MatchState.FIGHTING
                        || (damagerMatchData.getTeamId() == victimMatchData.getTeamId() && !match.isFfa())) {
                    event.setCancelled(true);
                    return;
                }

                if (match.getKit() == Kit.SUMO) {
                    event.setDamage(0.0);
                }

                damagerMatchData.incrementHits();
                damagerMatchData.incrementCombo();
                victimMatchData.resetCombo();
            } else if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();

                if (arrow.getShooter() instanceof Player) {
                    Player shooter = (Player) arrow.getShooter();

                    if (entity == shooter) {
                        return;
                    }

                    if (victimProfile.isInMatch()) {
                        Match match = victimProfile.getMatch();

                        PracticeProfile damagerProfile = plugin.getPlayerManager().getProfile(shooter.getUniqueId());

                        MatchData damagerMatchData = damagerProfile.getMatchData();
                        MatchData victimMatchData = victimProfile.getMatchData();

                        if (match.getMatchState() != MatchState.FIGHTING
                                || (damagerMatchData.getTeamId() == victimMatchData.getTeamId() && !match.isFfa())) {
                            event.setCancelled(true);
                            return;
                        }

                        double health = MathUtil.roundToHalves(entity.getHealth() - event.getFinalDamage());

                        if (health > 0.0) {
                            shooter.sendMessage(CC.ACCENT + entity.getName() + CC.PRIMARY + " now has " + CC.PINK + health + "❤" + CC.PRIMARY + ".");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPotMiss(PotionSplashEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            for (PotionEffect effect : event.getEntity().getEffects()) {
                if (effect.getType().equals(PotionEffectType.HEAL)) {
                    Player player = (Player) event.getEntity().getShooter();

                    PracticeProfile profile = plugin.getPlayerManager().getProfile(player.getUniqueId());

                    if (profile == null || !profile.isInMatch()) {
                        return;
                    }

                    Match match = profile.getMatch();

                    if (match.getKit() == Kit.STRAFE && player.isSprinting() && event.getIntensity(player) > 0.5D) {
                        event.setIntensity(player, 1.0);
                    }

                    if (event.getIntensity(player) <= 0.5) {
                        MatchData matchData = profile.getMatchData();

                        matchData.incrementMissedPots();
                    }
                    return;
                }
            }
        }
    }
}
