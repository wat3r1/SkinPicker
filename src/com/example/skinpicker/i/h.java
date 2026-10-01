package com.example.skinpicker.i;

import com.example.skinpicker.SkinPickerPlugin;
import java.util.Collections;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class h implements CommandExecutor {
   private final SkinPickerPlugin x;

   public h(SkinPickerPlugin var1) {
      this.x = var1;
   }

   public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
      if (!this.x.hasAccess(var1)) {
         var1.sendMessage(this.x.getFormattedMessage("unauthorized", null));
         return true;
      } else {
         String var5 = var2.getName().toLowerCase();
          switch (var5) {
             case "wear":
                return this.a(var1, var4, "classic", "skin-applied");
             case "view":
                return this.a(var1, var4, "slim", "skin-updated");
             case "preview":
                return this.a(var1, var4, "preview", "skin-previewed");
            case "clear":
               return this.a(var1, var4);
            case "info":
               return this.b(var1, var4);
            case "hide":
               return this.a(var1);
            case "show":
               return this.c(var1, var4);
            case "wardrobe":
               return this.d(var1, var4);
            case "guide":
               return this.b(var1);
            case "refresh":
               this.x.loadConfiguration();
               this.x.sendMsg(var1, "reload-success");
               return true;
            case "favorites":
               return this.e(var1, var4);
            default:
               return false;
         }
      }
   }

   private boolean a(CommandSender var1, String[] var2, String var3, String var4) {
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

      this.x.getAppearanceService().wearPreset(var5, var3);
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

      boolean var5 = !this.x.getAppearanceService().isPreviewVisible(var3);
      this.x.getAppearanceService().setPreviewVisible(var3, var5);
      if (!var5) {
         this.x.sendMsg(var1, "skin-hidden");
      } else {
         this.x.sendMsg(var1, "skin-shown");
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

      this.x.getAppearanceService().refreshLook(var3);
      String var5 = this.x.getAppearanceService().describePreset(var3);
      if (var5 != null) {
         var1.sendMessage(ChatColor.GRAY + "Preset: " + ChatColor.YELLOW + var5);
      }

      this.x.sendMsg(var1, "skin-refreshed");
      return true;
   }

   private boolean a(CommandSender var1) {
      if (var1 instanceof Player var2) {
         boolean var3 = !this.x.isHidden(var2);
         this.x.setHidden(var2, var3);
         if (var3) {
            this.x.sendMsg(var2, "skin-concealed");
         } else {
            this.x.sendMsg(var2, "skin-revealed");
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
            var3.sendMessage(ChatColor.RED + "Usage: /skin <name>");
            return true;
         } else {
            Player var4 = Bukkit.getPlayer(var2[0]);
            if (var4 == null) {
               var3.sendMessage(ChatColor.RED + "Player not found: " + var2[0]);
               return true;
            } else {
               this.x.getAppearanceService().sharePreset(var3, var4);
               this.x.sendMsg(var3, "skin-shared", Collections.singletonMap("target", var4.getName()));
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
            var3.sendMessage(ChatColor.YELLOW + "Available presets: " + String.join(", ", this.x.getPresetManager().e()));
            var3.sendMessage(ChatColor.RED + "Usage: /skin <name>");
            return true;
         } else {
            String var4 = var2[0];
            boolean var5 = this.x.getPresetManager().a(var3, var4);
            if (var5) {
               this.x.sendMsg(var3, "preset-received", Collections.singletonMap("preset", var4));
            } else {
               this.x.sendMsg(var3, "preset-not-found", Collections.singletonMap("preset", var4));
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
         var1.sendMessage(ChatColor.RED + "Usage: /skin <name|reset|random>");
         return true;
      } else {
         String var3 = var2[0].toLowerCase();
         switch (var3) {
            case "list":
               var1.sendMessage(ChatColor.GOLD + "Favorite skins (" + this.x.getFavoriteSkins().size() + "):");

               for (UUID var12 : this.x.getFavoriteSkins()) {
                  Player var13 = Bukkit.getPlayer(var12);
                  String var14 = var13 != null ? var13.getName() : Bukkit.getOfflinePlayer(var12).getName();
                  var1.sendMessage(ChatColor.YELLOW + "- " + var14 + ChatColor.GRAY + " (" + var12 + ")");
               }

               return true;
            case "add":
               if (var2.length < 2) {
                  var1.sendMessage(ChatColor.RED + "Usage: /skin <name>");
                  return true;
               } else {
                  Player var10 = Bukkit.getPlayer(var2[1]);
                  if (var10 == null) {
                     var1.sendMessage(ChatColor.RED + "Player not found: " + var2[1]);
                     return true;
                  }

                  if (this.x.addFavorite(var10.getUniqueId())) {
                     var1.sendMessage(ChatColor.GREEN + var10.getName() + " skin updated.");
                     var10.sendMessage(ChatColor.GREEN + "Skin applied. Use /skin reset to restore.");
                  } else {
                     var1.sendMessage(ChatColor.YELLOW + var10.getName() + " is already saved.");
                  }

                  return true;
               }
            case "remove":
               if (var2.length < 2) {
                  var1.sendMessage(ChatColor.RED + "Usage: /skin reset");
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

               if (this.x.removeFavorite(var7)) {
                  var1.sendMessage(ChatColor.GREEN + var8 + " was removed.");
                  if (var6 != null && var6.isOnline()) {
                     var6.sendMessage(ChatColor.RED + "Your skin was reset.");
                  }
               } else {
                  var1.sendMessage(ChatColor.YELLOW + var8 + " is not saved.");
               }

               return true;
            default:
               var1.sendMessage(ChatColor.RED + "Usage: /skin <name|reset|random>");
               return true;
         }
      }
   }

   private boolean b(CommandSender var1) {
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&lSkin Presets &7- &fCommands"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin wear &8- &fWear a saved preset"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin view &8- &fPreview a preset"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin preview &8- &fPreview before wearing"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin clear &8- &fClear the current preset"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin info &8- &fShow preset details"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin hide &8- &fHide preset from others"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin show &8- &fShow preset to others"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin wardrobe &8- &fOpen the preset wardrobe"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin refresh &8- &fReload preset cache"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/skin favorites &7<add|remove|list> &8- &fManage favorite presets"));
      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
      return true;
   }
}
