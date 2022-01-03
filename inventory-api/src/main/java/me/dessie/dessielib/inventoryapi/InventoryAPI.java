package me.dessie.dessielib.inventoryapi;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryAPI implements Listener {

    private static boolean registered = false;
    private static JavaPlugin plugin;

    /**
     * Register the API to use your plugin.
     * @param yourPlugin Your plugin instance.
     */
    public static void register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("Cannot register InventoryAPI in " + yourPlugin.getName() + ". Already registered by " + getPlugin().getName());
        }

        plugin = yourPlugin;
        getPlugin().getServer().getPluginManager().registerEvents(new InventoryAPI(), getPlugin());
        registered = true;
    }

    @EventHandler
    private void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory currentInv = event.getInventory();

        if(InventoryBuilder.getBuilder(player) != null && InventoryBuilder.getBuilder(player).getInventory() == currentInv) {
            InventoryBuilder invBuilder = InventoryBuilder.getBuilder(player);
            Bukkit.getScheduler().runTaskLater(getPlugin(), invBuilder::updateBuilder, 1);
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(InventoryBuilder.getBuilder(player) == null) return;

        //Cancel if they have a Builder open but they didn't click in it
        if(InventoryBuilder.getBuilder(player).getInventory() != event.getClickedInventory()) {
            event.setCancelled(true);
            return;
        }

        if(event.getCurrentItem() == null) return;
        if(event.getCursor() != null && event.getCursor().getType() != Material.AIR && event.getCurrentItem().getType() == Material.AIR) return;

        InventoryBuilder invBuilder = InventoryBuilder.getBuilder(player);
        Bukkit.getScheduler().runTaskLater(getPlugin(), invBuilder::updateBuilder, 1);

        ItemBuilder clicked = invBuilder.getItem(event.getSlot());
        for (ItemBuilder item : invBuilder.getItems().values()) {
            if (item.isSimilar(clicked) && item.getSlot() == clicked.getSlot()) {
                switch (event.getClick()) {
                    case LEFT -> item.clickType = ClickType.LEFT;
                    case RIGHT -> item.clickType = ClickType.RIGHT;
                    case MIDDLE -> item.clickType = ClickType.MIDDLE;
                    case SHIFT_LEFT -> item.clickType = ClickType.SHIFT_LEFT;
                    case SHIFT_RIGHT -> item.clickType = ClickType.SHIFT_RIGHT;
                }

                if (event.getCursor() != null) {
                    item.heldItem = event.getCursor();
                }

                if (item.isCancel()) {
                    event.setCancelled(true);
                }
                item.executeClick(player, item);
                item.swap();
                return;
            }
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        Inventory currentInv = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (InventoryBuilder.getBuilder(player) != null && InventoryBuilder.getBuilder(player).getInventory() == currentInv) {
            InventoryBuilder invBuilder = InventoryBuilder.getBuilder(player);

            if (invBuilder.isPreventClose()) {
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> event.getPlayer().openInventory(invBuilder.getInventory()), 1);
            } else {
                invBuilder.executeClose(player, invBuilder);
                InventoryBuilder.getInventories().remove(player);
            }
        }
    }

    /**
     * @return If InventoryAPI has been registered.
     */
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * @return The plugin that registered the InventoryAPI.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }
}
