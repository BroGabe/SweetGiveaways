package com.thedev.sweetgiveaways.module;

import com.thedev.sweetgiveaways.SweetGiveaways;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The MessageManager class is responsible for managing and formatting messages
 * for the giveaway plugin. It handles placeholders, message conversion, and
 * clickable components based on configurations in the plugin's config file.
 */
public class MessageManager {

    private final FileConfiguration config;

    /**
     * Constructor for MessageManager.
     * Initializes the config based on the provided plugin instance.
     *
     * @param plugin The instance of the SweetGiveaways plugin.
     */
    public MessageManager(SweetGiveaways plugin) {
        this.config = plugin.getConfig();
    }

    /**
     * Replaces placeholders in the message with relevant values such as item name,
     * time left, and the number of entries.
     *
     * @param itemStack The ItemStack to replace %item% placeholder.
     * @param entries   The number of entries to replace %entries% placeholder.
     * @param time      The time left for the giveaway to replace %time% placeholder.
     * @return A function that takes a string and replaces the placeholders with
     *         their actual values.
     */
    private Function<String, String> replacePlaceholders(ItemStack itemStack, int entries, int time) {
        // Get the item name or type
        String itemName = (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
                ? itemStack.getItemMeta().getDisplayName() // If item has a custom name, use it
                : (itemStack != null ? itemStack.getType().name() : "Unknown Item"); // Otherwise, use item type name

        // Return a function that replaces the placeholders
        return s -> s.replace("%item%", itemName)
                .replace("%time%", String.valueOf(time))
                .replace("%entries%", String.valueOf(entries));
    }

    /**
     * Fetches and processes the giveaway message from the configuration, replacing
     * placeholders and creating clickable components where needed.
     *
     * @param itemStack The ItemStack to replace %item% placeholder.
     * @param entries   The number of entries to replace %entries% placeholder.
     * @param seconds   The time remaining for the giveaway to replace %time% placeholder.
     */
    public void getMessage(ItemStack itemStack, int entries, int seconds) {
        // Get the function that will replace placeholders with actual values
        Function<String, String> replacer = replacePlaceholders(itemStack, entries, seconds);

        // Process the messages from the config file
        List<TextComponent> components = config.getStringList("messages.giveaway-msg").stream()
                .map(this::messageConverter) // Convert colors using '&' to Minecraft color codes
                .map(msg -> msg.contains("%click%") ? createClickableComponent(msg) : new TextComponent(msg)) // If %click% is found, create a clickable component
                .peek(comp -> comp.setText(replacer.apply(comp.getText()))) // Apply the placeholder replacements
                .collect(Collectors.toList()); // Collect all components into a list

        // Broadcast all the components to the server
        components.forEach(Bukkit.getServer().spigot()::broadcast);
    }

    /**
     * Creates a clickable component based on the message, allowing users to click
     * and perform an action (e.g., join the giveaway).
     *
     * @param message The message containing the %click% placeholder.
     * @return A TextComponent with clickable functionality.
     */
    private TextComponent createClickableComponent(String message) {
        // Get the default text for the clickable part (e.g., "Click Here")
        String clickText = config.getString("messages.click-char", "Click Here");

        // Split the message into two parts: before and after %click%
        String[] parts = message.split("%click%", 2);

        // Create the base component with the part before %click%
        TextComponent component = new TextComponent(messageConverter(parts[0].trim())); // Trim to remove leading/trailing spaces

        // Create the clickable part with the defined clickable text
        TextComponent clickable = new TextComponent(messageConverter(clickText));
        clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/giveaway join")); // Action when clicked

        // Add a space before the clickable text
        component.addExtra(" ");
        component.addExtra(clickable);

        // If there is more text after %click%, add it to the component
        if (parts.length > 1 && !parts[1].isEmpty()) {
            component.addExtra(new TextComponent(" " + messageConverter(parts[1].trim()))); // Trim the second part as well
        }

        return component;
    }

    /**
     * Converts color codes in the message (using '&') to Minecraft color codes
     * (using '§').
     *
     * @param string The string to convert.
     * @return The string with color codes converted to Minecraft format.
     */
    private String messageConverter(String string) {
        return string.replace("&", "§"); // Replace '&' with Minecraft's color code symbol (§)
    }
}
