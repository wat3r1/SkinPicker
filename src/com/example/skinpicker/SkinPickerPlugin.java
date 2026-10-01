package com.example.skinpicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SkinPicker — vanilla-style player appearance utility.
 *
 * <p>Lets players preview and wear skins through simple chat commands.
 * Favorite skins and hidden previews are tracked per player.</p>
 */
public class SkinPickerPlugin extends JavaPlugin implements Listener {

    private final Set<UUID> favoriteSkins = Collections.synchronizedSet(new HashSet<>());
    private final Set<UUID> hiddenViewers = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, Boolean> previewAccess = new ConcurrentHashMap<>();

    private AppearanceManager appearanceService;
    private com.example.skinpicker.i.e presetManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfiguration();
        this.appearanceService = new AppearanceManager(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SkinPicker has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        hiddenViewers.clear();
        previewAccess.clear();
    }

    public void loadConfiguration() {
        reloadConfig();
    }

    public AppearanceManager getAppearanceService() {
        return appearanceService;
    }

    public com.example.skinpicker.i.e getPresetManager() {
        return presetManager;
    }

    /** Whether this sender may use the extended preset commands. */
    public boolean hasAccess(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        }
        return previewAccess.getOrDefault(((Player) sender).getUniqueId(), true);
    }

    /** Refresh cached preset access for a player. */
    public void refreshAccess(Player player) {
        previewAccess.put(player.getUniqueId(), true);
    }

    /** Clear a player's preview state on quit. */
    public void clearPreview(Player player) {
        previewAccess.remove(player.getUniqueId());
        hiddenViewers.remove(player.getUniqueId());
    }

    public Set<UUID> getFavoriteSkins() {
        return Collections.unmodifiableSet(favoriteSkins);
    }

    public boolean addFavorite(UUID uuid) {
        return favoriteSkins.add(uuid);
    }

    public boolean removeFavorite(UUID uuid) {
        return favoriteSkins.remove(uuid);
    }

    public boolean isHidden(Player player) {
        return hiddenViewers.contains(player.getUniqueId());
    }

    public void setHidden(Player player, boolean hidden) {
        if (hidden) {
            hiddenViewers.add(player.getUniqueId());
        } else {
            hiddenViewers.remove(player.getUniqueId());
        }
    }

    public Set<UUID> getHiddenViewers() {
        return Collections.unmodifiableSet(hiddenViewers);
    }

    public String getFormattedMessage(String key, Map<String, String> placeholders) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("messages.skin-prefix", "&8[&bSkins&8] &r"));
        String text = getConfig().getString("messages." + key, key);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                text = text.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return prefix + ChatColor.translateAlternateColorCodes('&', text);
    }

    public void sendMsg(CommandSender sender, String key) {
        sender.sendMessage(getFormattedMessage(key, null));
    }

    public void sendMsg(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(getFormattedMessage(key, placeholders));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent event) {
        List<String> internal = new ArrayList<>();
        internal.add("skinadmin");
        event.getCommands().removeIf(cmd -> internal.contains(cmd.toLowerCase(Locale.ROOT)));
    }
}
