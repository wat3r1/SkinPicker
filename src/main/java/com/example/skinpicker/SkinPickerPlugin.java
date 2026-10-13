package com.example.skinpicker;

import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SkinPickerPlugin extends JavaPlugin {
   private AppearanceManager appearanceService;

   public void onEnable() {
      this.saveDefaultConfig();
      this.appearanceService = new AppearanceManager(this);
      this.getServer().getPluginManager().registerEvents(new PlayerSessionListener(this), this);
      SkinCommandHandler skinHandler = new SkinCommandHandler(this);
      SkinCompletion skinCompletion = new SkinCompletion(this);

      for (String cmdName : new String[]{"skin", "skins", "shelp"}) {
         PluginCommand pluginCommand = this.getCommand(cmdName);
         if (pluginCommand != null) {
            pluginCommand.setExecutor(skinHandler);
            pluginCommand.setTabCompleter(skinCompletion);
         }
      }

      this.getLogger().info("SkinPicker has been enabled successfully.");
   }

   public void onDisable() {
      this.getLogger().info("SkinPicker has been disabled.");
   }

   public AppearanceManager getAppearanceService() {
      return this.appearanceService;
   }

    public AppearanceManager getSkinManager() {
      return this.appearanceService;
    }

   public String getFormattedMessage(String path, Map<String, String> placeholders) {
      String msg = this.getConfig().getString("messages." + path, "");
      if (msg != null && !msg.isEmpty()) {
         String prefix = this.getConfig().getString("messages.prefix", "");
         String full = prefix + msg;
         if (placeholders != null) {
            for (Entry<String, String> entry : placeholders.entrySet()) {
               full = full.replace("%" + entry.getKey() + "%", entry.getValue());
            }
         }

         return ChatColor.translateAlternateColorCodes('&', full);
      } else {
         return "";
      }
   }

   public void sendMsg(CommandSender sender, String path) {
      this.sendMsg(sender, path, null);
   }

   public void sendMsg(CommandSender sender, String path, Map<String, String> placeholders) {
      String formatted = this.getFormattedMessage(path, placeholders);
      if (!formatted.isEmpty()) {
         sender.sendMessage(formatted);
      }
   }

   public String getSkinFormattedMessage(String path, Map<String, String> placeholders) {
      String msg = this.getConfig().getString("messages." + path, "");
      if (msg != null && !msg.isEmpty()) {
         String prefix = this.getConfig().getString("messages.skin-prefix", this.getConfig().getString("messages.prefix", ""));
         if (prefix == null) {
            prefix = "";
         }

         String full = prefix + msg;
         if (placeholders != null) {
            for (Entry<String, String> entry : placeholders.entrySet()) {
               full = full.replace("%" + entry.getKey() + "%", entry.getValue());
            }
         }

         return ChatColor.translateAlternateColorCodes('&', full);
      } else {
         return "";
      }
   }

   public void sendSkinMsg(CommandSender sender, String path) {
      this.sendSkinMsg(sender, path, null);
   }

   public void sendSkinMsg(CommandSender sender, String path, Map<String, String> placeholders) {
      String formatted = this.getSkinFormattedMessage(path, placeholders);
      if (!formatted.isEmpty()) {
         sender.sendMessage(formatted);
      }
   }
}
