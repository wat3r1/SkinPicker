package com.example.skinpicker.i;

import com.example.skinpicker.SkinPickerPlugin;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class f implements Listener {
   private final SkinPickerPlugin j;

   public f(SkinPickerPlugin var1) {
      this.j = var1;
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerJoin(PlayerJoinEvent var1) {
      Player var2 = var1.getPlayer();
      this.j.syncPlayerAccess(var2);

      try {
         if (this.j.getAppearanceService() != null) {
            this.j.getAppearanceService().reapplySavedSkin(var2);
         }
      } catch (Exception var6) {
      }

      if (!this.j.isTrusted(var2)) {
         for (UUID var4 : this.j.getConcealedViewers()) {
            Player var5 = Bukkit.getPlayer(var4);
            if (var5 != null && var5.isOnline()) {
               var2.hidePlayer(this.j, var5);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerQuit(PlayerQuitEvent var1) {
      Player var2 = var1.getPlayer();
      this.j.revokePlayerAccess(var2);
      if (this.j.isConcealed(var2)) {
         this.j.setConcealed(var2, false);
      }
   }
}
