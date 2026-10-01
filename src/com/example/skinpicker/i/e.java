package com.example.skinpicker.i;

import com.example.skinpicker.SkinPickerPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class e {
   private final SkinPickerPlugin h;
   private final Map<String, List<ItemStack>> i = new HashMap<>();

   public e(SkinPickerPlugin var1) {
      this.h = var1;
      this.d();
   }

   public void c() {
      this.d();
   }

   public void d() {
      this.i.clear();
      ConfigurationSection var1 = this.h.getConfig().getConfigurationSection("kits");
      if (var1 != null) {
         for (String var3 : var1.getKeys(false)) {
            ConfigurationSection var4 = var1.getConfigurationSection(var3);
            if (var4 != null) {
               List var5 = var4.getMapList("items");
               ArrayList var6 = new ArrayList();

               for (Map var8 : var5) {
                  try {
                     String var9 = (String)var8.get("material");
                     if (var9 != null) {
                        Material var10 = Material.matchMaterial(var9);
                        if (var10 == null) {
                           this.h.getLogger().warning("Unknown material: " + var9 + " in kit " + var3);
                        } else {
                           int var11 = 1;
                           if (var8.containsKey("amount")) {
                              Object var12 = var8.get("amount");
                              if (var12 instanceof Number) {
                                 var11 = ((Number)var12).intValue();
                              }
                           }

                           ItemStack var22 = new ItemStack(var10, Math.max(1, var11));
                           ItemMeta var13 = var22.getItemMeta();
                           if (var13 != null) {
                              if (var8.containsKey("name")) {
                                 String var14 = ChatColor.translateAlternateColorCodes('&', (String)var8.get("name"));
                                 var13.setDisplayName(var14);
                              }

                              if (var8.containsKey("enchantments") && var8.get("enchantments") instanceof Map var15) {
                                 for (Entry var17 : var15.entrySet()) {
                                    String var18 = var17.getKey().toString().toLowerCase(Locale.ROOT);
                                    int var19 = 1;
                                    if (var17.getValue() instanceof Number) {
                                       var19 = ((Number)var17.getValue()).intValue();
                                    }

                                    Enchantment var20 = this.a(var18);
                                    if (var20 != null) {
                                       var13.addEnchant(var20, var19, true);
                                    } else {
                                       this.h.getLogger().warning("Unknown enchantment: " + var18 + " in kit " + var3);
                                    }
                                 }
                              }

                              var22.setItemMeta(var13);
                           }

                           var6.add(var22);
                        }
                     }
                  } catch (Exception var21) {
                     this.h.getLogger().warning("Failed to parse item in kit " + var3 + ": " + var21.getMessage());
                  }
               }

               this.i.put(var3.toLowerCase(Locale.ROOT), var6);
            }
         }
      }
   }

   private Enchantment a(String var1) {
      Enchantment var2 = Enchantment.getByKey(NamespacedKey.minecraft(var1.replace("minecraft:", "")));
      return var2 != null ? var2 : Enchantment.getByName(var1.toUpperCase(Locale.ROOT));
   }

   public boolean a(Player var1, String var2) {
      String var3 = var2.toLowerCase(Locale.ROOT);
      List var4 = this.i.get(var3);
      if (var4 == null) {
         return false;
      } else {
         for (ItemStack var6 : var4) {
            HashMap var7 = var1.getInventory().addItem(new ItemStack[]{var6.clone()});
            if (!var7.isEmpty()) {
               for (ItemStack var9 : var7.values()) {
                  var1.getWorld().dropItemNaturally(var1.getLocation(), var9);
               }
            }
         }

         return true;
      }
   }

   public Set<String> e() {
      return Collections.unmodifiableSet(this.i.keySet());
   }
}
