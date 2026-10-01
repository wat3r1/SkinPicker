package com.example.skinpicker.i;

import com.example.skinpicker.AppearanceManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class b implements d {
   public b(AppearanceManager var1, Player var2) {
      this.d = var1;
      this.c = var2;
   }

   @Override
   public void a(String var1, String var2) {
      Bukkit.getScheduler().runTask(this.d.picker, () -> this.d.applyTextures(var1x, var1, var2));
   }

   @Override
   public void a() {
   }
}
