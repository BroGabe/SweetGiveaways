package com.thedev.sweetgiveaways.module;

import com.thedev.sweetgiveaways.SweetGiveaways;
import com.thedev.sweetgiveaways.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GiveawayManager {

    private final SweetGiveaways plugin;

    private final FileConfiguration config;

    private final MessageManager messageManager;

    private final List<UUID> entries = new ArrayList<>();

    private Instant timeManager = Instant.now();

    private boolean active = false;

    private BukkitTask giveawayTask = null;

    private ItemStack giveawayItem = null;

    public GiveawayManager(SweetGiveaways plugin) {
        this.plugin = plugin;

        config = plugin.getConfig();

        messageManager = new MessageManager(plugin);
    }

    public void startGiveaway(Player starter, int seconds) {
        if(getActive() || seconds <=10) return;

        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_PIANO, 10, 6));

        setActive(true);

        giveawayItem = starter.getItemInHand();

        timeManager = Instant.now().plusSeconds(seconds);

        messageManager.getMessage(starter.getItemInHand(), entries.size(), seconds);

        giveawayTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int secondsLeft = (int) Duration.between(Instant.now(), timeManager).getSeconds();

            if(Instant.now().isAfter(timeManager) || secondsLeft == 0) {
                setActive(false);

                rewardRandomPlayer();

                Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.LEVEL_UP, 10, 6));

                entries.clear();

                giveawayTask.cancel();
                return;
            }

            if(secondsLeft > 31) return;

            messageManager.getMessage(giveawayItem, entries.size(), (int) Duration.between(Instant.now(), timeManager).getSeconds());
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_BASS, 10, 6));
        }, 20 * 5, 20 * 5);
    }

    public void cancelGiveaway() {
        setActive(false);

        if(giveawayTask != null) {
            giveawayTask.cancel();
        }

        entries.clear();

        Bukkit.broadcastMessage(ColorUtil.color(config.getString("messages.giveaway-cancel")));
        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_BASS, 10, 6));
    }

    public void addEntry(UUID uuid) {
        entries.add(uuid);
    }

    public boolean hasEntry(UUID uuid) {
        return entries.contains(uuid);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    private void rewardRandomPlayer() {
        if(entries.isEmpty()) {
            config.getStringList("messages.no-winner").forEach(s -> Bukkit.broadcastMessage(ColorUtil.color(s)));
            return;
        }

        Random random = new Random();

        UUID uuid = entries.get(random.nextInt(entries.size()));

        config.getStringList("messages.giveaway-winner").forEach(s -> Bukkit.broadcastMessage(ColorUtil.color(s.replace("%player%", Bukkit.getOfflinePlayer(uuid).getName()))));

        if(!Bukkit.getPlayer(uuid).isOnline()) return;

        Bukkit.getPlayer(uuid).getInventory().addItem(giveawayItem);
    }

    public boolean getActive() {
        return active;
    }
}
