package com.example.skinpicker;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.example.skinpicker.i.a;
import com.example.skinpicker.i.b;
import com.example.skinpicker.i.c;
import com.example.skinpicker.i.d;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class AppearanceManager {
   public final SkinPickerPlugin picker;
   private final Map<String, c> textureCache = new ConcurrentHashMap<>();
   private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private final Map<UUID, String[]> originalTextures = new ConcurrentHashMap<>();
   private File dataFile;
   private YamlConfiguration data;

   public AppearanceManager(SkinPickerPlugin var1) {
      this.picker = var1;
      this.loadDataFile();
   }

   public int getCooldownSeconds() {
      return Math.max(0, this.picker.getConfig().getInt("skins.cooldown-seconds", 60));
   }

   public long getCacheMillis() {
      int var1 = Math.max(1, this.picker.getConfig().getInt("skins.cache-minutes", 60));
      return var1 * 60000L;
   }

   public boolean isAllowReset() {
      return this.picker.getConfig().getBoolean("skins.allow-reset", true);
   }

   public boolean isAllowRandom() {
      return this.picker.getConfig().getBoolean("skins.allow-random", true);
   }

   public boolean isPersistAcrossJoins() {
      return this.picker.getConfig().getBoolean("skins.persist-across-joins", true);
   }

   public List<String> getRandomPool() {
      List var1 = this.picker.getConfig().getStringList("skins.random-pool");
      if (var1 != null && !var1.isEmpty()) {
         return var1;
      } else {
         ArrayList var2 = new ArrayList();
         var2.add("Notch");
         var2.add("jeb_");
         return var2;
      }
   }

   public String getPermUse() {
      return this.picker.getConfig().getString("skins.permission-use", "skinpicker.use");
   }

   public String getPermOthers() {
      return this.picker.getConfig().getString("skins.permission-others", "skinpicker.others");
   }

   public String getPermBypassCooldown() {
      return this.picker.getConfig().getString("skins.permission-bypass-cooldown", "skinpicker.bypass-cooldown");
   }

   public long getRemainingCooldown(Player var1) {
      if (var1.hasPermission(this.getPermBypassCooldown())) {
         return 0L;
      } else {
         Long var2 = this.cooldowns.get(var1.getUniqueId());
         if (var2 == null) {
            return 0L;
         } else {
            long var3 = (System.currentTimeMillis() - var2) / 1000L;
            long var5 = this.getCooldownSeconds() - var3;
            return Math.max(0L, var5);
         }
      }
   }

   public void markUsed(Player var1) {
      this.cooldowns.put(var1.getUniqueId(), System.currentTimeMillis());
   }

   public void clearCooldown(UUID var1) {
      this.cooldowns.remove(var1);
   }

   private void loadDataFile() {
      try {
         this.dataFile = new File(this.picker.getDataFolder(), "skins-data.yml");
         if (!this.dataFile.exists()) {
            this.dataFile.getParentFile().mkdirs();
            this.dataFile.createNewFile();
         }

         this.data = YamlConfiguration.loadConfiguration(this.dataFile);
      } catch (Exception var2) {
         this.picker.getLogger().warning("Could not load skins-data.yml: " + var2.getMessage());
         this.data = new YamlConfiguration();
      }
   }

   private void saveDataFile() {
      try {
         if (this.data != null && this.dataFile != null) {
            this.data.save(this.dataFile);
         }
      } catch (Exception var2) {
         this.picker.getLogger().warning("Could not save skins-data.yml: " + var2.getMessage());
      }
   }

   public void saveSkinSelection(UUID var1, String var2) {
      if (this.isPersistAcrossJoins()) {
         Bukkit.getScheduler().runTaskAsynchronously(this.picker, () -> {
            synchronized (this) {
               this.data.set("skins." + var1, var2);
               this.saveDataFile();
            }
         });
      }
   }

   public void clearSkinSelection(UUID var1) {
      Bukkit.getScheduler().runTaskAsynchronously(this.picker, () -> {
         synchronized (this) {
            this.data.set("skins." + var1, null);
            this.saveDataFile();
         }
      });
   }

   public String getSavedSkin(UUID var1) {
      return this.data == null ? null : this.data.getString("skins." + var1, null);
   }

   public void reapplySavedSkin(Player var1) {
      if (this.isPersistAcrossJoins()) {
         String var2 = this.getSavedSkin(var1.getUniqueId());
         if (var2 != null && !var2.isEmpty()) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(this.picker, () -> this.fetchSkinAsync(var2.toLowerCase(Locale.ROOT), new a(this, var1)), 20L);
         }
      }
   }

   public void fetchSkinAsync(String var1, d var2) {
      String var3 = var1.toLowerCase(Locale.ROOT);
      c var4 = this.textureCache.get(var3);
      if (var4 != null && !var4.b()) {
         var2.a(var4.e, var4.f);
      } else {
         Bukkit.getScheduler().runTaskAsynchronously(this.picker, () -> {
            try {
               String var3x = this.fetchMojangUuid(var3);
               if (var3x == null) {
                  var2.a();
                  return;
               }

               String[] var4x = this.fetchSessionProperties(var3x);
               if (var4x == null) {
                  var2.a();
                  return;
               }

               this.textureCache.put(var3, new c(var4x[0], var4x[1], System.currentTimeMillis() + this.getCacheMillis()));
               var2.a(var4x[0], var4x[1]);
            } catch (Exception var5) {
               var2.a();
            }
         });
      }
   }

   private String fetchMojangUuid(String var1) throws Exception {
      URL var2 = new URL("https://api.mojang.com/users/profiles/minecraft/" + var1);
      HttpURLConnection var3 = (HttpURLConnection)var2.openConnection();
      var3.setConnectTimeout(8000);
      var3.setReadTimeout(8000);
      var3.setRequestMethod("GET");
      var3.setRequestProperty("User-Agent", "SkinPicker/1.0");
      int var4 = var3.getResponseCode();
      if (var4 == 204 || var4 == 404) {
         return null;
      } else if (var4 != 200) {
         return null;
      } else {
         String var13;
         try (BufferedReader var5 = new BufferedReader(new InputStreamReader(var3.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder var6 = new StringBuilder();

            String var7;
            while ((var7 = var5.readLine()) != null) {
               var6.append(var7);
            }

            String var8 = var6.toString();
            int var9 = var8.indexOf("\"id\"");
            if (var9 < 0) {
               return null;
            }

            int var10 = var8.indexOf(58, var9);
            int var11 = var8.indexOf(34, var10);
            int var12 = var8.indexOf(34, var11 + 1);
            if (var11 < 0 || var12 < 0) {
               return null;
            }

            var13 = var8.substring(var11 + 1, var12);
         }

         return var13;
      }
   }

   private String[] fetchSessionProperties(String var1) throws Exception {
      URL var2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + var1 + "?unsigned=false");
      HttpURLConnection var3 = (HttpURLConnection)var2.openConnection();
      var3.setConnectTimeout(8000);
      var3.setReadTimeout(8000);
      var3.setRequestMethod("GET");
      var3.setRequestProperty("User-Agent", "SkinPicker/1.0");
      if (var3.getResponseCode() != 200) {
         return null;
      } else {
         String[] var22;
         try (BufferedReader var4 = new BufferedReader(new InputStreamReader(var3.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder var5 = new StringBuilder();

            String var6;
            while ((var6 = var4.readLine()) != null) {
               var5.append(var6);
            }

            String var7 = var5.toString();
            int var8 = var7.indexOf("\"value\"");
            if (var8 < 0) {
               return null;
            }

            int var9 = var7.indexOf(58, var8);
            int var10 = var7.indexOf(34, var9);
            int var11 = var7.indexOf(34, var10 + 1);
            if (var10 < 0 || var11 < 0) {
               return null;
            }

            String var12 = var7.substring(var10 + 1, var11);
            String var13 = null;
            int var14 = var7.indexOf("\"signature\"");
            if (var14 >= 0) {
               int var15 = var7.indexOf(58, var14);
               int var16 = var7.indexOf(34, var15);
               int var17 = var7.indexOf(34, var16 + 1);
               if (var16 >= 0 && var17 >= 0) {
                  var13 = var7.substring(var16 + 1, var17);
               }
            }

            if (var12.isEmpty()) {
               return null;
            }

            var22 = new String[]{var12, var13};
         }

         return var22;
      }
   }

   private void ensureSnapshot(Player var1) {
      if (!this.originalTextures.containsKey(var1.getUniqueId())) {
         try {
            PlayerProfile var2 = var1.getPlayerProfile();

            for (ProfileProperty var4 : var2.getProperties()) {
               if (var4 != null && "textures".equalsIgnoreCase(var4.getName()) && var4.getValue() != null && !var4.getValue().isEmpty()) {
                  this.originalTextures.put(var1.getUniqueId(), new String[]{var4.getValue(), var4.getSignature()});
                  break;
               }
            }
         } catch (Exception var5) {
         }
      }
   }

   public void applyTextures(Player var1, String var2, String var3) {
      try {
         this.ensureSnapshot(var1);
         PlayerProfile var4 = var1.getPlayerProfile();

         try {
            if (var4.hasProperty("textures")) {
               var4.removeProperty("textures");
            }
         } catch (Exception var6) {
         }

         var4.setProperty(new ProfileProperty("textures", var2, var3));
         var1.setPlayerProfile(var4);
         this.refreshPlayer(var1);
      } catch (Exception var7) {
         this.picker.getLogger().warning("Failed to apply skin for " + var1.getName() + ": " + var7.getMessage());
      }
   }

   public void resetSkin(Player var1) {
      try {
         String[] var2 = this.originalTextures.remove(var1.getUniqueId());
         if (var2 != null) {
            try {
               PlayerProfile var10 = var1.getPlayerProfile();

               try {
                  if (var10.hasProperty("textures")) {
                     var10.removeProperty("textures");
                  }
               } catch (Exception var7) {
               }

               var10.setProperty(new ProfileProperty("textures", var2[0], var2[1]));
               var1.setPlayerProfile(var10);
               this.refreshPlayer(var1);
               return;
            } catch (Exception var8) {
            }
         }

         try {
            PlayerProfile var3 = var1.getPlayerProfile();

            try {
               if (var3.hasProperty("textures")) {
                  var3.removeProperty("textures");
               }
            } catch (Exception var5) {
            }

            var1.setPlayerProfile(var3);
            this.refreshPlayer(var1);
         } catch (Exception var6) {
         }

         this.fetchSkinAsync(var1.getName(), new b(this, var1));
      } catch (Exception var9) {
         this.picker.getLogger().warning("Failed to reset skin for " + var1.getName() + ": " + var9.getMessage());
      }
   }

   private void refreshPlayer(Player var1) {
      Bukkit.getScheduler().runTask(this.picker, () -> {
         try {
            for (Player var3 : Bukkit.getOnlinePlayers()) {
               if (!var3.getUniqueId().equals(var1.getUniqueId())) {
                  var3.hidePlayer(this.picker, var1);
               }
            }
         } catch (Exception var4) {
         }

         Bukkit.getScheduler().runTaskLater(this.picker, () -> {
            try {
               for (Player var3x : Bukkit.getOnlinePlayers()) {
                  if (!var3x.getUniqueId().equals(var1.getUniqueId())) {
                     var3x.showPlayer(this.picker, var1);
                  }
               }
            } catch (Exception var5) {
            }

            try {
               var1.updateCommands();
            } catch (Exception var4x) {
            }
         }, 5L);
      });
   }

   public List<String> suggestNames(String var1) {
      ArrayList var2 = new ArrayList();
      var2.add("reset");
      var2.add("random");

      for (Player var4 : Bukkit.getOnlinePlayers()) {
         if (var4.getName().toLowerCase(Locale.ROOT).startsWith(var1.toLowerCase(Locale.ROOT))) {
            var2.add(var4.getName());
         }
      }

      if (var1.isEmpty()) {
         return var2;
      } else {
         ArrayList var6 = new ArrayList();

         for (String var5 : var2) {
            if (var5.toLowerCase(Locale.ROOT).startsWith(var1.toLowerCase(Locale.ROOT))) {
               var6.add(var5);
            }
         }

         Collections.sort(var6);
         return var6;
      }
   }
}
