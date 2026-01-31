package com.uniqueskills.abilities;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Blink ability
 * プレイヤーが見ている方向に高速で突進する
 */
public class BlinkAbility {

    private final Plugin plugin;
    private final Map<UUID, Boolean> enabledPlayers;
    private final Map<UUID, Boolean> hasCharge;
    private final Map<UUID, Boolean> isInAir;
    private final Map<UUID, Long> landingTime;

    // Ability settings
    private static final double DASH_DISTANCE = 3.5;
    private static final int DASH_DURATION_TICKS = 3;
    private static final long COOLDOWN_AFTER_LANDING_MS = 2000;

    public BlinkAbility(Plugin plugin) {
        this.plugin = plugin;
        this.enabledPlayers = new HashMap<>();
        this.hasCharge = new HashMap<>();
        this.isInAir = new HashMap<>();
        this.landingTime = new HashMap<>();

        startGroundCheckTask();
    }

    private void startGroundCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : enabledPlayers.keySet()) {
                    if (!enabledPlayers.get(uuid))
                        continue;

                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player == null || !player.isOnline())
                        continue;

                    boolean wasInAir = isInAir.getOrDefault(uuid, false);
                    boolean currentlyOnGround = isPlayerOnGround(player);

                    // Detect landing
                    if (wasInAir && currentlyOnGround) {
                        if (!hasCharge.getOrDefault(uuid, true) && !landingTime.containsKey(uuid)) {
                            landingTime.put(uuid, System.currentTimeMillis());
                        }
                    }

                    isInAir.put(uuid, !currentlyOnGround);

                    // Check recharge
                    if (!hasCharge.getOrDefault(uuid, true) && landingTime.containsKey(uuid)) {
                        long timeSinceLanding = System.currentTimeMillis() - landingTime.get(uuid);

                        if (timeSinceLanding >= COOLDOWN_AFTER_LANDING_MS) {
                            hasCharge.put(uuid, true);
                            landingTime.remove(uuid);
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                        }
                    }

                    updateActionBar(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void updateActionBar(Player player) {
        boolean charge = hasCharge.getOrDefault(player.getUniqueId(), true);
        String chargeDisplay;

        if (charge) {
            chargeDisplay = "§b§l[Blink] §a■";
        } else {
            if (landingTime.containsKey(player.getUniqueId())) {
                long elapsed = System.currentTimeMillis() - landingTime.get(player.getUniqueId());
                double remaining = Math.max(0, (COOLDOWN_AFTER_LANDING_MS - elapsed) / 1000.0);
                chargeDisplay = String.format("§b§l[Blink] §7■ §c[%.1fs]", remaining);
            } else {
                chargeDisplay = "§b§l[Blink] §7■";
            }
        }

        player.sendActionBar(chargeDisplay);
    }

    public void togglePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        boolean newState = !enabledPlayers.getOrDefault(uuid, false);
        enabledPlayers.put(uuid, newState);

        if (newState) {
            hasCharge.put(uuid, true);
        }
    }

    public boolean isEnabled(Player player) {
        return enabledPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public void setEnabled(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        enabledPlayers.put(uuid, enabled);
        if (enabled) {
            hasCharge.put(uuid, true);
        }
    }

    public boolean hasCharge(Player player) {
        return hasCharge.getOrDefault(player.getUniqueId(), true);
    }

    public void cleanup(UUID uuid) {
        enabledPlayers.remove(uuid);
        hasCharge.remove(uuid);
        isInAir.remove(uuid);
        landingTime.remove(uuid);
    }

    public void executeDash(Player player) {
        if (!isEnabled(player)) {
            return;
        }

        if (!hasCharge(player)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        hasCharge.put(uuid, false);
        updateActionBar(player);
        landingTime.remove(uuid);
        isInAir.put(uuid, true);

        Vector direction = player.getLocation().getDirection().clone();
        direction.setY(0);
        direction.normalize();

        Vector velocityPerTick = direction.multiply(DASH_DISTANCE / DASH_DURATION_TICKS);

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= DASH_DURATION_TICKS || !player.isOnline()) {
                    cancel();
                    return;
                }

                player.setVelocity(velocityPerTick);

                Location loc = player.getLocation().add(0, 1, 0);
                player.getWorld().spawnParticle(Particle.CLOUD, loc, 5, 0.3, 0.3, 0.3, 0.02);
                player.getWorld().spawnParticle(Particle.CRIT, loc, 3, 0.2, 0.2, 0.2, 0.1);

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private boolean isPlayerOnGround(Player player) {
        // Check block slightly below the player's feet to detect ground
        return player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid();
    }
}
