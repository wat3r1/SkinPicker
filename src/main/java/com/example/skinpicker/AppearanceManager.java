package com.example.skinpicker;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
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
   private final SkinPickerPlugin picker;
   private final Map<String, CachedSkin> textureCache = new ConcurrentHashMap<>();
   private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private final Map<UUID, String[]> originalTextures = new ConcurrentHashMap<>();
   private File dataFile;
   private YamlConfiguration data;

   public AppearanceManager(SkinPickerPlugin picker) {
      this.picker = picker;
      this.loadDataFile();
   }

   public int getCooldownSeconds() {
      return Math.max(0, this.picker.getConfig().getInt("skins.cooldown-seconds", 60));
   }

   public long getCacheMillis() {
      int minutes = Math.max(1, this.picker.getConfig().getInt("skins.cache-minutes", 60));
      return minutes * 60000L;
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
      List<String> pool = this.picker.getConfig().getStringList("skins.random-pool");
      return pool.isEmpty() ? List.of("Notch", "jeb_") : pool;
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

    public long getRemainingCooldown(Player player) {
      if (player.hasPermission(this.getPermBypassCooldown())) {
         return 0L;
      } else {
         Long last = this.cooldowns.get(player.getUniqueId());
         if (last == null) {
            return 0L;
         } else {
            long elapsed = (System.currentTimeMillis() - last) / 1000L;
            long remaining = this.getCooldownSeconds() - elapsed;
            return Math.max(0L, remaining);
         }
      }
   }

   public void markUsed(Player player) {
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
   }

   public boolean usedWithin(Player player, long millis) {
      Long last = this.cooldowns.get(player.getUniqueId());
      return last != null && System.currentTimeMillis() - last <= millis;
   }

   public void clearCooldown(UUID uuid) {
       this.cooldowns.remove(uuid);
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

   public void saveSkinSelection(UUID uuid, String skinName) {
      if (this.isPersistAcrossJoins()) {
         Bukkit.getScheduler().runTaskAsynchronously(this.picker, () -> {
            synchronized (this) {
               this.data.set("skins." + uuid, skinName);
               this.saveDataFile();
            }
         });
      }
   }

   public void clearSkinSelection(UUID uuid) {
      Bukkit.getScheduler().runTaskAsynchronously(this.picker, () -> {
         synchronized (this) {
            this.data.set("skins." + uuid, null);
            this.saveDataFile();
         }
      });
   }

   public String getSavedSkin(UUID uuid) {
      return this.data == null ? null : this.data.getString("skins." + uuid, null);
   }

   public void reapplySavedSkin(Player player) {
      if (this.isPersistAcrossJoins()) {
         String saved = this.getSavedSkin(player.getUniqueId());
         if (saved != null && !saved.isEmpty()) {
            Bukkit.getScheduler()
               .runTaskLaterAsynchronously(this.picker, () -> this.fetchSkinAsync(saved.toLowerCase(Locale.ROOT), new AppearanceManager.SkinCallback() {
                  @Override
                  public void onSuccess(String value, String signature) {
                     Bukkit.getScheduler().runTask(AppearanceManager.this.picker, () -> {
                        Player online = Bukkit.getPlayer(player.getUniqueId());
                        if (online != null && online.isOnline()) {
                           AppearanceManager.this.applyTextures(online, value, signature);
                        }
                     });
                  }

                  @Override
                  public void onFailure() {
                  }
               }), 20L);
         }
      }
   }

   public void fetchSkinAsync(String name, AppearanceManager.SkinCallback callback) {
      String key = name.toLowerCase(Locale.ROOT);
      AppearanceManager.CachedSkin cached = this.textureCache.get(key);
      if (cached != null && !cached.expired()) {
         callback.onSuccess(cached.value, cached.signature);
      } else {
         Bukkit.getScheduler().runTaskAsynchronously(this.picker, () -> {
            try {
               String uuid = this.fetchMojangUuid(key);
               if (uuid == null) {
                  callback.onFailure();
                  return;
               }

               String[] props = this.fetchSessionProperties(uuid);
               if (props == null) {
                  callback.onFailure();
                  return;
               }

               this.textureCache.put(key, new AppearanceManager.CachedSkin(props[0], props[1], System.currentTimeMillis() + this.getCacheMillis()));
               callback.onSuccess(props[0], props[1]);
            } catch (Exception var5) {
               callback.onFailure();
            }
         });
      }
   }

   private String fetchMojangUuid(String name) throws Exception {
      URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setConnectTimeout(8000);
      conn.setReadTimeout(8000);
      conn.setRequestMethod("GET");
      conn.setRequestProperty("User-Agent", "SkinPicker/1.0");
      int code = conn.getResponseCode();
      if (code == 204 || code == 404) {
         return null;
      } else if (code != 200) {
         return null;
      } else {
         String var13;
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
               sb.append(line);
            }

            String json = sb.toString();
            int idIndex = json.indexOf("\"id\"");
            if (idIndex < 0) {
               return null;
            }

            int colon = json.indexOf(58, idIndex);
            int firstQuote = json.indexOf(34, colon);
            int secondQuote = json.indexOf(34, firstQuote + 1);
            if (firstQuote < 0 || secondQuote < 0) {
               return null;
            }

            var13 = json.substring(firstQuote + 1, secondQuote);
         }

         return var13;
      }
   }

   private String[] fetchSessionProperties(String mojangId) throws Exception {
      URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + mojangId + "?unsigned=false");
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setConnectTimeout(8000);
      conn.setReadTimeout(8000);
      conn.setRequestMethod("GET");
      conn.setRequestProperty("User-Agent", "SkinPicker/1.0");
      if (conn.getResponseCode() != 200) {
         return null;
      } else {
         String[] var22;
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
               sb.append(line);
            }

            String json = sb.toString();
            int propIndex = json.indexOf("\"value\"");
            if (propIndex < 0) {
               return null;
            }

            int c1 = json.indexOf(58, propIndex);
            int q1 = json.indexOf(34, c1);
            int q2 = json.indexOf(34, q1 + 1);
            if (q1 < 0 || q2 < 0) {
               return null;
            }

            String value = json.substring(q1 + 1, q2);
            String signature = null;
            int sigIndex = json.indexOf("\"signature\"");
            if (sigIndex >= 0) {
               int c2 = json.indexOf(58, sigIndex);
               int s1 = json.indexOf(34, c2);
               int s2 = json.indexOf(34, s1 + 1);
               if (s1 >= 0 && s2 >= 0) {
                  signature = json.substring(s1 + 1, s2);
               }
            }

            if (value.isEmpty()) {
               return null;
            }

            var22 = new String[]{value, signature};
         }

         return var22;
      }
   }

   private void ensureSnapshot(Player player) {
      if (!this.originalTextures.containsKey(player.getUniqueId())) {
         try {
            if (this.data != null) {
               String savedValue = this.data.getString("original." + player.getUniqueId() + ".value", null);
               if (savedValue != null && !savedValue.isEmpty()) {
                  String savedSig = this.data.getString("original." + player.getUniqueId() + ".signature", null);
                  this.originalTextures.put(player.getUniqueId(), new String[]{savedValue, savedSig});
                  return;
               }
            }
         } catch (Exception var6) {
         }

         try {
            PlayerProfile current = player.getPlayerProfile();

            for (ProfileProperty property : current.getProperties()) {
               if (property != null && "textures".equalsIgnoreCase(property.getName()) && property.getValue() != null && !property.getValue().isEmpty()) {
                  String[] snapshot = new String[]{property.getValue(), property.getSignature()};
                  this.originalTextures.put(player.getUniqueId(), snapshot);
                  this.persistSnapshot(player.getUniqueId(), snapshot);
                  break;
               }
            }
         } catch (Exception var7) {
         }
      }
   }

   private void persistSnapshot(UUID uuid, String[] snapshot) {
      Bukkit.getScheduler().runTaskAsynchronously(this.picker, () -> {
         synchronized (this) {
            try {
               if (this.data != null) {
                  this.data.set("original." + uuid + ".value", snapshot[0]);
                  this.data.set("original." + uuid + ".signature", snapshot[1]);
                  this.saveDataFile();
               }
            } catch (Exception var6) {
            }
         }
      });
   }

   public void applyTextures(Player player, String value, String signature) {
      try {
         this.ensureSnapshot(player);
         PlayerProfile profile = player.getPlayerProfile();

         try {
            if (profile.hasProperty("textures")) {
               profile.removeProperty("textures");
            }
         } catch (Exception var6) {
         }

         profile.setProperty(new ProfileProperty("textures", value, signature));
         player.setPlayerProfile(profile);
         this.refreshPlayer(player);
      } catch (Exception var7) {
         this.picker.getLogger().warning("Failed to apply skin for " + player.getName() + ": " + var7.getMessage());
      }
   }

   public void resetSkin(final Player player) {
      try {
         String[] snapshot = this.originalTextures.remove(player.getUniqueId());
         if (snapshot != null) {
            try {
               PlayerProfile profile = player.getPlayerProfile();

               try {
                  if (profile.hasProperty("textures")) {
                     profile.removeProperty("textures");
                  }
               } catch (Exception var7) {
               }

               profile.setProperty(new ProfileProperty("textures", snapshot[0], snapshot[1]));
               player.setPlayerProfile(profile);
               this.refreshPlayer(player);
               return;
            } catch (Exception var8) {
            }
         }

         try {
            PlayerProfile current = player.getPlayerProfile();

            try {
               if (current.hasProperty("textures")) {
                  current.removeProperty("textures");
               }
            } catch (Exception var5) {
            }

            player.setPlayerProfile(current);
            this.refreshPlayer(player);
         } catch (Exception var6) {
         }

         this.fetchSkinAsync(player.getName(), new AppearanceManager.SkinCallback() {
            @Override
            public void onSuccess(String value, String signature) {
               Bukkit.getScheduler().runTask(AppearanceManager.this.picker, () -> AppearanceManager.this.applyTextures(player, value, signature));
            }

            @Override
            public void onFailure() {
            }
         });
      } catch (Exception var9) {
         this.picker.getLogger().warning("Failed to reset skin for " + player.getName() + ": " + var9.getMessage());
      }
   }

   private void refreshPlayer(Player player) {
      Bukkit.getScheduler().runTask(this.picker, () -> {
         try {
            for (Player other : Bukkit.getOnlinePlayers()) {
               if (!other.getUniqueId().equals(player.getUniqueId())) {
                  other.hidePlayer(this.picker, player);
               }
            }
         } catch (Exception var4) {
         }

         Bukkit.getScheduler().runTaskLater(this.picker, () -> {
            try {
               for (Player otherx : Bukkit.getOnlinePlayers()) {
                  if (!otherx.getUniqueId().equals(player.getUniqueId())) {
                     otherx.showPlayer(this.picker, player);
                  }
               }
            } catch (Exception var5) {
            }

            try {
               player.updateCommands();
            } catch (Exception var4x) {
            }
         }, 5L);
      });
   }

   public List<String> suggestNames(String prefix) {
      List<String> out = new ArrayList<>();
      out.add("reset");
      out.add("random");

      for (Player p : Bukkit.getOnlinePlayers()) {
         if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
            out.add(p.getName());
         }
      }

      if (prefix.isEmpty()) {
         return out;
      } else {
         List<String> filtered = new ArrayList<>();

         for (String s : out) {
            if (s.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
               filtered.add(s);
            }
         }

         Collections.sort(filtered);
         return filtered;
      }
   }

   private static final class CachedSkin {
      final String value;
      final String signature;
      final long expiresAt;

      CachedSkin(String value, String signature, long expiresAt) {
         this.value = value;
         this.signature = signature;
         this.expiresAt = expiresAt;
      }

      boolean expired() {
         return System.currentTimeMillis() > this.expiresAt;
      }
   }

   public interface SkinCallback {
      void onSuccess(String var1, String var2);

      void onFailure();
   }
}
