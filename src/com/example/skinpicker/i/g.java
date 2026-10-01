package com.example.skinpicker.i;

import com.example.skinpicker.AppearanceManager;
import com.example.skinpicker.SkinCommandHandler;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class g implements d {
   public g(SkinCommandHandler var1, Player var2, CommandSender var3, AppearanceManager var4, String var5, boolean var6) {
      this.p = var1;
      this.k = var2;
      this.l = var3;
      this.m = var4;
      this.n = var5;
      this.o = var6;
   }

   @Override
   public void a(String var1, String var2) {
      Bukkit.getScheduler().runTask(this.p.picker, () -> {
         Player var8 = Bukkit.getPlayer(var1x.getUniqueId());
         if (var8 != null && var8.isOnline()) {
            var3.applyTextures(var8, var1, var2);
            var3.saveSkinSelection(var8.getUniqueId(), var6.toLowerCase(Locale.ROOT));
            if (var7) {
               this.p.picker.sendSkinMsg(var2x, "skin-changed", Collections.singletonMap("skin", var6));
            } else {
               Map var9 = Map.of("target", var8.getName(), "skin", var6);
               this.p.picker.sendSkinMsg(var2x, "skin-changed-other", var9);
               this.p.picker.sendSkinMsg(var8, "skin-changed-by-other", Map.of("skin", var6, "setter", var2x.getName()));
            }
         } else {
            var2x.sendMessage(ChatColor.RED + "Player went offline.");
         }
      });
   }

   @Override
   public void a() {
      Bukkit.getScheduler().runTask(this.p.picker, () -> this.p.picker.sendSkinMsg(var1, "skin-not-found", Collections.singletonMap("skin", var2)));
   }
}
