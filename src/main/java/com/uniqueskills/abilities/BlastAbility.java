package com.uniqueskills.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Blast Pack ability
 * TNTトロッコ(アイテム)を投げて、爆発時にプレイヤーを吹き飛ばす
 * 2チャージ制: 1回目は短距離、2回目は長距離
 */
public class BlastAbility {

    private final Plugin plugin;
    private final Map<UUID, Boolean> enabledPlayers;
    private final Map<UUID, Integer> charges;
    private final Map<UUID, Long> lastThrowTime;
    private final Map<UUID, Boolean> isInAir;
    private final Map<UUID, Long> landingTime;
    private final Map<UUID, Item> activePacks;

    // Ability settings
    private static final double THROW_VELOCITY = 0.6;
    private static final int FUSE_TICKS = 100;
    private static final double BLAST_RADIUS = 5.0;
    private static final double BLAST_POWER_FIRST = 1.35;
    private static final double BLAST_POWER_SECOND = 2.0;
    private static final double UPWARD_MULTIPLIER = 0.35;
    private static final long CHARGE_COOLDOWN_MS = 500;
    private static final long LANDING_RECHARGE_MS = 4000;

    public BlastAbility(Plugin plugin) {
        this.plugin = plugin;
        this.enabledPlayers = new HashMap<>();
        this.charges = new HashMap<>();
        this.lastThrowTime = new HashMap<>();
        this.isInAir = new HashMap<>();
        this.landingTime = new HashMap<>();
        this.activePacks = new HashMap<>();

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
                    boolean currentlyOnGround = player.isOnGround();

                    // Detect landing
                    if (wasInAir && currentlyOnGround) {
                        if (charges.getOrDefault(uuid, 2) < 2 && !landingTime.containsKey(uuid)) {
                            landingTime.put(uuid, System.currentTimeMillis());
                        }
                    }

                    isInAir.put(uuid, !currentlyOnGround);

                    // Recharge to 2 after landing
                    if (landingTime.containsKey(uuid)) {
                        long timeSinceLanding = System.currentTimeMillis() - landingTime.get(uuid);

                        if (timeSinceLanding >= LANDING_RECHARGE_MS) {
                            int oldCharges = charges.getOrDefault(uuid, 2);
                            charges.put(uuid, 2);
                            landingTime.remove(uuid);

                            if (oldCharges < 2) {
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                            }
                        }
                    }

                    updateActionBar(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void updateActionBar(Player player) {
        int currentCharges = getCharges(player);
        String chargeDisplay;

        if (currentCharges == 2) {
            chargeDisplay = "§e§l[Blast] §a■■";
        } else {
            if (landingTime.containsKey(player.getUniqueId())) {
                long elapsed = System.currentTimeMillis() - landingTime.get(player.getUniqueId());
                double remaining = Math.max(0, (LANDING_RECHARGE_MS - elapsed) / 1000.0);
                String boxes = (currentCharges == 1) ? "§a■§7■" : "§7■■";
                chargeDisplay = String.format("§e§l[Blast] %s §c[%.1fs]", boxes, remaining);
            } else {
                if (currentCharges == 1) {
                    chargeDisplay = "§e§l[Blast] §a■§7■";
                } else {
                    chargeDisplay = "§e§l[Blast] §7■■";
                }
            }
        }

        player.sendActionBar(chargeDisplay);
    }

    public void togglePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        boolean newState = !enabledPlayers.getOrDefault(uuid, false);
        enabledPlayers.put(uuid, newState);

        if (newState) {
            charges.put(uuid, 2);
        }
    }

    public boolean isEnabled(Player player) {
        return enabledPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public void setEnabled(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        enabledPlayers.put(uuid, enabled);
        if (enabled) {
            charges.put(uuid, 2);
        }
    }

    public int getCharges(Player player) {
        return charges.getOrDefault(player.getUniqueId(), 2);
    }

    public void cleanup(UUID uuid) {
        enabledPlayers.remove(uuid);
        charges.remove(uuid);
        lastThrowTime.remove(uuid);
        isInAir.remove(uuid);
        landingTime.remove(uuid);

        if (activePacks.containsKey(uuid)) {
            Item item = activePacks.get(uuid);
            if (item != null && item.isValid()) {
                item.remove();
            }
            activePacks.remove(uuid);
        }
    }

    public boolean canThrow(Player player) {
        UUID uuid = player.getUniqueId();

        if (getCharges(player) <= 0) {
            return false;
        }

        if (lastThrowTime.containsKey(uuid)) {
            long timeSinceLastThrow = System.currentTimeMillis() - lastThrowTime.get(uuid);
            if (timeSinceLastThrow < CHARGE_COOLDOWN_MS) {
                return false;
            }
        }

        return true;
    }

    public void executeBlastPack(Player player) {
        if (!isEnabled(player)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        // If there's an active pack, detonate it
        if (activePacks.containsKey(uuid)) {
            // Prevent instant detonation after throwing (require 100ms delay)
            if (lastThrowTime.containsKey(uuid)) {
                long timeSinceThrow = System.currentTimeMillis() - lastThrowTime.get(uuid);
                if (timeSinceThrow < 100) {
                    return;
                }
            }
            detonateBlastPack(player);
            return;
        }

        if (!canThrow(player)) {
            return;
        }

        throwBlastPack(player);
    }

    private void throwBlastPack(Player player) {
        UUID uuid = player.getUniqueId();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().multiply(THROW_VELOCITY);

        ItemStack itemStack = new ItemStack(Material.TNT_MINECART);
        Item item = player.getWorld().dropItem(eyeLoc, itemStack);
        item.setVelocity(direction);
        item.setPickupDelay(32767);

        activePacks.put(uuid, item);

        int currentCharges = getCharges(player);
        charges.put(uuid, currentCharges - 1);
        landingTime.remove(uuid);
        lastThrowTime.put(uuid, System.currentTimeMillis());

        player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 0.8f);

        // Auto-remove if not detonated
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activePacks.get(uuid) == item) {
                    if (item.isValid())
                        item.remove();
                    activePacks.remove(uuid);
                }
            }
        }.runTaskLater(plugin, FUSE_TICKS);
    }

    private void detonateBlastPack(Player player) {
        UUID uuid = player.getUniqueId();
        Item item = activePacks.get(uuid);

        if (item == null || !item.isValid()) {
            activePacks.remove(uuid);
            return;
        }

        Location blastLoc = item.getLocation();

        item.remove();
        activePacks.remove(uuid);

        // Determine blast power based on whether player is in air
        boolean playerInAir = isInAir.getOrDefault(uuid, false);
        double blastPower = playerInAir ? BLAST_POWER_SECOND : BLAST_POWER_FIRST;

        blastLoc.getWorld().createExplosion(blastLoc, 0.0f, false, false);

        blastLoc.getWorld().playSound(blastLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.2f);

        blastLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, blastLoc, 3, 0.5, 0.5, 0.5, 0.1);
        blastLoc.getWorld().spawnParticle(Particle.FLAME, blastLoc, 30, 1.0, 1.0, 1.0, 0.1);
        blastLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, blastLoc, 20, 1.5, 1.5, 1.5, 0.05);

        // Apply blast force to nearby players
        blastLoc.getWorld().getNearbyEntities(blastLoc, BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .forEach(nearbyPlayer -> {
                    double distance = nearbyPlayer.getLocation().distance(blastLoc);
                    if (distance > BLAST_RADIUS)
                        return;

                    UUID nearbyUuid = nearbyPlayer.getUniqueId();
                    boolean nearbyPlayerInAir = isInAir.getOrDefault(nearbyUuid, false);

                    Vector blastDirection = nearbyPlayer.getLocation().toVector()
                            .subtract(blastLoc.toVector())
                            .normalize();

                    Vector lookDirection = nearbyPlayer.getLocation().getDirection().normalize();

                    Vector finalDirection;

                    if (nearbyPlayerInAir) {
                        // 空中の場合: 慣性をリセットして視線方向に飛ぶ
                        nearbyPlayer.setVelocity(new Vector(0, 0, 0));
                        finalDirection = lookDirection;
                    } else {
                        // 地面の場合: ブレンド
                        finalDirection = blastDirection.multiply(0.8).add(lookDirection.multiply(0.2)).normalize();
                    }

                    double power = blastPower * (1.0 - (distance / BLAST_RADIUS));

                    Vector velocity = finalDirection.multiply(power);
                    velocity.setY(velocity.getY() * UPWARD_MULTIPLIER);

                    nearbyPlayer.setVelocity(velocity);
                });
    }
}
