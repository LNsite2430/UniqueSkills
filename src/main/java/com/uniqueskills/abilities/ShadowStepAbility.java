package com.uniqueskills.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shadow Step Ability
 * 詠唱後、指定した位置にテレポートする
 */
public class ShadowStepAbility {

    private final Plugin plugin;
    private final Map<UUID, Boolean> enabledPlayers;
    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, Boolean> isChanneling;

    private static final long COOLDOWN_MS = 6000;
    private static final int CHANNEL_DURATION_TICKS = 18; // 0.9秒 (さらに高速化)
    private static final double MAX_DISTANCE = 20.0; // 距離制限

    public ShadowStepAbility(Plugin plugin) {
        this.plugin = plugin;
        this.enabledPlayers = new HashMap<>();
        this.cooldowns = new HashMap<>();
        this.isChanneling = new HashMap<>();

        startCooldownTask();
    }

    private void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (isEnabled(player)) {
                        updateActionBar(player);
                        showTargetPreview(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void showTargetPreview(Player player) {
        UUID uuid = player.getUniqueId();

        // Don't show preview if channeling or on cooldown
        if (isChanneling.getOrDefault(uuid, false))
            return;
        if (cooldowns.containsKey(uuid)) {
            long elapsed = System.currentTimeMillis() - cooldowns.get(uuid);
            if (elapsed < COOLDOWN_MS)
                return;
        }

        // Only show if holding Ender Eye
        if (player.getInventory().getItemInMainHand().getType() != org.bukkit.Material.ENDER_EYE) {
            return;
        }

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        RayTraceResult result = player.getWorld().rayTraceBlocks(eyeLoc, direction, MAX_DISTANCE);

        if (result != null && result.getHitBlock() != null) {
            Location targetLoc = result.getHitBlock().getLocation().add(0.5, 1.0, 0.5);

            if (targetLoc.getBlock().getType().isSolid()
                    || targetLoc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                if (targetLoc.getBlock().getType().isSolid())
                    targetLoc.add(0, 1, 0);
            }

            // Show trajectory particles (path to target)
            double distance = eyeLoc.distance(targetLoc);
            int steps = (int) Math.min(distance * 2, 40); // Max 40 particles for performance

            for (int i = 0; i < steps; i++) {
                double t = (double) i / steps;
                Location particleLoc = eyeLoc.clone().add(direction.clone().multiply(distance * t));

                // Small purple particles along the path
                player.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(200, 120, 255), 0.5f));
            }

            // Show larger marker at destination (circular pattern)
            double radius = 0.8;
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2 * i) / 8;
                double x = targetLoc.getX() + Math.cos(angle) * radius;
                double z = targetLoc.getZ() + Math.sin(angle) * radius;
                Location circleLoc = new Location(targetLoc.getWorld(), x, targetLoc.getY() + 0.1, z);

                player.spawnParticle(Particle.REDSTONE, circleLoc, 1, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(180, 80, 255), 1.2f));
            }

            // Center marker
            player.spawnParticle(Particle.END_ROD, targetLoc.clone().add(0, 0.2, 0), 1, 0, 0, 0, 0);
        }
    }

    private void updateActionBar(Player player) {
        UUID uuid = player.getUniqueId();
        if (isChanneling.getOrDefault(uuid, false)) {
            player.sendActionBar("§5§l[Shadow Step] §dChanneling... §7(Don't move)");
            return;
        }

        if (cooldowns.containsKey(uuid)) {
            long elapsed = System.currentTimeMillis() - cooldowns.get(uuid);
            double remaining = Math.max(0, (COOLDOWN_MS - elapsed) / 1000.0);
            if (remaining > 0) {
                player.sendActionBar(String.format("§5§l[Shadow Step] §c[%.1fs]", remaining));
            } else {
                cooldowns.remove(uuid);
                player.sendActionBar("§5§l[Shadow Step] §aReady");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
            }
        } else {
            player.sendActionBar("§5§l[Shadow Step] §aReady");
        }
    }

    public void setEnabled(Player player, boolean enabled) {
        enabledPlayers.put(player.getUniqueId(), enabled);
        if (enabled) {
            cooldowns.remove(player.getUniqueId());
            isChanneling.remove(player.getUniqueId());
        } else {
            cleanup(player.getUniqueId());
        }
    }

    public boolean isEnabled(Player player) {
        return enabledPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public void cleanup(UUID uuid) {
        enabledPlayers.remove(uuid);
        cooldowns.remove(uuid);
        isChanneling.remove(uuid);
    }

    public void execute(Player player) {
        if (!isEnabled(player))
            return;
        UUID uuid = player.getUniqueId();

        if (isChanneling.getOrDefault(uuid, false))
            return;

        if (cooldowns.containsKey(uuid)) {
            long elapsed = System.currentTimeMillis() - cooldowns.get(uuid);
            if (elapsed < COOLDOWN_MS) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.5f);
                return;
            } else {
                cooldowns.remove(uuid);
            }
        }

        // Raytrace for target (Must hit a block)
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        RayTraceResult result = player.getWorld().rayTraceBlocks(eyeLoc, direction, MAX_DISTANCE);

        Location targetLoc;
        if (result != null && result.getHitBlock() != null) {
            targetLoc = result.getHitBlock().getLocation().add(0.5, 1.0, 0.5);

            // Check if target has enough space (2 blocks high)
            if (targetLoc.getBlock().getType().isSolid()
                    || targetLoc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                // If aiming at side of block, adjust? For now, just simplistic checks.
                // Try to find safe spot above
                if (targetLoc.getBlock().getType().isSolid())
                    targetLoc.add(0, 1, 0);
            }
        } else {
            // Block hit is required
            player.sendMessage("§5[Shadow Step] §cTarget is too far or invalid.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        startChanneling(player, targetLoc);
    }

    private void startChanneling(Player player, Location targetLoc) {
        UUID uuid = player.getUniqueId();
        isChanneling.put(uuid, true);

        Location startLoc = player.getLocation().clone();

        // Apply Debuffs (Blindness, Slowness, Jump Boost negative)
        // Duration: Channel time + 5 ticks buffer
        player.addPotionEffect(
                new PotionEffect(PotionEffectType.BLINDNESS, CHANNEL_DURATION_TICKS + 10, 0, false, false, false));
        player.addPotionEffect(
                new PotionEffect(PotionEffectType.SLOW, CHANNEL_DURATION_TICKS + 10, 255, false, false, false)); // No
                                                                                                                 // movement
        player.addPotionEffect(
                new PotionEffect(PotionEffectType.JUMP, CHANNEL_DURATION_TICKS + 10, 250, false, false, false)); // No
                                                                                                                 // jump

        // Sound at start (channeling)
        player.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_STARE, 1.0f, 0.5f);
        player.getWorld().playSound(targetLoc, Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 2.0f);

        // Calculate if this is a fake TP (very short distance)
        double distance = startLoc.distance(targetLoc);
        boolean isFakeTP = distance < 3.0;

        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !isEnabled(player)) {
                    this.cancel();
                    isChanneling.put(uuid, false);
                    return;
                }

                // Visuals at Target (Rotating Circle) - VISIBLE TO ALL PLAYERS
                angle += Math.PI / 10;
                double radius = 0.8;

                for (int i = 0; i < 2; i++) {
                    double currentAngle = angle + (i * Math.PI);
                    double x = targetLoc.getX() + Math.cos(currentAngle) * radius;
                    double z = targetLoc.getZ() + Math.sin(currentAngle) * radius;
                    Location pLoc = new Location(targetLoc.getWorld(), x, targetLoc.getY() + 0.1, z);

                    // Use world.spawnParticle so ALL players can see it
                    targetLoc.getWorld().spawnParticle(Particle.REDSTONE, pLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(138, 43, 226), 1.5f));
                }

                // Pillar effect at target (VISIBLE TO ALL)
                if (ticks % 5 == 0) {
                    targetLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, targetLoc.clone().add(0, 0.5, 0), 3, 0.2,
                            0.5, 0.2, 0.01);
                }

                // Effect at Player (Smoke) - VISIBLE TO ALL
                player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().add(0, 1, 0), 2, 0.3, 0.5,
                        0.3, 0.02);

                if (ticks >= CHANNEL_DURATION_TICKS) {
                    if (isFakeTP) {
                        executeFakeTeleport(player, startLoc);
                    } else {
                        initiateRealTeleport(player, startLoc, targetLoc);
                    }
                    this.cancel();
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeFakeTeleport(Player player, Location startLoc) {
        UUID uuid = player.getUniqueId();
        isChanneling.put(uuid, false);

        // Play loud departure sound (enemies hear this and think you teleported)
        player.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 1.0f);

        // Play effect at current location
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, startLoc.clone().add(0, 1, 0), 20, 0.5, 1.0, 0.5, 0.1);

        // Message to player
        player.sendMessage("§5[Shadow Step] §7Fake teleport executed!");

        // Start Cooldown
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    private void initiateRealTeleport(Player player, Location startLoc, Location targetLoc) {
        UUID uuid = player.getUniqueId();

        // Step 1: Play LOUD departure sound (21m radius equivalent)
        // Volume 3.0 = very loud, everyone nearby hears this
        player.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, startLoc.clone().add(0, 1, 0), 20, 0.5, 1.0, 0.5, 0.1);

        // Step 2: Wait 0.7 seconds (14 ticks) - hitbox remains at departure location
        // During this time, player is still vulnerable at the START location
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    isChanneling.put(uuid, false);
                    return;
                }

                // Step 3: Actually teleport
                finishTeleport(player, targetLoc);
            }
        }.runTaskLater(plugin, 14L); // 0.7 seconds
    }

    private void finishTeleport(Player player, Location targetLoc) {
        UUID uuid = player.getUniqueId();
        isChanneling.put(uuid, false);

        // Set direction to match player's current look (keep facing)
        targetLoc.setYaw(player.getLocation().getYaw());
        targetLoc.setPitch(player.getLocation().getPitch());

        // Actually teleport
        player.teleport(targetLoc);

        // Play quieter arrival sound (15m radius equivalent)
        // Volume 1.0 = normal, much quieter than departure
        player.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.SQUID_INK, targetLoc.clone().add(0, 1, 0), 20, 0.5, 1.0, 0.5, 0.1);

        // Apply vulnerability debuffs for 0.6 seconds (12 ticks)
        // Player cannot fight effectively immediately after arrival
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 12, 3, false, false, false)); // Slow movement
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 12, 5, false, false, false)); // Can't
                                                                                                             // mine/attack
                                                                                                             // fast
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 12, 0, false, false, false)); // Reduced
                                                                                                         // damage

        // Start Cooldown
        cooldowns.put(uuid, System.currentTimeMillis());
    }
}
