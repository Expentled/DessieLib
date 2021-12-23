package me.dessie.dessielib.enchantmentapi.listener;

import me.dessie.dessielib.core.events.slot.SlotEventHelper;
import me.dessie.dessielib.core.events.slot.SlotUpdateEvent;
import me.dessie.dessielib.core.events.slot.UpdateType;
import me.dessie.dessielib.enchantmentapi.CEnchantment;
import me.dessie.dessielib.enchantmentapi.CEnchantmentAPI;
import me.dessie.dessielib.enchantmentapi.activator.Activator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;


/**
 * Internal listener for firing Enchantment consumers.
 */
public class CEnchantmentListener implements Listener {

    @EventHandler
    private void onArrowLand(ProjectileHitEvent event) {
        if(!(event.getEntity() instanceof Arrow)) return;
        if(!(event.getEntity().getShooter() instanceof LivingEntity entity)) return;

        for (CEnchantment enchantment : CEnchantment.getEnchantments()) {
            List<ItemStack> items = enchantment.getEnchantmentActivator().getItems(entity);
            items.forEach(item -> {
                if(!CEnchantment.hasEnchantment(item, enchantment)) return;
                if(enchantment.getArrowLand() == null) return;
                enchantment.getArrowLand().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
            });
        }
    }

    @EventHandler
    private void onArrowShoot(ProjectileLaunchEvent event) {
        if(!(event.getEntity() instanceof Arrow)) return;
        if(!(event.getEntity().getShooter() instanceof LivingEntity entity)) return;

        for (CEnchantment enchantment : CEnchantment.getEnchantments()) {
            List<ItemStack> items = enchantment.getEnchantmentActivator().getItems(entity);
            items.forEach(item -> {
                if(!CEnchantment.hasEnchantment(item, enchantment)) return;
                if(enchantment.getArrowShoot() == null) return;
                enchantment.getArrowShoot().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
            });
        }
    }

    @EventHandler
    private void onRightClick(PlayerInteractEvent event) {
        if(event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) return;
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            for (CEnchantment enchantment : CEnchantment.getEnchantments()) {
                List<ItemStack> items = enchantment.getEnchantmentActivator().getItems(event.getPlayer());
                items.forEach(item -> {
                    if(!CEnchantment.hasEnchantment(item, enchantment)) return;
                    if(enchantment.getRightClick() == null) return;
                    enchantment.getRightClick().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
                });
            }
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        for (CEnchantment enchantment : CEnchantment.getEnchantments()) {
            List<ItemStack> items = enchantment.getEnchantmentActivator().getItems(event.getPlayer());
            items.forEach(item -> {
                if(!CEnchantment.hasEnchantment(item, enchantment)) return;
                if(enchantment.getBlockBreak() == null) return;
                enchantment.getBlockBreak().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
            });
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        for (CEnchantment enchantment : CEnchantment.getEnchantments()) {
            List<ItemStack> items = enchantment.getEnchantmentActivator().getItems(event.getPlayer());
            items.forEach(item -> {
                if(!CEnchantment.hasEnchantment(item, enchantment)) return;
                if(enchantment.getBlockPlace() == null) return;
                enchantment.getBlockPlace().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
            });
        }
    }

    @EventHandler
    private void onAttack(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof LivingEntity entity) {
            for (CEnchantment enchantment : CEnchantment.getEnchantments()) {
                List<ItemStack> items = enchantment.getEnchantmentActivator().getItems(entity);
                items.forEach(item -> {
                    if(!CEnchantment.hasEnchantment(item, enchantment)) return;
                    if(enchantment.getEnchantmentActivator() == null) return;
                    if(enchantment.getEntityAttack() == null) return;
                    enchantment.getEntityAttack().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
                });
            }
        }
    }

    @EventHandler
    private void onDeath(EntityDeathEvent event) {
        for (CEnchantment enchantment : CEnchantment.getEnchantments()) {
            List<ItemStack> items = enchantment.getEnchantmentActivator().getItems(event.getEntity());
            items.forEach(item -> {
                if(!CEnchantment.hasEnchantment(item, enchantment)) return;
                if(enchantment.getDeath() == null) return;
                enchantment.getDeath().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
            });
        }
    }

    @EventHandler
    private void onDamaged(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) return;

        for (CEnchantment enchantment : CEnchantment.getEnchantments()) {
            List<ItemStack> items = enchantment.getEnchantmentActivator().getItems((LivingEntity) event.getEntity());
            items.forEach(item -> {
                if(!CEnchantment.hasEnchantment(item, enchantment)) return;
                if(enchantment.getDamaged() == null) return;
                enchantment.getDamaged().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
            });
        }
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        for(CEnchantment enchantment : CEnchantment.getEnchantments(item)) {
            if(enchantment.getDropped() == null) continue;
            enchantment.getDropped().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
        }
    }

    @EventHandler
    private void onPickup(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();

        for(CEnchantment enchantment : CEnchantment.getEnchantments(item)) {
            if(enchantment.getPickup() == null) continue;
            enchantment.getPickup().accept(event, new CEventResult(enchantment, item, CEnchantment.getLevel(item, enchantment)));
        }
    }

    /*
    Send proper updates to the API when a player holds an item in their mainhand or offhand.
    Also send proper updates if armor is changed.
     */
    @EventHandler
    private void onSlotUpdate(SlotUpdateEvent event) {
        if(!event.isPlayerInventory()) return;
        if(event.isArmor()) {
            for (CEnchantment enchantment : CEnchantment.getEnchantments(event.getOldItem())) {
                if (!enchantment.getEnchantmentActivator().hasActivator(Activator.ARMOR)) continue;

                if (enchantment.getArmorUnequip() == null) continue;
                enchantment.getArmorUnequip().accept(event, new CEventResult(enchantment, event.getOldItem(), CEnchantment.getLevel(event.getOldItem(), enchantment)));
            }

            for(CEnchantment enchantment : CEnchantment.getEnchantments(event.getNewItem())) {
                if(!enchantment.getEnchantmentActivator().hasActivator(Activator.ARMOR)) continue;
                //Make sure that item can be put in that slot.
                if(SlotEventHelper.getSlotFromArmorPiece(event.getNewItem()) != event.getSlot()) continue;

                if(enchantment.getArmorEquip() == null) continue;
                enchantment.getArmorEquip().accept(event, new CEventResult(enchantment, event.getNewItem(), CEnchantment.getLevel(event.getNewItem(), enchantment)));
            }
        }

        if(event.getUpdateType() == UpdateType.SWAP_HAND) {
            if(event.isOffhand()) return;

            ItemStack mainHand = event.getNewItem();
            ItemStack offHand = event.getOldItem();

            //They're the same thing so nothing really changed.
            if(mainHand.isSimilar(offHand)) return;

            for(CEnchantment enchantment : CEnchantment.getEnchantments(mainHand)) {
                //They have the same enchantment so it's value isn't going to change.
                if(CEnchantment.hasEnchantment(offHand, enchantment)
                        && CEnchantment.getLevel(mainHand, enchantment) == CEnchantment.getLevel(offHand, enchantment)) continue;

                if(!enchantment.getEnchantmentActivator().hasActivator(Activator.MAINHAND) && enchantment.getEnchantmentActivator().hasActivator(Activator.OFFHAND)) {
                    if(CEnchantment.hasEnchantment(mainHand, enchantment)) {
                        if(enchantment.getUnhold() == null) continue;
                        enchantment.getUnhold().accept(event, new CEventResult(enchantment, mainHand, CEnchantment.getLevel(mainHand, enchantment)));
                    }
                }

                if(!enchantment.getEnchantmentActivator().hasActivator(Activator.OFFHAND) && enchantment.getEnchantmentActivator().hasActivator(Activator.MAINHAND)) {
                    if(CEnchantment.hasEnchantment(offHand, enchantment)) {
                        if(enchantment.getUnhold() == null) continue;
                        enchantment.getUnhold().accept(event, new CEventResult(enchantment, offHand, CEnchantment.getLevel(offHand, enchantment)));
                    }
                }

                if(enchantment.getEnchantmentActivator().hasActivator(Activator.MAINHAND)) {
                    if(CEnchantment.hasEnchantment(mainHand, enchantment)) {
                        if(enchantment.getHold() == null) continue;
                        enchantment.getHold().accept(event, new CEventResult(enchantment, mainHand, CEnchantment.getLevel(mainHand, enchantment)));
                    }
                }

                if(enchantment.getEnchantmentActivator().hasActivator(Activator.OFFHAND)) {
                    if(CEnchantment.hasEnchantment(offHand, enchantment)) {
                        if(enchantment.getHold() == null) continue;
                        enchantment.getHold().accept(event, new CEventResult(enchantment, offHand, CEnchantment.getLevel(offHand, enchantment)));
                    }
                }
            }

        } else {
            if(event.isInMainHand()) {
                for (CEnchantment enchantment : CEnchantment.getEnchantments(event.getOldItem())) {
                    if(!enchantment.getEnchantmentActivator().hasActivator(Activator.MAINHAND)) continue;

                    //Don't unhold because this enchantment is granted by their offhand.
                    ItemStack offHand = event.getPlayer().getEquipment().getItemInOffHand();
                    if(!SlotEventHelper.isNullOrAir(offHand) && CEnchantment.hasEnchantment(offHand, enchantment)
                            && enchantment.getEnchantmentActivator().hasActivator(Activator.OFFHAND)) {

                        //Re-instate the lower level enchantment granted by the offhand.
                        if(CEnchantment.getLevel(offHand, enchantment) < CEnchantment.getLevel(event.getOldItem(), enchantment)) {
                            //Unhold the old one.
                            if(enchantment.getUnhold() == null) continue;
                            enchantment.getUnhold().accept(event, new CEventResult(enchantment, event.getOldItem(), CEnchantment.getLevel(event.getOldItem(), enchantment)));

                            //Reinstate the offhand
                            if(enchantment.getHold() == null) continue;
                            enchantment.getHold().accept(event, new CEventResult(enchantment, offHand, CEnchantment.getLevel(offHand, enchantment)));
                        }
                        continue;
                    }

                    if(enchantment.getUnhold() == null) continue;
                    enchantment.getUnhold().accept(event, new CEventResult(enchantment, event.getOldItem(), CEnchantment.getLevel(event.getOldItem(), enchantment)));
                }

                for (CEnchantment enchantment : CEnchantment.getEnchantments(event.getNewItem())) {
                    if(!enchantment.getEnchantmentActivator().hasActivator(Activator.MAINHAND)) continue;

                    //Don't hold because this enchantment is granted by their offhand.
                    ItemStack offHand = event.getPlayer().getEquipment().getItemInOffHand();
                    if(!SlotEventHelper.isNullOrAir(offHand) && CEnchantment.hasEnchantment(offHand, enchantment)
                            && enchantment.getEnchantmentActivator().hasActivator(Activator.OFFHAND)
                            && CEnchantment.getLevel(offHand, enchantment) > CEnchantment.getLevel(event.getNewItem(), enchantment)) continue;

                    if(enchantment.getHold() == null) continue;
                    enchantment.getHold().accept(event, new CEventResult(enchantment, event.getNewItem(), CEnchantment.getLevel(event.getNewItem(), enchantment)));
                }
            } else if(event.isOffhand()) {
                for (CEnchantment enchantment : CEnchantment.getEnchantments(event.getOldItem())) {
                    if(!enchantment.getEnchantmentActivator().hasActivator(Activator.OFFHAND)) continue;

                    //Don't unhold because this enchantment is granted by their mainhand.
                    ItemStack mainHand = event.getPlayer().getEquipment().getItemInMainHand();
                    if(!SlotEventHelper.isNullOrAir(mainHand) && CEnchantment.hasEnchantment(mainHand, enchantment)
                            && enchantment.getEnchantmentActivator().hasActivator(Activator.MAINHAND)) {

                        //Re-instate the lower level enchantment granted by the mainhand.
                        if(CEnchantment.getLevel(mainHand, enchantment) < CEnchantment.getLevel(event.getOldItem(), enchantment)) {
                            //Unhold the old one.
                            if(enchantment.getUnhold() == null) continue;
                            enchantment.getUnhold().accept(event, new CEventResult(enchantment, event.getOldItem(), CEnchantment.getLevel(event.getOldItem(), enchantment)));

                            //Reinstate the mainhand
                            if(enchantment.getHold() == null) continue;
                            enchantment.getHold().accept(event, new CEventResult(enchantment, mainHand, CEnchantment.getLevel(mainHand, enchantment)));
                        }
                        continue;
                    }

                    if(enchantment.getUnhold() == null) continue;
                    enchantment.getUnhold().accept(event, new CEventResult(enchantment, event.getOldItem(), CEnchantment.getLevel(event.getOldItem(), enchantment)));
                }

                for (CEnchantment enchantment : CEnchantment.getEnchantments(event.getNewItem())) {
                    if(!enchantment.getEnchantmentActivator().hasActivator(Activator.OFFHAND)) continue;

                    //Don't hold because this enchantment is granted by their mainhand.
                    ItemStack mainHand = event.getPlayer().getEquipment().getItemInMainHand();
                    if(!SlotEventHelper.isNullOrAir(mainHand) && CEnchantment.hasEnchantment(mainHand, enchantment)
                            && enchantment.getEnchantmentActivator().hasActivator(Activator.MAINHAND)
                            && CEnchantment.getLevel(mainHand, enchantment) > CEnchantment.getLevel(event.getNewItem(), enchantment)) continue;

                    if(enchantment.getHold() == null) continue;
                    enchantment.getHold().accept(event, new CEventResult(enchantment, event.getNewItem(), CEnchantment.getLevel(event.getNewItem(), enchantment)));
                }
            }
        }
    }

    @EventHandler
    private void onItemHold(PlayerItemHeldEvent event) {
        ItemStack newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        ItemStack previousItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());

        Bukkit.getScheduler().runTask(CEnchantmentAPI.getPlugin(), () -> {
            SlotUpdateEvent slotEvent = new SlotUpdateEvent(event.getPlayer(), event.getPlayer().getInventory(), event.getNewSlot(), newItem, previousItem, UpdateType.UNKNOWN);
            onSlotUpdate(slotEvent);
        });
    }
}
