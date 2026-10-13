package com.example.skinpicker;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinCommandHandler implements CommandExecutor {
   private final SkinPickerPlugin picker;
   private final Random random = new Random();

   public SkinCommandHandler(SkinPickerPlugin picker) {
      this.picker = picker;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      return dispatch(sender, label, args);
   }

   public boolean dispatch(CommandSender sender, String label, String[] args) {
      if ((label.equalsIgnoreCase("shelp") || label.equalsIgnoreCase("skinpicker")) && sender instanceof Player selfHelp) {
         this.picker.sendSkinMsg(selfHelp, "skin-usage");
         return true;
      }
      AppearanceManager skins = this.picker.getAppearanceService();
      String permUse = skins.getPermUse();
      String permOthers = skins.getPermOthers();
      if (sender instanceof Player player && !player.hasPermission(permUse)) {
         this.picker.sendSkinMsg(sender, "skin-no-permission");
         return true;
      } else if (!(sender instanceof Player player)) {
         if (args.length < 2) {
            this.picker.sendSkinMsg(sender, "skin-console-usage");
            return true;
         } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
               sender.sendMessage("Player not found: " + args[0]);
               return true;
            } else {
               this.handleSkinChange(sender, target, args[1], skins, true);
               return true;
            }
         }
      } else if (args.length == 0) {
         this.picker.sendSkinMsg(player, "skin-usage");
         return true;
       } else {
          String sub = args[0];
          if (sub.equalsIgnoreCase("reset")) {
            if (args.length >= 2) {
               if (!player.hasPermission(permOthers)) {
                  this.picker.sendSkinMsg(player, "skin-no-permission-others");
                  return true;
               } else {
                  Player target = Bukkit.getPlayer(args[1]);
                  if (target == null) {
                     player.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                     return true;
                  } else {
                     this.resetTarget(sender, target, skins);
                     return true;
                  }
               }
            } else if (!skins.isAllowReset()) {
               this.picker.sendSkinMsg(player, "skin-reset-disabled");
               return true;
            } else {
               long remaining = skins.getRemainingCooldown(player);
               if (remaining > 0L) {
                  this.picker.sendSkinMsg(player, "skin-cooldown", Collections.singletonMap("seconds", String.valueOf(remaining)));
                  return true;
               } else {
                  skins.resetSkin(player);
                  skins.clearSkinSelection(player.getUniqueId());
                  skins.markUsed(player);
                  this.picker.sendSkinMsg(player, "skin-reset");
                  return true;
               }
            }
         } else if (sub.equalsIgnoreCase("random")) {
            if (!skins.isAllowRandom()) {
               this.picker.sendSkinMsg(player, "skin-random-disabled");
               return true;
            } else {
               Player target = player;
               if (args.length >= 2) {
                  if (!player.hasPermission(permOthers)) {
                     this.picker.sendSkinMsg(player, "skin-no-permission-others");
                     return true;
                  }

                  target = Bukkit.getPlayer(args[1]);
                  if (target == null) {
                     player.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                     return true;
                  }
               }

               if (target.getUniqueId().equals(player.getUniqueId())) {
                  long remaining = skins.getRemainingCooldown(player);
                  if (remaining > 0L) {
                     this.picker.sendSkinMsg(player, "skin-cooldown", Collections.singletonMap("seconds", String.valueOf(remaining)));
                     return true;
                  }
               }

               List<String> pool = skins.getRandomPool();
               String picked = pool.get(this.random.nextInt(pool.size()));
               this.handleSkinChange(sender, target, picked, skins, false);
               return true;
            }
         } else if (args.length >= 2) {
            if (!player.hasPermission(permOthers)) {
               this.picker.sendSkinMsg(player, "skin-no-permission-others");
               return true;
            } else {
               Player targetx = Bukkit.getPlayer(args[0]);
               if (targetx == null) {
                  this.handleSkinChange(sender, player, args[0].toLowerCase(Locale.ROOT), skins, false);
                  return true;
               } else {
                  this.handleSkinChange(sender, targetx, args[1], skins, false);
                  return true;
               }
            }
         } else {
            this.handleSkinChange(sender, player, sub.toLowerCase(Locale.ROOT), skins, false);
            return true;
         }
      }
   }

   private void handleSkinChange(
      final CommandSender sender, final Player target, final String skinName, final AppearanceManager skins, final boolean skipCooldown
   ) {
      final boolean isSelf = sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId());
      if (isSelf && !skipCooldown && sender instanceof Player px) {
         long remaining = skins.getRemainingCooldown(px);
         if (remaining > 0L) {
            this.picker.sendSkinMsg(px, "skin-cooldown", Collections.singletonMap("seconds", String.valueOf(remaining)));
            return;
         }
      }

      this.picker.sendSkinMsg(sender, "skin-fetching", Collections.singletonMap("skin", skinName));
      skins.fetchSkinAsync(
         skinName.toLowerCase(Locale.ROOT),
         new AppearanceManager.SkinCallback() {
            @Override
            public void onSuccess(String value, String signature) {
               Bukkit.getScheduler().runTask(SkinCommandHandler.this.picker, () -> {
                   Player online = Bukkit.getPlayer(target.getUniqueId());
                   if (online != null && online.isOnline()) {
                      skins.applyTextures(online, value, signature);
                      skins.saveSkinSelection(online.getUniqueId(), skinName.toLowerCase(Locale.ROOT));
                      if (isSelf && !skipCooldown && sender instanceof Player self) {
                        skins.markUsed(self);
                     }

                     if (isSelf) {
                        SkinCommandHandler.this.picker.sendSkinMsg(sender, "skin-changed", Collections.singletonMap("skin", skinName));
                     } else {
                        Map<String, String> ph = Map.of("target", online.getName(), "skin", skinName);
                        SkinCommandHandler.this.picker.sendSkinMsg(sender, "skin-changed-other", ph);
                        SkinCommandHandler.this.picker.sendSkinMsg(online, "skin-changed-by-other", Map.of("skin", skinName, "setter", sender.getName()));
                     }
                  } else {
                     sender.sendMessage(ChatColor.RED + "Player went offline.");
                  }
               });
            }

            @Override
            public void onFailure() {
               Bukkit.getScheduler()
                  .runTask(
                     SkinCommandHandler.this.picker,
                     () -> SkinCommandHandler.this.picker.sendSkinMsg(sender, "skin-not-found", Collections.singletonMap("skin", skinName))
                  );
            }
         }
      );
   }

   private void resetTarget(CommandSender sender, Player target, AppearanceManager skins) {
      skins.resetSkin(target);
      skins.clearSkinSelection(target.getUniqueId());
      boolean isSelf = sender instanceof Player p && p.getUniqueId().equals(target.getUniqueId());
      if (isSelf) {
         this.picker.sendSkinMsg(sender, "skin-reset");
      } else {
         this.picker.sendSkinMsg(sender, "skin-reset-other", Collections.singletonMap("target", target.getName()));
         this.picker.sendSkinMsg(target, "skin-reset");
      }
   }
}
