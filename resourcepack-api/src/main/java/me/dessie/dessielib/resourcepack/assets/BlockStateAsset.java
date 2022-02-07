package me.dessie.dessielib.resourcepack.assets;

import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import me.dessie.dessielib.resourcepack.ResourcePack;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class ONLY tells the ResourcePack what specific BlockStates should be replaced with a specific texture.
 * This class will not generate any textures or model files, {@link BlockAsset} to generate and copy custom texture/model files.
 *
 * This class also adds functionality to these custom BlockStates, through the use of consumers.
 */
public class BlockStateAsset extends Asset implements Listener {

    private final File minecraftBlockStatesFolder;

    //The requirements for the BlockState to "activate". For example, facing=east.
    private final Map<String, String> predicates = new HashMap<>();

    //A property to apply to this BlockState, for example, y=90
    private final Map<String, Object> properties = new HashMap<>();

    //The block to replace as this state, such as note_block or red_mushroom_block
    private final String replacementName;

    //The model that this BlockState will use.
    //This should be given with a Namespace as well.
    //For example,
    //minecraft:block/anvil
    //dessielib:block/custom_block
    private final String model;

    private double strength = -1;
    private final List<Material> preferredItems = new ArrayList<>();
    private boolean preferredItemRequiredForDrop = false;
    private final List<ItemStack> drops = new ArrayList<>();

    //A function that will take in an ItemStack and a Player
    //And should return a double for the break speed with that item.
    private final BiFunction<ItemStack, Player, Double> breakSpeedFunction = (item, player) -> {
        //Don't do any calculation is strength is -1
        if(this.getStrength() == -1) return 0.0;

        //How quickly the block should be broken by a specific item.
        //This only applies if the item is "preferred", like stone and pickaxe, or wood and axe.
        //If an item isn't preferred, it will be 1.
        double breakSpeed = 1.0;
        if(this.getPreferredItems().contains(item.getType()) && CraftItemStack.asNMSCopy(item).getItem() instanceof DiggerItem diggerItem) {
            breakSpeed = diggerItem.getTier().getSpeed();
        }

        //Apply the Efficiency enchantment if the tool was preferred.
        if(breakSpeed > 1) {
            breakSpeed += (float)(Math.pow(item.getEnchantmentLevel(Enchantment.DIG_SPEED), 2)) + 1;
        }

        //Apply the Haste potion effect
        if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
            breakSpeed *= 1.0F + (float)(player.getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier() + 1) * 0.2F;
        }

        //Apply water slowdown
        if (((CraftPlayer) player).getHandle().isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(((CraftPlayer) player).getHandle())) {
            breakSpeed /= 5.0F;
        }

        //Apply in air slowdown
        if (!player.isOnGround()) {
            breakSpeed /= 5.0F;
        }

        return breakSpeed / this.getStrength() / (this.getPreferredItems().contains(item.getType()) ? 30 : 100);
    };

    /**
     * @param name The Asset's name
     * @param replacementName The Vanilla BlockState name to override.
     *                        For example, "note_block" or "dirt"
     *
     * @param model A BlockAsset to use as the Override
     */
    public BlockStateAsset(String name, String replacementName, BlockAsset model) {
        this(name, replacementName, model.getNamespace() + ":block/" + model.getName());
    }

    /**
     * @param name The Asset's name
     * @param replacementName The Vanilla BlockState name to override.
     *                        For example, "note_block" or "dirt"
     *
     * @param model A Material to use as the override.
     *              If you're having issues with this, you may need to manually find the name of the BlockState json file.
     */
    public BlockStateAsset(String name, String replacementName, Material model) {
        this(name, replacementName, "minecraft:block/" + model.name().toLowerCase(Locale.ROOT));
    }

    /**
     * @param name The Asset's name
     * @param replacement A Material to Override.
     *                    If you're having issues with this, you may need to manually find the name of the BlockState json file.
     *
     * @param model A BlockAsset to use as the Override
     */
    public BlockStateAsset(String name, Material replacement, BlockAsset model) {
        this(name, replacement.name().toLowerCase(Locale.ROOT), model);
    }

    /**
     * @param name The Asset's name
     * @param replacement A Material to Override.
     *                    If you're having issues with this, you may need to manually find the name of the BlockState json file.
     *
     * @param model A Material to use as the override.
     *              If you're having issues with this, you may need to manually find the name of the BlockState json file.
     */
    public BlockStateAsset(String name, Material replacement, Material model) {
        this(name, replacement.name().toLowerCase(Locale.ROOT), model);
    }

    /**
     * @param name The Asset's name
     * @param replacement A Material to Override.
     *                    If you're having issues with this, you may need to manually find the name of the BlockState json file.
     *
     * @param model The model to replace the replacementName with.
     *              For example, "minecraft:diamond_block" or "dessielib:custom_block"
     */
    public BlockStateAsset(String name, Material replacement, String model) {
        this(name, replacement.name().toLowerCase(Locale.ROOT), model);
    }

    /**
     * @param name The Asset's name
     * @param replacementName The Vanilla BlockState name to override.
     *                        For example, "note_block" or "dirt"
     *
     * @param model The model to replace the replacementName with.
     *              For example, "minecraft:diamond_block" or "dessielib:custom_block"
     */
    public BlockStateAsset(String name, String replacementName, String model) {
        super(name);
        this.model = model;
        this.replacementName = replacementName;
        this.minecraftBlockStatesFolder = new File(this.getAssetsFolder() + "/minecraft/blockstates");

        //Add the BlockBreakListener for drops.
        this.addEventListener(BlockBreakEvent.class, (asset, event) -> {
            return asset.blockMatches(event.getBlock()) && !event.isCancelled() && event.getPlayer().getGameMode() == GameMode.SURVIVAL;
        }, (event -> {
            if(this.isPreferredItemRequiredForDrop()) {
                if(this.getPreferredItems().contains(event.getPlayer().getInventory().getItemInMainHand().getType())) {
                    for (ItemStack item : this.getDrops()) {
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
                    }
                }
            }
        }), EventPriority.HIGHEST);

        this.setGenerator(new AssetGenerator() {
            @Override
            public void init(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<BlockStateAsset> assets = this.cast(BlockStateAsset.class, assetList);

                assets.stream().findAny().ifPresent(asset -> {
                    this.createDirectories(asset.getMinecraftBlockStatesFolder());
                });
            }

            @Override
            public void generate(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<BlockStateAsset> assets = this.cast(BlockStateAsset.class, assetList);

                for(BlockStateAsset asset : assets) {
                    //Get all the BlockState assets that are in this file.
                    List<BlockStateAsset> toGenerate = builder.getAssetsOf(BlockStateAsset.class)
                            .stream()
                            .filter(generate -> asset.getReplacementName().equals(generate.getReplacementName()))
                            .collect(Collectors.toList());

                    //Create the BlockState file.
                    String fileName = asset.getReplacementName() + ".json";
                    File blockStateFile = new File(asset.getMinecraftBlockStatesFolder(), fileName);

                    JsonObjectBuilder object = new JsonObjectBuilder();

                    for(BlockStateAsset generate : toGenerate) {
                        StringBuilder state = new StringBuilder();
                        for(String key : generate.getPredicates().keySet()) {
                            state.append(key).append("=").append(generate.getPredicates().get(key)).append(",");
                        }
                        //Delete the trailing comma
                        if(state.length() > 0) {
                            state.delete(state.length() - 1, state.length());
                        }

                        JsonObjectBuilder property = new JsonObjectBuilder();

                        //Add all the properties.
                        property.add("model", generate.getModel());

                        for(String key : generate.getProperties().keySet()) {
                            Object value = generate.getProperties().get(key);
                            if(value instanceof String) {
                                property.add(key, (String) value);
                            }
                            if(value instanceof Number) {
                                property.add(key, (Number) value);
                            }

                            if(value instanceof Boolean) {
                                property.add(key, (Boolean) value);
                            }

                            if(value instanceof Character) {
                                property.add(key, (Character) value);
                            }
                        }
                        object.add(state.toString(), property.getObject());
                    }

                    write(new JsonObjectBuilder().add("variants", object.getObject()).getObject(), blockStateFile);
                }
            }
        });
    }

    /**
     * @return The name of the file to use within the resource pack.
     */
    public String getReplacementName() {return replacementName;}

    /**
     * @return The vanilla predicates that must be met for this state to be valid.
     */
    public Map<String, String> getPredicates() {return predicates;}

    /**
     * @return The properties for this state asset.
     */
    public Map<String, Object> getProperties() {return properties;}

    /**
     * @return The custom strength for this block.
     *         Strength is used when determining how long a block should take to mine.
     */
    public double getStrength() {return strength;}

    /**
     * @return What {@link ItemStack} the block should drop.
     */
    public List<ItemStack> getDrops() {return drops;}

    /**
     * @return Which {@link Material}s are preferred when breaking this block.
     */
    public List<Material> getPreferredItems() {return preferredItems;}

    /**
     * @return If a preferred material must be used to get drops from this block.
     */
    public boolean isPreferredItemRequiredForDrop() {return preferredItemRequiredForDrop;}

    /**
     * @return The folder that contains the minecraft block state resources.
     *         Resolves to serverDir/plugins/pluginName/assets/minecraft/blockstates
     */
    public File getMinecraftBlockStatesFolder() {return minecraftBlockStatesFolder;}

    /**
     * @return The String reference to the model texture.
     *         For example,
     *         minecraft:block/anvil
     *         dessielib:block/custom_block
     */
    public String getModel() {return model;}

    /**
     * @param item The {@link ItemStack} that is being used to break the block.
     * @param player The {@link Player} that is breaking the block.
     * @return How quickly the player can mine the block with that ItemStack.
     */
    public double getBreakSpeed(ItemStack item, Player player) {
        return this.breakSpeedFunction.apply(item, player);
    }

    /**
     * Adds a predicate that is used to determine when this asset should be applied
     * to the material. Blocks have different types of predicates that can be used.
     *
     * You may need to reference the internal Minecraft resource pack to find
     * valid keys and values for the block you're using.
     *
     * @param key The key of the predicate. For example, "facing".
     * @param value The value of the predicate. For example, "west".
     * @return The BlockStateAsset instance.
     */
    public BlockStateAsset addPredicate(String key, String value) {
        predicates.put(key, value);
        return this;
    }

    /**
     * Adds a property that is applied when this asset is being applied to a block.
     * For example, y=90 will rotate the block on the y-axis by 90 degrees.
     *
     * You may need to reference the internal Minecraft resource pack to find
     * valid keys and values for the block you're using.
     *
     * @param key The key of the property. For example, "y".
     * @param value The value of the predicate. For example, "90".
     * @return The BlockStateAsset instance.
     */
    public BlockStateAsset addProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Materials within this List will break this block faster.
     * This method only obeys Axes, Pickaxes, Shovels, and Hoes.
     *
     * This method does nothing if the strength is -1.
     *
     * @param materials The preferred Materials to add
     * @return This BlockStateAsset
     */
    public BlockStateAsset addPreferredItems(Material... materials) {
        this.preferredItems.addAll(Arrays.asList(materials));
        return this;
    }

    /**
     * The strength of this BlockStateAsset.
     * If it's -1, the break speed will be unmodified.
     * If it's changed, the break speed will be calculated and the API will attempt to mimic this break speed.
     *
     * For strength examples, see net.minecraft.world.level.block.Blocks.class, as this is where you can find default values.
     * Some block examples:
     * Obsidian: 50
     * Stone: 1.5
     * Dirt: 0.5
     * Planks: 2.0
     *
     * @param strength The Strength of the block
     * @return This BlockStateAsset
     */
    public BlockStateAsset setStrength(double strength) {
        this.strength = strength;
        return this;
    }

    /**
     * If true, one of the Preferred ItemStack materials must be used for anything to drop.
     * @param required If a preferred item is required or not.
     * @return This BlockStateAsset
     */
    public BlockStateAsset setPreferredItemRequiredToDrop(boolean required) {
        preferredItemRequiredForDrop = required;
        return this;
    }

    /**
     * When this block is destroyed, items from the drop list will be dropped instead of the default item.
     *
     * @param items The ItemStacks that this BlockStateAsset should drop.
     * @return This BlockStateAsset
     */
    public BlockStateAsset addDrops(ItemStack... items) {
        this.drops.addAll(Arrays.asList(items));
        return this;
    }

    /**
     * When this block is destroyed, items from the drop list will be dropped instead of the default item.
     *
     * @param items The ItemAssets that this BlockStateAsset should drop.
     * @return This BlockStateAsset
     */
    public BlockStateAsset addDrops(ItemAsset... items) {
        this.drops.addAll(Arrays.stream(items).map(ItemAsset::getItem).toList());
        return this;
    }

    /**
     * Adds an EventListener to this BlockStateAsset, that will callback your methods when the event is triggered and the predicate is met.
     * Generally, your predicate will use {@link #blockMatches(Block)} to make sure the Block matches this asset.
     *
     * @param type The Event class
     * @param predicate The Predicate that should be met to fire your listener.
     * @param consumer A consumer that accepts the event
     * @return This BlockStateAsset
     */
    public <T extends Event> BlockStateAsset addEventListener(Class<T> type, BiPredicate<BlockStateAsset, T> predicate, Consumer<T> consumer) {
        return this.addEventListener(type, predicate, consumer, EventPriority.NORMAL);
    }

    /**
     * Adds an EventListener to this BlockStateAsset, that will callback your methods when the event is triggered and the predicate is met.
     * Generally, your predicate will use {@link #blockMatches(Block)} to make sure the Block matches this asset.
     *
     * @param type The Event class
     * @param predicate The Predicate that should be met to fire your listener.
     * @param consumer A consumer that accepts the event
     * @param priority The Event's priority
     * @return This BlockStateAsset
     */
    public <T extends Event> BlockStateAsset addEventListener(Class<T> type, BiPredicate<BlockStateAsset, T> predicate, Consumer<T> consumer, EventPriority priority) {
        ResourcePack.getPlugin().getServer().getPluginManager().registerEvent(type, this, priority, (listener, event) -> {
            if(predicate.test(this, (T) event)) {
                consumer.accept((T) event);
            }
        }, ResourcePack.getPlugin());
        return this;
    }

    /**
     * Tests if a Bukkit Block matches this BlockStateAsset
     * All state properties must match for this to return true.
     *
     * @param block The Block to chest
     * @return If the Block matches this BlockStateAsset's properties.
     */
    public boolean blockMatches(Block block) {
        BlockState state = ((CraftBlock) block).getNMS();
        for(String property : this.getPredicates().keySet()) {
            Property<?> stateProperty = state.getProperties().stream().filter(prop -> prop.getName().equalsIgnoreCase(property)).findAny().orElse(null);
            if(stateProperty == null || !this.getPredicates().get(property).equalsIgnoreCase(state.getValue(stateProperty).toString())) {
                return false;
            }
        }

        return true;
    }
}
