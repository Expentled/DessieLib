package me.dessie.dessielib.enchantmentapi.properties;

import me.dessie.dessielib.enchantmentapi.CEnchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Used to define the enchantment properties for obtaining a specific enchantment
 *
 * This includes the enchantment rarity and where the enchantment can be obtained.
 * Also includes information about how enchantments will behave under specific circumstances, such as when used in a grindstone.
 *
 * To effectively use this class, you should have a basic understanding of how Enchantments are chosen.
 *
 * Minimum and Maximum Power levels are extremely important for determining the rarity of an enchantment within an Enchantment table.
 * Use {@link Rarity} if you want a default rarity that mimics vanilla Enchantments.
 *
 * For Enchantment Table enchantability purposes.
 * See https://minecraft.fandom.com/wiki/Enchanting/Levels for vanilla powers.
 * This stores information for choosing this enchantment with a specific level.
 * If the enchantment power falls between the key of the level in min and max, it's a possible enchantment.
 *
 * For example...
 *
 * minModifiedPower = {1=5, 2=10, 3=21}
 * maxModifiedPower = {1=12, 2=23, 3=43}
 * Chosen Power by calculation = 22
 * This enchantment is chosen and would enchant at level 3, since it falls between 21 and 43.
 * Though it also falls between 10 and 23, we always use the higher level.
 *
 *
 *
 * Anvil Multipliers
 * The enchantment cost when combining this enchantment using an anvil.
 * This multiplier is used as (multiplier * newLevel)
 * For example, Looting's multiplier is 4, so if we combine a Looting 3 and Looting 1 sword
 * this would cost 12 levels (4 * 3).
 *
 * More information on anvil costs can be found here
 * https://minecraft.fandom.com/wiki/Anvil_mechanics#Costs_for_combining_enchantments
 *
 *
 *
 * Grindstone XP Drops
 * By default, Minecraft will drop a random number of XP between the min and max powers for that Enchantment.
 * For example, Protection I has a range of 1 to 12. That means we'll get anywhere from 1 to 12 exp when grinding it.
 *
 * You can override this randomization by using {@link CEnchantProperties#setGrindstoneXpFunction(Function)}
 *
 *
 *
 */
public class CEnchantProperties {

    private CEnchantment enchantment;

    /**
     * The minimum power level for each enchantment level.
     * For example, {1=5, 2=10, 3=21}
     */
    private Map<Integer, Integer> minModifiedPower = new HashMap<>();

    /**
     * The maximum power level for each enchantment level.
     * For example, {1=12, 2=23, 3=43}
     */
    private Map<Integer, Integer> maxModifiedPower = new HashMap<>();

    /**
     * A {@link Rarity} that can be used instead of manually setting the min and max powers.
     */
    private Rarity enchantmentRarity = null;

    /**
     * The weight of an Enchantment. The higher the weight, the more likely it is to appear when enchanting.
     */
    private int enchantmentWeight = 0;

    /**
     * The cost multiplier for combining this enchantment. The higher the multiplier, the more expensive it is to combine.
     */
    private int anvilMultiplier = 0;

    /**
     * If this enchantment can be found when enchanting using an Enchanting Table.
     */
    private boolean canEnchantWithTable = false;

    /**
     * If this enchantment can be in an Enchantment Book.
     */
    private boolean canBeOnBook = false;

    /**
     * If this enchantment can be removed using the Grindstone.
     */
    private boolean canRemoveWithGrindstone = false;

    /**
     * If this enchantment can be found in villager trading.
     */
    private boolean canBeVillagerTrade = false;

    /**
     * The Function that will be used instead of randomly generating the XP amount.
     * Provides you the level of the CEnchantment.
     */
    private Function<Integer, Integer> grindstoneXp;

    /**
     * If the Minecraft should randomly handle the XP dropped from this Enchantment when using a Grindstone.
     */
    private boolean doGrindstoneXpRandom = false;

    /**
     * If this Enchantment is displayed on ItemStacks as Lore.
     */
    private boolean isLoreDisplayed = false;

    /**
     * @return The CEnchantment that these properties are attached to.
     */
    public CEnchantment getEnchantment() { return enchantment; }

    /**
     * @return If this enchantment can be removed using the Grindstone.
     */
    public boolean canRemoveWithGrindstone() { return canRemoveWithGrindstone; }

    /**
     * @return If this enchantment can be found when enchanting using an Enchanting Table.
     */
    public boolean canEnchantWithTable() { return canEnchantWithTable; }

    /**
     * @return If this enchantment can be in an Enchantment Book.
     */
    public boolean canBeOnBook() { return canBeOnBook; }

    /**
     * @return If this enchantment can be found in villager trading.
     */
    public boolean canBeVillagerTrade() { return canBeVillagerTrade; }

    /**
     * @return The anvil cost multiplier for this Enchantment
     */
    public int getAnvilMultiplier() { return anvilMultiplier; }

    /**
     * @return The {@link Rarity} of this Enchantment. Can be null if it was custom.
     */
    public Rarity getEnchantmentRarity() { return enchantmentRarity; }

    /**
     * @return If this enchantment drops random XP.
     */
    public boolean isDoGrindstoneXpRandom() { return doGrindstoneXpRandom; }

    /**
     * @return If the lore is displayed on ItemStacks that have this Enchantment.
     */
    public boolean isLoreDisplayed() { return isLoreDisplayed; }

    /**
     * @return The enchantment weight.
     */
    public int getEnchantmentWeight() {
        if(this.enchantmentWeight == 0 && this.getEnchantmentRarity() == null) return 0;
        return this.enchantmentWeight != 0 ? this.enchantmentWeight : this.getEnchantmentRarity().getWeight();
    }

    /**
     * @param level The level to get the minimum power for.
     * @return The minimum modified power.
     */
    public int getMinModifiedPower(int level) {
        if(!minModifiedPower.containsKey(level) && this.getEnchantmentRarity() == null) return 0;
        return minModifiedPower.containsKey(level) ? minModifiedPower.get(level) : this.getEnchantmentRarity().getMinPower(level);
    }

    /**
     * @param level The level to get the minimum power for.
     * @return The maximum modified power.
     */
    public int getMaxModifiedPower(int level) {
        if(!maxModifiedPower.containsKey(level) && this.getEnchantmentRarity() == null) return 0;
        return maxModifiedPower.containsKey(level) ? maxModifiedPower.get(level) : this.getEnchantmentRarity().getMaxPower(level);
    }

    /**
     * @param level The level of the enchantment
     * @return How much experience should be dropped by the Grindstone at this enchantment level.
     */
    public int getGrindstoneXp(int level) {
        int baseXp = 0;
        if(this.grindstoneXp != null) {
            return this.grindstoneXp.apply(level);
        } else {
            baseXp += this.getMinModifiedPower(level);
        }

        return baseXp;
    }

    /**
     * @param function The function that determines how much XP is dropped at a given level.
     *                 The function provides you the level of the enchantment, and you are expected to return
     *                 the XP drop amount.
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setGrindstoneXpFunction(Function<Integer, Integer> function) {
        this.grindstoneXp = function;
        return this;
    }

    /**
     * Sets the properties of this Enchantment to work as how a vanilla curse would.
     *
     * Curses can:
     *   Be on books
     *   Be villager trades
     *   Displays Lore
     * Curses cannot:
     *   Be removed using the Grindstone
     *   Be enchanted at the Enchantment Table.
     *
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setAsNormalCurse() {
        this.setCanBeOnBook(true);
        this.setCanEnchantWithTable(false);
        this.setCanRemoveWithGrindstone(false);
        this.setCanBeVillagerTrade(true);
        this.setDisplaysLore(true);
        return this;
    }

    /**
     * Sets the properties of this Enchantment to work as how a vanilla enchantment would.
     *
     * Enchantments can:
     *   Be on books
     *   Be villager trades
     *   Be enchanted at the Enchantment Table
     *   Have a {@link Rarity} of COMMON.
     *   Displays Lore
     *   Be removed at the Grindstone
     *   Do random Grindstone XP Drop
     *
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setAsNormalEnchant() {
        this.enchantmentRarity = Rarity.COMMON;
        this.canBeOnBook = true;
        this.canEnchantWithTable = true;
        this.anvilMultiplier = 1;
        this.canRemoveWithGrindstone = true;
        this.doGrindstoneXpRandom = true;
        this.canBeVillagerTrade = true;
        this.isLoreDisplayed = true;
        return this;
    }

    /**
     * @param enchantmentRarity The {@link Rarity} to set
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setEnchantmentRarity(Rarity enchantmentRarity) {
        this.enchantmentRarity = enchantmentRarity;
        return this;
    }

    /**
     * @param weight The weight to set
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setEnchantmentWeight(int weight) {
        this.enchantmentWeight = weight;
        return this;
    }

    /**
     * @param level The level to define a minimum and maximum power for.
     * @param minPower The minimum power.
     * @param maxPower The maximum power.
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setLevelPower(int level, int minPower, int maxPower) {
        this.minModifiedPower.put(level, minPower);
        this.maxModifiedPower.put(level, maxPower);
        return this;
    }

    /**
     * Sets if this Enchantment can be on an Enchantment Book
     * @param canBeOnBook If this Enchantment can be on an Enchantment Book
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setCanBeOnBook(boolean canBeOnBook) {
        this.canBeOnBook = canBeOnBook;
        return this;
    }

    /**
     * Sets if this Enchantment can be found in Villager Trades
     * @param canBeVillagerTrade If this Enchantment can be a Villager trade
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setCanBeVillagerTrade(boolean canBeVillagerTrade) {
        this.canBeVillagerTrade = canBeVillagerTrade;
        return this;
    }

    /**
     * Sets if this Enchantment is displayed on the ItemStack
     * @param displaysLore If this Enchantment is displayed
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setDisplaysLore(boolean displaysLore) {
        this.isLoreDisplayed = displaysLore;
        return this;
    }

    /**
     * Sets the anvil multiplier
     * @param multiplier The new anvil multiplier
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setAnvilMultiplier(int multiplier) {
        this.anvilMultiplier = multiplier;
        return this;
    }

    /**
     * Sets if this Enchantment can be enchanted using the Enchanting Table
     * @param canEnchantWithTable If this Enchantment can be enchanted using the Enchanting Table
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setCanEnchantWithTable(boolean canEnchantWithTable) {
        this.canEnchantWithTable = canEnchantWithTable;
        return this;
    }

    /**
     * Sets if this Enchantment can be removed at the Grindstone
     * @param canRemoveWithGrindstone If this Enchantment can be removed at the Grindstone
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setCanRemoveWithGrindstone(boolean canRemoveWithGrindstone) {
        this.canRemoveWithGrindstone = canRemoveWithGrindstone;
        return this;
    }

    /**
     * @param doGrindstoneXpRandom If this enchantment should drop random XP from the Grindstone.
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setDoGrindstoneXpRandom(boolean doGrindstoneXpRandom) {
        this.doGrindstoneXpRandom = doGrindstoneXpRandom;
        return this;
    }

    /**
     * Sets the {@link CEnchantment} that these properties apply to.
     * @param enchantment The CEnchantment
     * @return The CEnchantProperties instance.
     */
    public CEnchantProperties setEnchantment(CEnchantment enchantment) {
        this.enchantment = enchantment;
        return this;
    }
}
