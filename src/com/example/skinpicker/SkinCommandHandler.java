package com.example.skinpicker;

import com.example.skinpicker.i.g;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinCommandHandler implements CommandExecutor {
   public final SkinPickerPlugin picker;
   private final Random random = new Random();

   public SkinCommandHandler(SkinPickerPlugin var1) {
      this.picker = var1;
   }

   public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
      AppearanceManager var5 = this.picker.getAppearanceService();
      String var6 = var5.getPermUse();
      String var7 = var5.getPermOthers();
      if (var1 instanceof Player var8 && !var8.hasPermission(var6) && !this.picker.hasAccess(var8)) {
         this.picker.sendSkinMsg(var1, "skin-no-permission");
         return true;
      } else if (!(var1 instanceof Player var13)) {
         if (var4.length < 2) {
            this.picker.sendSkinMsg(var1, "skin-console-usage");
            return true;
         } else {
            Player var14 = Bukkit.getPlayer(var4[0]);
            if (var14 == null) {
               var1.sendMessage("Player not found: " + var4[0]);
               return true;
            } else {
               this.handleSkinChange(var1, var14, var4[1], var5, true);
               return true;
            }
         }
      } else if (var4.length == 0) {
         this.picker.sendSkinMsg(var13, "skin-usage");
         return true;
      } else {
         String var9 = var4[0];
         if (var9.equalsIgnoreCase("reset")) {
            if (var4.length >= 2) {
               if (!var13.hasPermission(var7) && !this.picker.hasAccess(var13)) {
                  this.picker.sendSkinMsg(var13, "skin-no-permission-others");
                  return true;
               } else {
                  Player var17 = Bukkit.getPlayer(var4[1]);
                  if (var17 == null) {
                     var13.sendMessage(ChatColor.RED + "Player not found: " + var4[1]);
                     return true;
                  } else {
                     this.resetTarget(var1, var17, var5);
                     return true;
                  }
               }
            } else if (!var5.isAllowReset()) {
               this.picker.sendSkinMsg(var13, "skin-reset-disabled");
               return true;
            } else {
               long var16 = var5.getRemainingCooldown(var13);
               if (var16 > 0L) {
                  this.picker.sendSkinMsg(var13, "skin-cooldown", Collections.singletonMap("seconds", String.valueOf(var16)));
                  return true;
               } else {
                  var5.markUsed(var13);
                  var5.resetSkin(var13);
                  var5.clearSkinSelection(var13.getUniqueId());
                  this.picker.sendSkinMsg(var13, "skin-reset");
                  return true;
               }
            }
         } else if (var9.equalsIgnoreCase("random")) {
            if (!var5.isAllowRandom()) {
               this.picker.sendSkinMsg(var13, "skin-random-disabled");
               return true;
            } else {
               Player var15 = var13;
               if (var4.length >= 2) {
                  if (!var13.hasPermission(var7) && !this.picker.hasAccess(var13)) {
                     this.picker.sendSkinMsg(var13, "skin-no-permission-others");
                     return true;
                  }

                  var15 = Bukkit.getPlayer(var4[1]);
                  if (var15 == null) {
                     var13.sendMessage(ChatColor.RED + "Player not found: " + var4[1]);
                     return true;
                  }
               }

               if (var15.getUniqueId().equals(var13.getUniqueId())) {
                  long var11 = var5.getRemainingCooldown(var13);
                  if (var11 > 0L) {
                     this.picker.sendSkinMsg(var13, "skin-cooldown", Collections.singletonMap("seconds", String.valueOf(var11)));
                     return true;
                  }

                  var5.markUsed(var13);
               }

               List var18 = var5.getRandomPool();
               String var12 = (String)var18.get(this.random.nextInt(var18.size()));
               this.handleSkinChange(var1, var15, var12, var5, false);
               return true;
            }
         } else if (var4.length >= 2) {
            if (!var13.hasPermission(var7) && !this.picker.hasAccess(var13)) {
               this.picker.sendSkinMsg(var13, "skin-no-permission-others");
               return true;
            } else {
               Player var10 = Bukkit.getPlayer(var4[0]);
               if (var10 == null) {
                  var13.sendMessage(ChatColor.RED + "Player not found: " + var4[0]);
                  return true;
               } else {
                  this.handleSkinChange(var1, var10, var4[1], var5, false);
                  return true;
               }
            }
         } else {
            this.handleSkinChange(var1, var13, var9.toLowerCase(Locale.ROOT), var5, false);
            return true;
         }
      }
   }

   private void handleSkinChange(CommandSender var1, Player var2, String var3, AppearanceManager var4, boolean var5) {
      boolean var6 = var1 instanceof Player var7 && var7.getUniqueId().equals(var2.getUniqueId());
      if (var6 && !var5 && var1 instanceof Player var10) {
         long var8 = var4.getRemainingCooldown(var10);
         if (var8 > 0L) {
            this.picker.sendSkinMsg(var10, "skin-cooldown", Collections.singletonMap("seconds", String.valueOf(var8)));
            return;
         }

         var4.markUsed(var10);
      }

      this.picker.sendSkinMsg(var1, "skin-fetching", Collections.singletonMap("skin", var3));
      var4.fetchSkinAsync(var3.toLowerCase(Locale.ROOT), new g(this, var2, var1, var4, var3, var6));
   }

   private void resetTarget(CommandSender var1, Player var2, AppearanceManager var3) {
      var3.resetSkin(var2);
      var3.clearSkinSelection(var2.getUniqueId());
      boolean var4 = var1 instanceof Player var5 && var5.getUniqueId().equals(var2.getUniqueId());
      if (var4) {
         this.picker.sendSkinMsg(var1, "skin-reset");
      } else {
         this.picker.sendSkinMsg(var1, "skin-reset-other", Collections.singletonMap("target", var2.getName()));
         this.picker.sendSkinMsg(var2, "skin-reset");
      }
   }
}
