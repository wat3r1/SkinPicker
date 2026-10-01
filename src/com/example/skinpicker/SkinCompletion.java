package com.example.skinpicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class SkinCompletion implements TabCompleter {
   private final SkinPickerPlugin picker;

   public SkinCompletion(SkinPickerPlugin var1) {
      this.picker = var1;
   }

   public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
      String var5 = var2.getName().toLowerCase(Locale.ROOT);
      if (!var5.equals("skin") && !var5.equals("skins") && !var5.equals("skinpicker")) {
         return Collections.emptyList();
      } else {
         AppearanceManager var6 = this.picker.getAppearanceService();
         if (var1 instanceof Player var7 && !var7.hasPermission(var6.getPermUse()) && !this.picker.isTrusted(var7)) {
            return Collections.emptyList();
         } else {
            ArrayList var11 = new ArrayList();
            if (var4.length == 1) {
               ArrayList var12 = new ArrayList<>(var6.suggestNames(var4[0]));
               StringUtil.copyPartialMatches(var4[0], var12, var11);
               Collections.sort(var11);
               return var11;
            } else if (var4.length != 2) {
               return Collections.emptyList();
            } else {
               ArrayList var8 = new ArrayList();

               for (Player var10 : this.picker.getServer().getOnlinePlayers()) {
                  var8.add(var10.getName());
               }

               var8.addAll(var6.getRandomPool());
               var8.add("reset");
               var8.add("random");
               StringUtil.copyPartialMatches(var4[1], var8, var11);
               Collections.sort(var11);
               return var11;
            }
         }
      }
   }
}
