package com.uniqueskills.abilities;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DashAbility {

    private final Plugin plugin;
    private final Map<UUID, Boolean> enabledPlayers;
    private final Map<UUID, Double> energy; // 0-100
    // State for Dash (Running)
    private final Map<UUID, Boolean> isDashing;

    // Slide Management
    private final Map<UUID, Boolean> hasSlideCharge;
    private final Map<UUID, Long> slideRechargeTime;

    // Energy Regen Management
    private final Map<UUID, Long> lastEmptyTime; // Track when energy hit 0

    private static final double MAX_ENERGY = 100.0;
    private static final double DASH_COST_PER_TICK = 1.0; // Reduced to 1% per tick (~5 sec duration)
    private static final double ENERGY_REGEN_PER_TICK = 0.5; // Slow regen
    private static final long ENERGY_REGEN_DELAY_MS = 3000; // 3 seconds delay after hitting 0

    private static final int SLIDE_DURATION_TICKS = 12; // 0.6 seconds
    private static final long SLIDE_RECHARGE_MS = 5000; // 5 seconds
    private static final double SLIDE_SPEED = 1.2;

    public DashAbility(Plugin plugin) {
        this.plugin = plugin;
        this.enabledPlayers = new HashMap<>();
        this.energy = new HashMap<>();
        this.isDashing = new HashMap<>();
        this.hasSlideCharge = new HashMap<>();
        this.slideRechargeTime = new HashMap<>();
        this.lastEmptyTime = new HashMap<>();

        startTask();
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (isEnabled(player)) {
                        updatePlayer(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void updatePlayer(Player player) {
        UUID uuid = player.getUniqueId();

        // --- Movement Check ---
        // Check if player is moving horizontally
        Vector vel = player.getVelocity();
        boolean isMoving = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ()) > 0.05;
        // Sprinting also counts as moving
        if (player.isSprinting())
            isMoving = true;

        // --- Dash Logic ---
        boolean currentlyDashing = isDashing.getOrDefault(uuid, false);
        double currentEnergy = energy.getOrDefault(uuid, MAX_ENERGY);

        if (currentlyDashing) {
            // APPLY speed if energy > 0 (even if standing still)
            if (currentEnergy > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, 4, false, false, true));

                // CONSUME energy only when moving
                if (isMoving) {
                    currentEnergy -= DASH_COST_PER_TICK;
                    if (currentEnergy < 0)
                        currentEnergy = 0;
                    energy.put(uuid, currentEnergy);

                    // Particles
                    if (player.getTicksLived() % 3 == 0) {
                        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 0.5, 0), 2,
                                0.2, 0.2, 0.2, 0.05);
                        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 0.5, 0),
                                1, 0.2, 0.2, 0.2, 0.02);
                    }
                }
            }

            // Check if run out
            if (currentEnergy <= 0) {
                stopDash(player, true); // Force stop due to energy
            }
        } else {
            // --- Regen Logic (ONLY when dash mode is OFF) ---
            if (currentEnergy < MAX_ENERGY) {
                boolean inDelay = false;
                if (lastEmptyTime.containsKey(uuid)) {
                    long elapsed = System.currentTimeMillis() - lastEmptyTime.get(uuid);
                    if (elapsed < ENERGY_REGEN_DELAY_MS) {
                        inDelay = true;
                    } else {
                        lastEmptyTime.remove(uuid); // Delay over
                    }
                }

                if (!inDelay) {
                    currentEnergy = Math.min(MAX_ENERGY, currentEnergy + ENERGY_REGEN_PER_TICK);
                    energy.put(uuid, currentEnergy);
                }
            }
        }

        // --- Slide Recharge Logic ---
        if (slideRechargeTime.containsKey(uuid)) {
            long elapsed = System.currentTimeMillis() - slideRechargeTime.get(uuid);
            if (elapsed >= SLIDE_RECHARGE_MS) {
                slideRechargeTime.remove(uuid);
                hasSlideCharge.put(uuid, true);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
            }
        }

        // --- Action Bar ---
        updateActionBar(player, currentEnergy);
    }

    private void updateActionBar(Player player, double currentEnergy) {
        UUID uuid = player.getUniqueId();
        boolean hasSlide = hasSlideCharge.getOrDefault(uuid, true);

        StringBuilder display = new StringBuilder();

        // Energy Status
        if (isDashing.getOrDefault(uuid, false)) {
            display.append("§b⚡ Running: ");
        } else {
            display.append("§6⚡ Energy: ");
        }

        display.append(String.format("§e%d%%", (int) currentEnergy));

        // Delay Indicator
        if (lastEmptyTime.containsKey(uuid)) {
            long elapsed = System.currentTimeMillis() - lastEmptyTime.get(uuid);
            if (elapsed < ENERGY_REGEN_DELAY_MS) {
                double remaining = (ENERGY_REGEN_DELAY_MS - elapsed) / 1000.0;
                display.append(String.format(" §c[COOLDOWN %.1fs]", remaining));
            }
        }

        display.append(" §7| §bSlide: ");

        // Slide Status
        if (slideRechargeTime.containsKey(uuid)) {
            long elapsed = System.currentTimeMillis() - slideRechargeTime.get(uuid);
            double remaining = Math.max(0, (SLIDE_RECHARGE_MS - elapsed) / 1000.0);
            display.append(String.format("§c[%.1fs]", remaining));
        } else if (hasSlide) {
            display.append("§a■");
        } else {
            display.append("§7■");
        }

        player.sendActionBar(display.toString());
    }

    private void stopDash(Player player, boolean depleted) {
        UUID uuid = player.getUniqueId();
        isDashing.put(uuid, false);

        // Immediate cleanup of effects
        player.removePotionEffect(PotionEffectType.SPEED);

        if (depleted) {
            lastEmptyTime.put(uuid, System.currentTimeMillis());
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.5f);
            player.sendMessage("§c⚠ Energy depleted!");
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.8f, 1.5f); // Stop sound
        }
    }

    public void setEnabled(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        enabledPlayers.put(uuid, enabled);
        if (enabled) {
            energy.put(uuid, MAX_ENERGY);
            hasSlideCharge.put(uuid, true);
            isDashing.put(uuid, false);
            slideRechargeTime.remove(uuid);
            lastEmptyTime.remove(uuid);
        } else {
            cleanup(uuid);
        }
    }

    public boolean isEnabled(Player player) {
        return enabledPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public void cleanup(UUID uuid) {
        enabledPlayers.remove(uuid);
        energy.remove(uuid);
        hasSlideCharge.remove(uuid);
        slideRechargeTime.remove(uuid);
        lastEmptyTime.remove(uuid);
        isDashing.remove(uuid);
    }

    // Called on Right Click
    public void execute(Player player) {
        if (!isEnabled(player))
            return;

        // Toggle Dash
        toggleDash(player);
    }

    // Called on Sneak Toggle
    public void handleSneak(Player player) {
        if (!isEnabled(player))
            return;

        // Slide only works if currently dashing (running)
        if (isDashing.getOrDefault(player.getUniqueId(), false)) {
            executeSlide(player);
        }
    }

    private void toggleDash(Player player) {
        UUID uuid = player.getUniqueId();
        boolean current = isDashing.getOrDefault(uuid, false);

        if (current) {
            // Turn OFF
            stopDash(player, false);
        } else {
            // Turn ON
            // Check constraints
            if (lastEmptyTime.containsKey(uuid)) {
                // Check if actually in cooldown
                long elapsed = System.currentTimeMillis() - lastEmptyTime.get(uuid);
                if (elapsed < ENERGY_REGEN_DELAY_MS) {
                    player.sendMessage("§cCooldown active!");
                    return;
                }
            }

            if (energy.getOrDefault(uuid, 0.0) < 5.0) { // Min energy to start
                player.sendMessage("§cNot enough energy!");
                return;
            }

            isDashing.put(uuid, true);
            player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 2.0f);
        }
    }

    private void executeSlide(Player player) {
        UUID uuid = player.getUniqueId();

        // Check if has charge
        if (!hasSlideCharge.getOrDefault(uuid, true)) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.5f);
            player.sendMessage("§6[Dash] §cSlide on cooldown!");
            return;
        }

        // Turn off dash mode when sliding
        stopDash(player, false);

        // Consume charge
        hasSlideCharge.put(uuid, false);
        slideRechargeTime.put(uuid, System.currentTimeMillis());

        // Get direction (horizontal only)
        Vector direction = player.getLocation().getDirection().clone();
        direction.setY(0).normalize();

        // Play sounds - Shorter and snappier
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);

        // Apply slide with smoother velocity curve
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !isEnabled(player)) {
                    this.cancel();
                    return;
                }

                // Quad easing
                double progress = (double) ticks / SLIDE_DURATION_TICKS;
                double decay = 1.0 - (progress * progress);
                Vector slideVelocity = direction.clone().multiply(SLIDE_SPEED * decay);

                // Vertical limit
                double verticalVel = Math.min(player.getVelocity().getY(), 0.1);
                slideVelocity.setY(verticalVel);

                player.setVelocity(slideVelocity);

                // Particles (ground trail) - smoother and more frequent
                if (ticks % 2 == 0) {
                    player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                            player.getLocation().add(0, 0.1, 0), 8, 0.4, 0.05, 0.4, 0.08);
                    player.getWorld().spawnParticle(Particle.CRIT,
                            player.getLocation().add(0, 0.2, 0), 3, 0.25, 0.05, 0.25, 0.02);
                    player.getWorld().spawnParticle(Particle.CLOUD,
                            player.getLocation().add(0, 0.05, 0), 2, 0.3, 0.02, 0.3, 0.01);
                }

                // Apply Speed 5 slightly before the slide ends for a smoother transition
                if (ticks == SLIDE_DURATION_TICKS - 3) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 4, false, false, true));
                }

                if (ticks >= SLIDE_DURATION_TICKS) {
                    this.cancel();
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
