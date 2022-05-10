package me.dessie.dessielib.enchantmentapi;

import me.dessie.dessielib.core.events.slot.SlotEventHelper;
import me.dessie.dessielib.core.events.slot.SlotUpdateEvent;
import me.dessie.dessielib.core.utils.Colors;
import me.dessie.dessielib.enchantmentapi.activator.Activator;
import me.dessie.dessielib.enchantmentapi.activator.EnchantmentActivator;
import me.dessie.dessielib.enchantmentapi.listener.CEventResult;
import me.dessie.dessielib.enchantmentapi.properties.CEnchantProperties;
import me.dessie.dessielib.enchantmentapi.utils.RomanNumeral;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Main class for creating a custom Enchantment, and handles everything related.
 */
public class CEnchantment extends Enchantment implements Listener {

    private static final List<CEnchantment> enchantments = new ArrayList<>();

    private boolean registered;

    private final String name;
    private String displayName;

    private int maxLevel = 1;
    private boolean treasure;
    private boolean cursed;
    private Predicate<ItemStack> canEnchantPredicate;
    private final List<Material> canEnchant = new ArrayList<>();
    private final List<Enchantment> conflicts = new ArrayList<>();
    private EnchantmentTarget target;
    private EnchantmentActivator activator = new EnchantmentActivator(Activator.HAND);
    private CEnchantProperties properties = new CEnchantProperties().setAsNormalEnchant();

    private boolean usesRomanNumerals = true;

    private BiConsumer<EntityDamageByEntityEvent, CEventResult> entityAttack;
    private BiConsumer<PlayerInteractEvent, CEventResult> rightClick;
    private BiConsumer<BlockBreakEvent, CEventResult> blockBreak;
    private BiConsumer<BlockPlaceEvent, CEventResult> blockPlace;

    private BiConsumer<ProjectileLaunchEvent, CEventResult> arrowShoot;
    private BiConsumer<ProjectileHitEvent, CEventResult> arrowLand;
    private BiConsumer<EntityDamageEvent, CEventResult> damaged;
    private BiConsumer<EntityPickupItemEvent, CEventResult> pickup;
    private BiConsumer<PlayerDropItemEvent, CEventResult> dropped;
    private BiConsumer<EntityDeathEvent, CEventResult> death;

    private BiConsumer<SlotUpdateEvent, CEventResult> hold;
    private BiConsumer<SlotUpdateEvent, CEventResult> unhold;

    private BiConsumer<SlotUpdateEvent, CEventResult> armorEquip;
    private BiConsumer<SlotUpdateEvent, CEventResult> armorUnequip;

    private Consumer<CEventResult> enchant;
    private Consumer<CEventResult> disenchant;

    /**
     * @param name The name of the enchantment. Must be unique to your plugin.
     *             Can be similar to other enchantments outside your namespace.
     *
     *             The name should not contain spaces or special characters.
     */
    public CEnchantment(String name) {
        super(new NamespacedKey(CEnchantmentAPI.getPlugin(), name));
        if(!CEnchantmentAPI.isRegistered()) {
            throw new IllegalStateException("You need to register CEnchantmentLoader before creating CEnchantments!");
        }

        this.name = name;
        this.registerEnchantment();
    }

    /**
     * @return The {@link CEnchantProperties} that are being applied to this Enchantment.
     */
    public CEnchantProperties getEnchantProperties() { return this.properties; }

    /**
     * @return How this Enchantment is displayed as Lore when it's placed on an ItemStack.
     */
    public String getDisplayName() { return this.displayName == null ? this.getName() : this.displayName;  }

    /**
     * @return The {@link EnchantmentActivator} that this Enchantment uses to fire it's consumers.
     */
    public EnchantmentActivator getEnchantmentActivator() { return activator; }

    /**
     * @return If the level is displayed as Roman Numerals
     */
    public boolean isUsesRomanNumerals() { return usesRomanNumerals; }

    /**
     * @return If the Enchantment is registered and ready for use.
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Adds an EventListener to this CEnchantment, that will callback your methods when the event is triggered and the predicate is met.
     * Generally, your predicate will check if the item has this enchantment.
     *
     * @param type The Event class
     * @param predicate The Predicate that should be met to fire your listener.
     * @param consumer A consumer that accepts the event
     * @param <T> The class type of the event to listen for.
     * @return This CEnchantment
     */
    public <T extends Event> CEnchantment addEventListener(Class<T> type, BiPredicate<CEnchantment, T> predicate, Consumer<T> consumer) {
        return this.addEventListener(type, predicate, consumer, EventPriority.NORMAL);
    }

    /**
     * Adds an EventListener to this CEnchantment, that will callback your methods when the event is triggered and the predicate is met.
     * Generally, your predicate will check if the item has this enchantment.
     *
     * @param type The Event class
     * @param predicate The Predicate that should be met to fire your listener.
     * @param consumer A consumer that accepts the event
     * @param priority The Event's priority
     * @param <T> The class type of the event to listen for.
     * @return This CEnchantment
     */
    public <T extends Event> CEnchantment addEventListener(Class<T> type, BiPredicate<CEnchantment, T> predicate, Consumer<T> consumer, EventPriority priority) {
        CEnchantmentAPI.getPlugin().getServer().getPluginManager().registerEvent(type, this, priority, (listener, event) -> {
            if(event.getClass() != type) return;

            if(predicate.test(this, (T) event)) {
                consumer.accept((T) event);
            }
        }, CEnchantmentAPI.getPlugin());
        return this;
    }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment attacks another entity.
     * @return The consumer
     */
    public BiConsumer<EntityDamageByEntityEvent, CEventResult> getEntityAttack() { return entityAttack; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment right clicks
     * @return The consumer
     */
    public BiConsumer<PlayerInteractEvent, CEventResult> getRightClick() { return rightClick; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment breaks a block
     * @return The consumer
     */
    public BiConsumer<BlockBreakEvent, CEventResult> getBlockBreak() { return blockBreak; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment places a block
     * @return The consumer
     */
    public BiConsumer<BlockPlaceEvent, CEventResult> getBlockPlace() { return blockPlace; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment shoots an arrow
     * @return The consumer
     */
    public BiConsumer<ProjectileLaunchEvent, CEventResult> getArrowShoot() { return arrowShoot; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment has an arrow that they shot land
     * @return The consumer
     */
    public BiConsumer<ProjectileHitEvent, CEventResult> getArrowLand() { return arrowLand; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment drops an item
     * @return The consumer
     */
    public BiConsumer<PlayerDropItemEvent, CEventResult> getDropped() { return dropped; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment picks up an item
     * @return The consumer
     */
    public BiConsumer<EntityPickupItemEvent, CEventResult> getPickup() { return pickup; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment is damaged
     * @return The consumer
     */
    public BiConsumer<EntityDamageEvent, CEventResult> getDamaged() { return damaged; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment dies
     * @return The consumer
     */
    public BiConsumer<EntityDeathEvent, CEventResult> getDeath() { return death; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment holds an item
     * @return The consumer
     */
    public BiConsumer<SlotUpdateEvent, CEventResult> getHold() { return hold; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment stops holding an item
     * @return The consumer
     */
    public BiConsumer<SlotUpdateEvent, CEventResult> getUnhold() { return unhold; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment equips an armor piece
     * @return The consumer
     */
    public BiConsumer<SlotUpdateEvent, CEventResult> getArmorEquip() { return armorEquip; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment removes an armor piece
     * @return The consumer
     */
    public BiConsumer<SlotUpdateEvent, CEventResult> getArmorUnequip() { return armorUnequip; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment enchants an item
     * @return The consumer
     */
    public Consumer<CEventResult> getEnchant() { return enchant; }

    /**
     * Returns the {@link BiConsumer} for when an entity with this enchantment disenchants an item
     * @return The consumer
     */
    public Consumer<CEventResult> getDisenchant() { return disenchant; }

    /**
     * Enchants an ItemStack with the Enchantment.
     * Obeys all conflicts and level caps. Use {@link CEnchantment#unsafeEnchant(ItemStack, int)} to bypass.
     *
     * Note: This method is required to add CEnchantments to an ItemStack. Do not use {@link ItemStack#addEnchantment(Enchantment, int)}!
     *
     * @param item The {@link ItemStack} that will be enchanted.
     * @param level The level to enchant the ItemStack with.
     */
    public void enchant(ItemStack item, int level) { enchant(item, level, false); }

    /**
     * Enchants an ItemStack with the Enchantment.
     *
     * Note: This method is required to add CEnchantments to an ItemStack. Do not use {@link ItemStack#addEnchantment(Enchantment, int)}!
     *
     * @param item The {@link ItemStack} that will be enchanted.
     * @param level The level to enchant the ItemStack with.
     * @param unsafe Whether to forcefully add the enchantment, or to obey conflicts and level cap.
     */
    private void enchant(ItemStack item, int level, boolean unsafe) { enchant(item, level, unsafe, true); }

    /**
     * Forcefully enchants an ItemStack with the Enchantment.
     * This method will ignore item and enchantment conflicts and enchantment level cap.
     *
     * Note: This method is required to add CEnchantments to an ItemStack. Do not use {@link ItemStack#addEnchantment(Enchantment, int)}!
     *
     * @param item The {@link ItemStack} that will be enchanted.
     * @param level The level to enchant the ItemStack with.
     */
    public void unsafeEnchant(ItemStack item, int level) { enchant(item, level, true); }

    /**
     * Enchants an ItemStack with the Enchantment.
     *
     * Note: This method is required to add CEnchantments to an ItemStack. Do not use {@link ItemStack#addEnchantment(Enchantment, int)}!
     *
     * @param item The {@link ItemStack} that will be enchanted.
     * @param level The level to enchant the ItemStack with.
     * @param unsafe Whether to forcefully add the enchantment, or to obey conflicts and level cap.
     * @param doEnchantEvent If the enchantment consumer should be accepted when enchanting.
     */
    public void enchant(ItemStack item, int level, boolean unsafe, boolean doEnchantEvent) {
        if(item == null || item.getItemMeta() == null) return;

        if(item.getItemMeta().hasEnchant(this)) {
            removeEnchantment(item, false);
        }

        if(item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK) {
            if(!this.getEnchantProperties().canBeOnBook()) return;

            item.setType(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(this, level, unsafe);
            item.setItemMeta(meta);
        } else {
            if(unsafe) {
                item.addUnsafeEnchantment(this, level);
            } else item.addEnchantment(this, level);
        }

        if(this.getEnchantProperties().isLoreDisplayed()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            lore.add(0, (this.isCursed() ? ChatColor.RED : ChatColor.GRAY) + this.getDisplayName() + (this.getMaxLevel() > 1 ? " " +
                    (this.isUsesRomanNumerals() ? RomanNumeral.fromInt(level) : level) : ""));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        if(doEnchantEvent && this.getEnchant() != null) {
            this.getEnchant().accept(new CEventResult(this, item, level));
        }
    }

    /**
     * Removes the Enchantment from the provided ItemStack. 
     *
     * Note: This method is required to remove CEnchantments from an ItemStack. Do not use {@link ItemStack#removeEnchantment(Enchantment)}!
     * 
     * @param item The ItemStack to remove the enchantment from.
     */
    public void removeEnchantment(ItemStack item) { removeEnchantment(item, true); }

    /**
     * Removes the Enchantment from the provided ItemStack.
     *
     * Note: This method is required to remove CEnchantments from an ItemStack. Do not use {@link ItemStack#removeEnchantment(Enchantment)}!
     *
     * @param item The ItemStack to remove the enchantment from.
     * @param doDisenchantEvent If the disenchant consumer should be accepted.
     */
    public void removeEnchantment(ItemStack item, boolean doDisenchantEvent) {
        if(item.getItemMeta() == null) return;
        int level = getLevel(item, this);

        if(item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.removeStoredEnchant(this);
            item.setItemMeta(meta);
        } else {
            item.removeEnchantment(this);
        }

        if(this.getEnchantProperties().isLoreDisplayed()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            for (String s : lore) {
                if (s.contains((this.isCursed() ? ChatColor.RED : ChatColor.GRAY) + this.getDisplayName())) {
                    lore.remove(s);
                    break;
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        if(doDisenchantEvent && this.getDisenchant() != null) {
            this.getDisenchant().accept(new CEventResult(this, item, level));
        }
    }

    /**
     * @param properties The {@link CEnchantProperties} to apply to this CEnchantment.
     * @return The CEnchantment instance.
     */
    public CEnchantment setEnchantProperties(CEnchantProperties properties) {
        if(properties == null) throw new IllegalArgumentException("Properties cannot be null");

        this.properties = properties;
        properties.setEnchantment(this);
        return this;
    }

    /**
     * Sets the name that is displayed on an ItemStack.
     * The display name can be colored and formatted, and may contain spaces and special characters.
     *
     * @param displayName The name to display
     * @return The CEnchantment instance.
     */
    public CEnchantment setDisplayName(String displayName) {
        this.displayName = Colors.color(displayName);
        return this;
    }

    /**
     * Sets the maximum enchantment level that this enchantment can be obtained naturally.
     * This can be bypassed using {@link CEnchantment#unsafeEnchant(ItemStack, int)}
     *
     * @param maxLevel The maximum level this enchantment can be.
     * @return The CEnchantment instance.
     */
    public CEnchantment setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    /**
     * @param usesRomanNumerals If the level should be displayed using Roman Numerals or Integers.
     * @return The CEnchantment instance.
     */
    public CEnchantment setUsesRomanNumerals(boolean usesRomanNumerals) {
        this.usesRomanNumerals = usesRomanNumerals;
        return this;
    }

    /**
     * Marking an Enchantment as a treasure enchantment will
     *   1. Make it unobtainable via the Enchanting Table
     *   2. Make it cost significantly more in villager trades.
     *
     * @param treasure Marks this enchantment as a treasure Enchantment
     * @return The CEnchantment instance.
     */
    public CEnchantment setTreasure(boolean treasure) {
        this.treasure = treasure;
        return this;
    }

    /**
     * Marking an Enchantment as a cursed enchantment will make the enchantment appear red.
     * Cursed enchantments can still be removed from the Grindstone if {@link CEnchantProperties#canRemoveWithGrindstone()} is true.
     *
     * @param cursed Marks this enchantment as a cursed Enchantment
     * @return The CEnchantment instance.
     */
    public CEnchantment setCursed(boolean cursed) {
        this.cursed = cursed;
        return this;
    }

    /**
     *
     * This can be bypassed using {@link CEnchantment#unsafeEnchant(ItemStack, int)}
     * Additional Materials can be added by using {@link CEnchantment#addEnchantables(Material...)}
     *
     * @param target Defines which ItemStacks this Enchantment can safely be applied to.
     *               If the material does not satisfy this target and does not satisfy
     * @return The CEnchantment instance.
     */
    public CEnchantment setEnchantmentTarget(EnchantmentTarget target) {
        this.target = target;
        return this;
    }

    /**
     * Set the {@link EnchantmentActivator} for this Enchantment.
     * This Activator handles when the consumer events are fired for this Enchantment, such that your Enchantment triggers at correct times.
     *
     * @param activator The EnchantmentActivator that will satisfy triggering this Enchantment.
     * @return The CEnchantment instance.
     */
    public CEnchantment setEnchantmentActivator(EnchantmentActivator activator) {
        this.activator = activator;
        return this;
    }

    /**
     * An arbitrary {@link Predicate} that must be satisfied for the enchantment to be applied to the ItemStack.
     * If this predicate fails, the ItemStack will not be enchanted.
     *
     * This can be bypassed using {@link CEnchantment#unsafeEnchant(ItemStack, int)}
     *
     * @param predicate The Predicate to check.
     * @return The CEnchantment instance.
     */
    public CEnchantment setCanEnchantPredicate(Predicate<ItemStack> predicate) {
        this.canEnchantPredicate = predicate;
        return this;
    }

    /**
     * Adds valid {@link Material}s to this enchantment.
     * These can be bypassed using {@link CEnchantment#unsafeEnchant(ItemStack, int)}
     *
     * See {@link CEnchantment#setEnchantmentTarget(EnchantmentTarget)} for groups of Materials that can be added.
     *
     * @param materials The Materialss that are valid for the enchantment.
     * @return The CEnchantment instance.
     */
    public CEnchantment addEnchantables(Material... materials) {
        canEnchant.addAll(Arrays.asList(materials));
        return this;
    }

    /**
     * Adds Enchantments that conflict with this enchantment.
     * Vanilla {@link Enchantment} and CEnchantments can both be added as conflicts.
     *
     * If an item has a conflicting enchantment with this enchantment, the item will not be enchanted with this enchantment.
     *
     * This can be bypassed using {@link CEnchantment#unsafeEnchant(ItemStack, int)}
     *
     * @param enchantments Enchantments to add that conflict with the enchantment.
     * @return The CEnchantment instance.
     */
    public CEnchantment addConflicts(Enchantment... enchantments) {
        conflicts.addAll(Arrays.asList(enchantments));
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when the Player right clicks.
     * @return The CEnchantment instance.
     */
    public CEnchantment onRightClick(BiConsumer<PlayerInteractEvent, CEventResult> consumer) {
        this.rightClick = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when the Entity attacks another entity.
     * @return The CEnchantment instance.
     */
    public CEnchantment onEntityAttack(BiConsumer<EntityDamageByEntityEvent, CEventResult> consumer) {
        this.entityAttack = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when the Entity breaks a block.
     * @return The CEnchantment instance.
     */
    public CEnchantment onBlockBreak(BiConsumer<BlockBreakEvent, CEventResult> consumer) {
        this.blockBreak = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when the Entity places a block.
     * @return The CEnchantment instance.
     */
    public CEnchantment onBlockPlace(BiConsumer<BlockPlaceEvent, CEventResult> consumer) {
        this.blockPlace = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when the Entity fires an arrow.
     * @return The CEnchantment instance.
     */
    public CEnchantment onArrowFire(BiConsumer<ProjectileLaunchEvent, CEventResult> consumer) {
        this.arrowShoot = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an arrow the Entity fired lands
     * @return The CEnchantment instance.
     */
    public CEnchantment onArrowLand(BiConsumer<ProjectileHitEvent, CEventResult> consumer) {
        this.arrowLand = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an arrow the Entity holds the Item that this Enchantment is applied to.
     *                 See {@link CEnchantment#onUnhold(BiConsumer)} for reverting changes you make in this consumer.
     * @return The CEnchantment instance.
     */
    public CEnchantment onHold(BiConsumer<SlotUpdateEvent, CEventResult> consumer) {
        this.hold = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an arrow the Entity stops holds the Item that this Enchantment is applied to.
     *                 See {@link CEnchantment#onHold(BiConsumer)} for applying something when they hold an item.
     * @return The CEnchantment instance.
     */
    public CEnchantment onUnhold(BiConsumer<SlotUpdateEvent, CEventResult> consumer) {
        this.unhold = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an Entity equips an Armor piece that has this Enchantment.
     * @return The CEnchantment instance.
     */
    public CEnchantment onArmorEquip(BiConsumer<SlotUpdateEvent, CEventResult> consumer) {
        this.armorEquip = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an Entity un-equips an Armor piece that has this Enchantment.
     * @return The CEnchantment instance.
     */
    public CEnchantment onArmorUnequip(BiConsumer<SlotUpdateEvent, CEventResult> consumer) {
        this.armorUnequip = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an Entity is damaged.
     * @return The CEnchantment instance.
     */
    public CEnchantment onDamaged(BiConsumer<EntityDamageEvent, CEventResult> consumer) {
        this.damaged = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an Entity dies
     * @return The CEnchantment instance.
     */
    public CEnchantment onDeath(BiConsumer<EntityDeathEvent, CEventResult> consumer) {
        this.death = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an Entity drops an item
     * @return The CEnchantment instance.
     */
    public CEnchantment onDrop(BiConsumer<PlayerDropItemEvent, CEventResult> consumer) {
        this.dropped = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an Entity picks an item up
     * @return The CEnchantment instance.
     */
    public CEnchantment onPickup(BiConsumer<EntityPickupItemEvent, CEventResult> consumer) {
        this.pickup = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an Entity enchants an Item with this Enchantment
     * @return The CEnchantment instance.
     */
    public CEnchantment onEnchant(Consumer<CEventResult> consumer) {
        this.enchant = consumer;
        return this;
    }

    /**
     * @param consumer A consumer that will be applied when an Entity removes this enchantment from an Item
     * @return The CEnchantment instance.
     */
    public CEnchantment onDisenchant(Consumer<CEventResult> consumer) {
        this.disenchant = consumer;
        return this;
    }

    private void registerEnchantment() {
        if(this.isRegistered()) throw new IllegalStateException("Enchantment is already registered");

        //Server already knows about the enchantment, server was probably reloaded.
        //We need to unregister this enchantment.
        if(Enchantment.getByKey(new NamespacedKey(CEnchantmentAPI.getPlugin(), this.getName())) != null) {
            try {
                Field byKey = Enchantment.class.getDeclaredField("byKey");
                Field byName = Enchantment.class.getDeclaredField("byName");
                byKey.setAccessible(true);
                byName.setAccessible(true);
                Map<NamespacedKey, Enchantment> keys = (Map<NamespacedKey, Enchantment>) byKey.get(null);
                Map<String, Enchantment> names = (Map<String, Enchantment>) byName.get(null);

                keys.remove(new NamespacedKey(CEnchantmentAPI.getPlugin(), this.getName()));
                names.remove(this.getName());

                byKey.setAccessible(false);
                byName.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.setBoolean(null, true);
            field.setAccessible(false);

            Enchantment.registerEnchantment(this);
            this.registered = true;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Enchantment.stopAcceptingRegistrations();
        enchantments.add(this);
    }

    /**
     * Checks if two enchantment conflict with each other.
     * For example, it's possible for a CEnchantment to conflict with Sharpness
     * But Sharpness doesn't conflict with Mighty.
     *
     * @param enchantment An Enchantment to check
     * @param enchantment2 A second enchantment to check.
     * @return If either enchantment conflicts with each other.
     */
    public static boolean conflictsWith(Enchantment enchantment, Enchantment enchantment2) {
        return enchantment.conflictsWith(enchantment2) || enchantment2.conflictsWith(enchantment);
    }

    /**
     * @param enchantments All enchantments to check
     * @return If this enchantment conflicts with ANY of the provided me.dessie.dessielib.experimental.enchantments.
     */
    public boolean conflictsWith(Set<Enchantment> enchantments) {
        for(Enchantment enchantment : enchantments) {
            if(conflictsWith(enchantment)) return true;
        }

        return false;
    }

    /**
     * @param item The {@link ItemStack} to check for conflicts.
     * @return If this Enchantment conflicts with any Enchantments on the provided ItemStack.
     */
    public boolean conflictsWith(ItemStack item) {
        for(Enchantment enchantment : item.getEnchantments().keySet()) {
            if(conflictsWith(enchantment)) return true;
        }

        return false;
    }

    /**
     * @param item The {@link ItemStack} to check
     * @return If this Enchantment can safely be applied to the ItemStack.
     */
    @Override
    public boolean canEnchantItem(ItemStack item) {
        return canEnchant.contains(item.getType())
                || this.target != null && getItemTarget().includes(item)
                || (this.canEnchantPredicate != null && this.canEnchantPredicate.test(item));
    }

    /**
     * @param item The {@link ItemStack} to check.
     * @param enchantment The Enchantment to look for
     * @return If the provided ItemStack is enchanted with the provided Enchantment.
     */
    public static boolean hasEnchantment(ItemStack item, Enchantment enchantment) {
        if(item == null) return false;
        if(item.getItemMeta() == null) return false;
        return item.getItemMeta().hasEnchant(enchantment);
    }

    /**
     *
     * @param item The {@link ItemStack} to check.
     * @param enchantment The Enchantment to retrieve the level for.
     * @return The level of the Enchantment on the item, or 0 if the item is not enchanted with the provided Enchantment.
     */
    public static int getLevel(ItemStack item, Enchantment enchantment) {
        if(item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            return meta.getStoredEnchants().getOrDefault(enchantment, 0);
        }

        if(!hasEnchantment(item, enchantment)) return 0;
        return item.getEnchantments().get(enchantment);
    }

    /**
     * @return All registered CEnchantments.
     */
    public static List<CEnchantment> getEnchantments() {
        return enchantments;
    }

    /**
     * This method will not return Vanilla Enchantments.
     *
     * @param item The {@link ItemStack} to get the CEnchantments for
     * @return All CEnchantments that are currently applied to the ItemStack.
     */
    public static List<CEnchantment> getEnchantments(ItemStack item) {
        if(SlotEventHelper.isNullOrAir(item)) return new ArrayList<>();

        if(item.getType() == Material.ENCHANTED_BOOK) {
            return (((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().keySet().stream().map(ench -> CEnchantment.getByKey(ench.getKey()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            return item.getEnchantments().keySet().stream().map(ench -> CEnchantment.getByKey(ench.getKey()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    /**
     * @param name The name of a CEnchantment
     * @return The CEnchantment instance with the provided name, or null if it doesn't exist.
     */
    public static CEnchantment getByName(String name) {
        return enchantments.stream().filter(ench -> ench.getName().equalsIgnoreCase(name))
                .findAny().orElse(null);
    }

    /**
     * @param key The {@link NamespacedKey} of a CEnchantment
     * @return The CEnchantment instance with the provided key, or null if it doesn't exist.
     */
    public static CEnchantment getByKey(NamespacedKey key) {
        return CEnchantment.getEnchantments().stream().filter(ench -> ench.getKey().equals(key)).findAny().orElse(null);
    }

    @Override
    public String toString() {
        return "CEnchantment[" + this.getKey() + "]";
    }

    @Override
    public String getName() { return this.name; }

    @Override
    public int getMaxLevel() { return this.maxLevel; }

    @Override
    public int getStartLevel() { return 1; }

    @Override
    public EnchantmentTarget getItemTarget() { return target; }

    @Override
    public boolean isTreasure() { return treasure; }

    @Override
    public boolean isCursed() { return cursed; }

    @Override
    public boolean conflictsWith(Enchantment enchantment) {
        return conflicts.contains(enchantment) || this == enchantment;
    }

}
