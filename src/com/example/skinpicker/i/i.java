package com.example.skinpicker.i;

import com.example.skinpicker.SkinPickerPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class i implements TabCompleter {
   private final SkinPickerPlugin y;

   public i(SkinPickerPlugin var1) {
      this.y = var1;
   }

   public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
      if (!this.y.hasAccess(var1)) {
         return Collections.emptyList();
      } else {
         String var5 = var2.getName().toLowerCase(Locale.ROOT);
         ArrayList var6 = new ArrayList();
         if (var5.equals("wardrobe")) {
            if (var4.length == 1) {
               StringUtil.copyPartialMatches(var4[0], this.y.getPresetManager().e(), var6);
               Collections.sort(var6);
               return var6;
            }
         } else if (var5.equals("favorites")) {
            if (var4.length == 1) {
               StringUtil.copyPartialMatches(var4[0], Arrays.asList("add", "remove", "list"), var6);
               Collections.sort(var6);
               return var6;
            }

            if (var4.length == 2 && (var4[0].equalsIgnoreCase("add") || var4[0].equalsIgnoreCase("remove"))) {
               ArrayList var10 = new ArrayList();

               for (Player var12 : Bukkit.getOnlinePlayers()) {
                  var10.add(var12.getName());
               }

               StringUtil.copyPartialMatches(var4[1], var10, var6);
               Collections.sort(var6);
               return var6;
            }
         } else if ((var5.equals("wear") || var5.equals("view") || var5.equals("preview") || var5.equals("clear") || var5.equals("info") || var5.equals("show"))
            && var4.length == 1) {
            ArrayList var7 = new ArrayList();

            for (Player var9 : Bukkit.getOnlinePlayers()) {
               var7.add(var9.getName());
            }

            StringUtil.copyPartialMatches(var4[0], var7, var6);
            Collections.sort(var6);
            return var6;
         }

         return Collections.emptyList();
      }
   }
}
