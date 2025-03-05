package com.thedev.sweetgiveaways.module;

import com.thedev.sweetgiveaways.SweetGiveaways;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class MessageManager {

    private final FileConfiguration config;

    public MessageManager(SweetGiveaways plugin) {
        config = plugin.getConfig();
    }

    private Function<String, String> replacePlaceholders(ItemStack itemStack, int entries, int time) {
        String itemName = (itemStack.hasItemMeta() ? itemStack.getItemMeta().getDisplayName() : itemStack.getType().name());
        return (s -> s.replace("%item%", itemName)
                .replace("%time%", String.valueOf(time))
                .replace("%entries%", String.valueOf(entries)));
    }

    public void getMessage(ItemStack itemStack, int entries, int seconds) {
        List<TextComponent> textComponents = new ArrayList<>();

        for(String string : config.getStringList("messages.giveaway-msg")) {
            TextComponent newComponent = ((string.contains("%click%")) ?
                    null : new TextComponent(messageConverter(string)));

            if(newComponent != null) textComponents.add(newComponent);

            setClickablePlaceholder(textComponents, messageConverter(string));
        }

        textComponents.forEach(com -> {
            com.setText(replacePlaceholders(itemStack, entries, seconds).apply(com.getText()));
            Bukkit.getServer().spigot().broadcast(com);
        });
    }

    private void setClickablePlaceholder(List<TextComponent> components, String string) {
        if(!string.contains("%click%")) return;

        StringBuilder stringBuilder = new StringBuilder();

        TextComponent component = null;

        String[] splitString = string.split(" ");

        Iterator<String> stringIterator = Arrays.stream(splitString).iterator();

        while (stringIterator.hasNext()) {
            String iteratedString = stringIterator.next();

            if(!iteratedString.contains("%click%")) {
                stringBuilder.append(iteratedString).append(" ");
                continue;
            }

            if(!stringBuilder.toString().isEmpty() && component == null) {
                component = new TextComponent(stringBuilder.toString());
            } else if (!stringBuilder.toString().isEmpty()){
                component.addExtra(stringBuilder.toString());
            }

            stringBuilder = new StringBuilder();

            TextComponent clickable = new TextComponent(messageConverter(iteratedString.replace("%click%", config.getString("messages.click-char"))) + " ");
            clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/giveaway join"));

            if(component == null) {
                component = clickable;
            } else {
                component.addExtra(clickable);
            }
        }

        if(stringBuilder.toString().isEmpty()) {
            components.add(component);
            return;
        }

        assert component != null;
        component.addExtra(stringBuilder.toString());

        components.add(component);
    }

    private String messageConverter(String string) {
        return string.replace("&", "§");
    }
}
