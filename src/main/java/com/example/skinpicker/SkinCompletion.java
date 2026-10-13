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

   public SkinCompletion(SkinPickerPlugin picker) {
      this.picker = picker;
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      String cmdName = command.getName().toLowerCase(Locale.ROOT);
      if (!cmdName.equals("skin") && !cmdName.equals("skins") && !cmdName.equals("shelp")) {
         return Collections.emptyList();
      } else {
         AppearanceManager looks = this.picker.getAppearanceService();
         if (sender instanceof Player player && !player.hasPermission(looks.getPermUse())) {
            return Collections.emptyList();
         } else {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
               List<String> candidates = new ArrayList<>(looks.suggestNames(args[0]));
               StringUtil.copyPartialMatches(args[0], candidates, completions);
               Collections.sort(completions);
               return completions;
            } else if (args.length != 2) {
               return Collections.emptyList();
            } else {
               List<String> names = new ArrayList<>();

               for (Player p : this.picker.getServer().getOnlinePlayers()) {
                  names.add(p.getName());
               }

               names.addAll(looks.getRandomPool());
               names.add("reset");
               names.add("random");
               StringUtil.copyPartialMatches(args[1], names, completions);
               Collections.sort(completions);
               return completions;
            }
         }
      }
   }
}
