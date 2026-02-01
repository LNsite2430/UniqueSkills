package com.uniqueskills.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Teleport ability
 * ビーコンを放ち、再発動でテレポートする
 * スニーク右クリックでFake Teleport
 */
public class TeleportAbility {

    private final Plugin plugin;
    private final Map<UUID, Boolean> enabledPlayers;
    private final Map<UUID, GatecrashSession> activeSessions;

    private final Map<UUID, Boolean> hasCharge;
    private final Map<UUID, Long> cooldownStartTime;

    private static final double BEACON_SPEED = 0.25;
    private static final int MAX_DURATION_TICKS = 600; // 30秒 (全体寿命)
    private static final int MOVE_DURATION_TICKS = 300; // 15秒 (移動時間)
    private static final long RECHARGE_TIME_MS = 3500;

    public TeleportAbility(Plugin plugin) {
        this.plugin = plugin;
        this.enabledPlayers = new HashMap<>();
        this.activeSessions = new HashMap<>();
        this.hasCharge = new HashMap<>();
        this.cooldownStartTime = new HashMap<>();

        startCooldownCheckTask();
    }

    private void startCooldownCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                for (UUID uuid : enabledPlayers.keySet()) {
                    if (!enabledPlayers.get(uuid))
                        continue;

                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player == null || !player.isOnline())
                        continue;

                    if (!hasCharge.getOrDefault(uuid, true)) {
                        if (!activeSessions.containsKey(uuid)) {
                            if (cooldownStartTime.containsKey(uuid)) {
                                long timeSinceCooldownStart = currentTime - cooldownStartTime.get(uuid);

                                if (timeSinceCooldownStart >= RECHARGE_TIME_MS) {
                                    hasCharge.put(uuid, true);
                                    cooldownStartTime.remove(uuid);
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f,
                                            1.5f);
                                }
                            } else {
                                cooldownStartTime.put(uuid, currentTime);
                            }
                        }
                    }

                    updateActionBar(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void updateActionBar(Player player) {
        UUID uuid = player.getUniqueId();
        boolean charge = hasCharge.getOrDefault(uuid, true);
        StringBuilder sb = new StringBuilder();

        sb.append("§9§l[Teleport] ");
        sb.append(charge ? "§a■" : "§7■");

        if (activeSessions.containsKey(uuid)) {
            GatecrashSession session = activeSessions.get(uuid);
            double remainingSeconds = (MAX_DURATION_TICKS - session.getLifeTime()) / 20.0;
            sb.append(" §e[").append(String.format("%.1fs", remainingSeconds)).append("]");
        } else if (!charge && cooldownStartTime.containsKey(uuid)) {
            long elapsed = System.currentTimeMillis() - cooldownStartTime.get(uuid);
            double remaining = Math.max(0, (RECHARGE_TIME_MS - elapsed) / 1000.0);
            sb.append(" §c[").append(String.format("%.1fs", remaining)).append("]");
        }

        player.sendActionBar(sb.toString());
    }

    public void togglePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        boolean newState = !enabledPlayers.getOrDefault(uuid, false);
        enabledPlayers.put(uuid, newState);

        if (newState) {
            hasCharge.put(uuid, true);
            cooldownStartTime.remove(uuid);
        } else {
            if (activeSessions.containsKey(uuid)) {
                activeSessions.get(uuid).cancelSession();
                activeSessions.remove(uuid);
            }
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
            cooldownStartTime.remove(uuid);
        } else {
            if (activeSessions.containsKey(uuid)) {
                activeSessions.get(uuid).cancelSession();
                activeSessions.remove(uuid);
            }
        }
    }

    public void cleanup(UUID uuid) {
        enabledPlayers.remove(uuid);
        hasCharge.remove(uuid);
        cooldownStartTime.remove(uuid);

        if (activeSessions.containsKey(uuid)) {
            activeSessions.get(uuid).cancelSession();
            activeSessions.remove(uuid);
        }
    }

    public void execute(Player player) {
        if (!isEnabled(player))
            return;

        UUID uuid = player.getUniqueId();

        if (activeSessions.containsKey(uuid)) {
            if (player.isSneaking()) {
                fakeTeleport(player);
            } else {
                teleport(player);
            }
            return;
        }

        if (!hasCharge.getOrDefault(uuid, true)) {
            return;
        }

        if (player.isSneaking()) {
            spawnBeaconOnSpot(player);
        } else {
            spawnBeacon(player);
        }
    }

    private void spawnBeaconOnSpot(Player player) {
        UUID uuid = player.getUniqueId();
        Location startLoc = player.getLocation().add(0, 0.5, 0);
        Vector direction = new Vector(0, 0, 0); // 速度0

        GatecrashSession session = new GatecrashSession(uuid, startLoc, direction);
        activeSessions.put(uuid, session);
        session.runTaskTimer(plugin, 0L, 1L);

        // Cooldown starts when ability ENDS (tp, fake, expire).
        hasCharge.put(uuid, false);

        // Play different sound for static placement
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0f, 2.0f);
    }

    private void spawnBeacon(Player player) {
        UUID uuid = player.getUniqueId();
        Location startLoc = player.getLocation().add(0, 0.5, 0);
        Vector direction = player.getLocation().getDirection().setY(0).normalize().multiply(BEACON_SPEED);

        GatecrashSession session = new GatecrashSession(uuid, startLoc, direction);
        activeSessions.put(uuid, session);
        session.runTaskTimer(plugin, 0L, 1L);

        // Cooldown starts when ability ENDS (tp, fake, expire).
        hasCharge.put(uuid, false);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.0f, 1.5f);
    }

    private void teleport(Player player) {
        UUID uuid = player.getUniqueId();
        GatecrashSession session = activeSessions.get(uuid);
        if (session == null)
            return;

        Location targetLoc = session.getCurrentLocation();
        targetLoc.setDirection(player.getLocation().getDirection());
        targetLoc.add(0, 0.5, 0);

        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5,
                new Particle.DustOptions(Color.BLUE, 2));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 0.5f);

        player.teleport(targetLoc);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 5, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20, 5, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false, false));

        targetLoc.getWorld().spawnParticle(Particle.REDSTONE, targetLoc.clone().add(0, 1, 0), 20, 0.5, 1, 0.5,
                new Particle.DustOptions(Color.BLUE, 2));
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 1.5f);

        session.cancelSession();
        activeSessions.remove(uuid);

        cooldownStartTime.put(uuid, System.currentTimeMillis());
    }

    private void fakeTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        GatecrashSession session = activeSessions.get(uuid);
        if (session == null)
            return;

        Location beaconLoc = session.getCurrentLocation().clone();

        beaconLoc.getWorld().spawnParticle(Particle.REDSTONE, beaconLoc.clone().add(0, 1, 0), 20, 0.5, 1, 0.5,
                new Particle.DustOptions(Color.BLUE, 2));
        beaconLoc.getWorld().playSound(beaconLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 1.5f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 50) {
                    this.cancel();
                    return;
                }

                double y = beaconLoc.getY() + 0.15;
                Location center = beaconLoc.clone();
                center.setY(y);

                for (int i = 0; i < 8; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double radius = Math.random() * 0.7;
                    double x = center.getX() + Math.cos(angle) * radius;
                    double z = center.getZ() + Math.sin(angle) * radius;

                    Location pLoc = new Location(center.getWorld(), x, y, z);

                    pLoc.getWorld().spawnParticle(Particle.REDSTONE, pLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0, 0, 139), 2.0f));
                    if (Math.random() > 0.8) {
                        pLoc.getWorld().spawnParticle(Particle.WATER_SPLASH, pLoc, 1, 0, 0, 0, 0);
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        player.sendMessage("§9§l[Teleport] §bFake Teleport!");

        session.cancelSession();
        activeSessions.remove(uuid);

        cooldownStartTime.put(uuid, System.currentTimeMillis());
    }

    private class GatecrashSession extends BukkitRunnable {
        private final UUID ownerId;
        private Location currentLocation;
        private final Vector initialDirection;
        private int lifeTime = 0;
        private boolean isStuck = false;
        private ItemDisplay displayEntity;

        public GatecrashSession(UUID ownerId, Location startLoc, Vector direction) {
            this.ownerId = ownerId;
            this.currentLocation = startLoc;
            this.initialDirection = direction.clone();

            if (startLoc.getWorld() != null) {
                this.displayEntity = startLoc.getWorld().spawn(startLoc, ItemDisplay.class, entity -> {
                    entity.setItemStack(new ItemStack(Material.HEART_OF_THE_SEA));
                    Transformation transformation = entity.getTransformation();
                    transformation.getScale().set(0.5f, 0.5f, 0.5f);
                    entity.setTransformation(transformation);
                    entity.setTeleportDuration(1);
                });
            }
        }

        public Location getCurrentLocation() {
            return currentLocation;
        }

        public int getLifeTime() {
            return lifeTime;
        }

        public void cancelSession() {
            if (displayEntity != null && displayEntity.isValid()) {
                displayEntity.remove();
            }
            this.cancel();
        }

        @Override
        public void run() {
            if (lifeTime >= MAX_DURATION_TICKS) {
                activeSessions.remove(ownerId);
                this.cancelSession();

                cooldownStartTime.put(ownerId, System.currentTimeMillis());

                return;
            }

            currentLocation.getWorld().spawnParticle(Particle.REDSTONE, currentLocation.clone().add(0, 0.2, 0), 2, 0.1,
                    0, 0.1, new Particle.DustOptions(Color.BLUE, 1.0f));
            currentLocation.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, currentLocation.clone().add(0, 0.1, 0),
                    1, 0.05, 0.05, 0.05, 0.01);

            if (lifeTime % 5 == 0) {
                currentLocation.getWorld().spawnParticle(Particle.REDSTONE, currentLocation.clone().add(0, 0.05, 0), 1,
                        0.3, 0, 0.3, new Particle.DustOptions(Color.fromRGB(0, 0, 150), 2.0f));
            }

            if (!isStuck) {
                double decay = 0;
                if (lifeTime < MOVE_DURATION_TICKS) {
                    decay = 1.0 - ((double) lifeTime / MOVE_DURATION_TICKS);
                    decay = Math.max(0, decay);
                }

                if (decay <= 0.001) {
                    // Do nothing
                } else {
                    Vector currentVelocity = initialDirection.clone().multiply(decay);

                    Location nextLoc = currentLocation.clone().add(currentVelocity);

                    if (nextLoc.getBlock().getType().isSolid()) {
                        boolean found = false;
                        double[] angles = { 15, -15, 30, -30, 45, -45, 60, -60, 75, -75 };

                        for (double angleDeg : angles) {
                            double angleRad = Math.toRadians(angleDeg);
                            Vector rotated = currentVelocity.clone().rotateAroundY(angleRad);
                            Location testLoc = currentLocation.clone().add(rotated);

                            if (!testLoc.getBlock().getType().isSolid()) {
                                nextLoc = testLoc;
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                            isStuck = true;
                    }

                    if (!isStuck) {
                        if (!nextLoc.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
                            nextLoc.add(0, -0.6, 0);
                        }
                    }

                    if (!isStuck) {
                        if (!nextLoc.getBlock().getType().isSolid()) {
                            currentLocation = nextLoc;
                        } else {
                            isStuck = true;
                        }
                    }
                }
            } else {
                if (lifeTime % 20 == 0) {
                    currentLocation.getWorld().playSound(currentLocation, Sound.BLOCK_BEACON_AMBIENT, 0.1f, 2.0f);
                }
            }

            // Update Display
            if (displayEntity != null && displayEntity.isValid()) {
                Location displayLoc = currentLocation.clone();
                displayLoc.setYaw(lifeTime * 15);
                displayLoc.setPitch(0);
                displayEntity.teleport(displayLoc);
            }

            lifeTime++;
        }
    }
}
