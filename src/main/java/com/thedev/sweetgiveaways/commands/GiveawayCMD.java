package com.thedev.sweetgiveaways.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.thedev.sweetgiveaways.SweetGiveaways;
import com.thedev.sweetgiveaways.module.GiveawayManager;
import com.thedev.sweetgiveaways.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@CommandAlias("giveaway|giveaways")
public class GiveawayCMD extends BaseCommand {

    private final SweetGiveaways plugin;

    public GiveawayCMD(SweetGiveaways plugin) {
        this.plugin = plugin;
    }

    @Default
    @Subcommand("create")
    @Syntax("<seconds>")
    @CommandPermission("giveaways.admin")
    @Description("starts a giveaway with the given seconds")
    @CommandCompletion("@range:1-30")
    public void onGive(Player sender, int seconds) {
        if(sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR) {
            sender.sendMessage(ColorUtil.color("&cPlease use a valid item!"));
            sender.playSound(sender.getLocation(), Sound.NOTE_PIANO, 10, 6);
            return;
        }

        GiveawayManager giveawayManager = plugin.getGiveawayManager();

        if(giveawayManager.getActive()) {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.already-active")));
            sender.playSound(sender.getLocation(), Sound.NOTE_PIANO, 10, 6);
            return;
        }

        giveawayManager.startGiveaway(sender, seconds);
    }

    @Subcommand("cancel")
    @CommandPermission("giveaways.admin")
    @Description("cancels the active giveaway")
    public void onCancel(Player sender) {
        GiveawayManager giveawayManager = plugin.getGiveawayManager();

        if(!giveawayManager.getActive()) {
            sender.sendMessage(ColorUtil.color("&cNo giveaway is active!"));
            sender.playSound(sender.getLocation(), Sound.NOTE_PIANO, 10, 6);

            return;
        }

        giveawayManager.cancelGiveaway();
    }

    @Subcommand("join")
    @Description("joins the active giveaway!")
    public void onJoin(Player sender) {
        GiveawayManager giveawayManager = plugin.getGiveawayManager();

        if(!giveawayManager.getActive()) {
            sender.sendMessage(ColorUtil.color("&cNo giveaway is active!"));
            sender.playSound(sender.getLocation(), Sound.NOTE_PIANO, 10, 6);

            return;
        }

        if(giveawayManager.hasEntry(sender.getUniqueId())) {
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.active-entry")));
            sender.playSound(sender.getLocation(), Sound.VILLAGER_NO, 10, 6);
            return;
        }

        giveawayManager.addEntry(sender.getUniqueId());

        sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.joined-giveaway")));
        sender.playSound(sender.getLocation(), Sound.LEVEL_UP, 10, 6);
    }
}
