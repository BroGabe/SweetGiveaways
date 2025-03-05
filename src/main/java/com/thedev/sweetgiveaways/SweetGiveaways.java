package com.thedev.sweetgiveaways;

import co.aikar.commands.PaperCommandManager;
import com.thedev.sweetgiveaways.commands.GiveawayCMD;
import com.thedev.sweetgiveaways.module.GiveawayManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class SweetGiveaways extends JavaPlugin {

    private GiveawayManager giveawayManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        giveawayManager = new GiveawayManager(this);

        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.registerCommand(new GiveawayCMD(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public GiveawayManager getGiveawayManager() {
        return giveawayManager;
    }

}
