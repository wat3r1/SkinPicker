package com.example.skinpicker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSessionListener implements Listener {
   private final SkinPickerPlugin picker;

   public PlayerSessionListener(SkinPickerPlugin picker) {
      this.picker = picker;
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();

      try {
         if (this.picker.getAppearanceService() != null) {
            this.picker.getAppearanceService().reapplySavedSkin(player);
         }
      } catch (Exception var4) {
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();

      try {
         if (this.picker.getAppearanceService() != null) {
            this.picker.getAppearanceService().clearCooldown(player.getUniqueId());
         }
      } catch (Exception exception) {
         this.picker.getLogger().warning("Could not clear cooldown for " + player.getName() + ": " + exception.getMessage());
      }
   }
}
