package com.example.skinpicker.i;

import com.example.skinpicker.AppearanceManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class a implements d {
   public a(AppearanceManager var1, Player var2) {
      this.b = var1;
      this.a = var2;
   }

   @Override
   public void a(String var1, String var2) {
      Bukkit.getScheduler().runTask(this.b.picker, () -> {
         Player var4 = Bukkit.getPlayer(var1x.getUniqueId());
         if (var4 != null && var4.isOnline()) {
            this.b.applyTextures(var4, var1, var2);
         }
      });
   }

   @Override
   public void a() {
   }
}
