package me.dessie.dessielib.inventoryapi;

import me.dessie.dessielib.core.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class InventoryBuilder {
    private static HashMap<Player, InventoryBuilder> inventories = new HashMap<>();

    private int size;
    private String name;
    private Inventory inventory;
    private List<InventoryBuilder> pages = new ArrayList<>();
    private Player player;
    private int page;

    private BiConsumer<Player, InventoryBuilder> close;
    private BiConsumer<Player, InventoryBuilder> open;
    private BiConsumer<Player, InventoryBuilder> pageChange;

    private boolean preventClose = false;

    private HashMap<Integer, ItemBuilder> items = new HashMap<>();

    /**
     * Creates an empty Inventory with a size and title.
     * @param size The size of the Inventory, should be divisible by 9 and no larger than 54.
     * @param name The name of the Inventory.
     */
    public InventoryBuilder(int size, String name) {
        if(!InventoryAPI.isRegistered()) {
            throw new IllegalStateException("You need to register InventoryAPI before creating InventoryBuilders!");
        }

        this.size = size;
        this.name = name;
        this.page = 0;
        this.pages.add(this);
    }

    /**
     *
     * @param builder The InventoryBuilder to copy. Pages will not be copied.
     */
    public InventoryBuilder(InventoryBuilder builder) {
        this(builder, false);
    }

    /**
     * Creates a complete copy of this Inventory.
     *
     * @param builder The InventoryBuilder to copy.
     * @param pages If pages should be copied
     */
    public InventoryBuilder(InventoryBuilder builder, boolean pages) {
        Objects.requireNonNull(builder, "Cannot copy null builder!");

        this.size = builder.getSize();
        this.name = builder.getName();
        this.close = builder.close;
        this.open = builder.open;
        this.pageChange = builder.pageChange;
        this.preventClose = builder.preventClose;
        this.items = new HashMap<>(builder.items);

        if(pages) {
            for(InventoryBuilder page : this.pages) {
                this.pages.add(new InventoryBuilder(page));
            }
        }
    }

    /**
     * @return The title of this Inventory
     */
    public String getName() { return this.name; }

    /**
     * @return The size of this Inventory
     */
    public int getSize() { return this.size; }

    /**
     * @param slot The Inventory slot
     * @return The {@link ItemBuilder} in the specified slot
     */
    public ItemBuilder getItem(int slot) { return this.items.get(slot); }

    /**
     * @return The {@link Inventory} this Builder represents
     */
    public Inventory getInventory() { return this.inventory; }

    /**
     * @return The Player this Inventory was assigned to last.
     */

    public Player getPlayer() { return this.player; }

    /**
     * @return A Map of all {@link ItemBuilder}s and their current slot.
     */
    public HashMap<Integer, ItemBuilder> getItems() {return this.items;}

    /**
     * @return If the player can close the Inventory
     */
    public boolean isPreventClose() {
        return this.preventClose;
    }

    /**
     * @return The current page this Inventory is viewing
     */
    public int getCurrentPage() { return this.page + 1; }

    /**
     * Opens an InventoryBuilder for the Player.
     *
     * If an InventoryBuilder is opened for multiple players, only the last
     * player will be tracked within this object.
     *
     * The Inventory will be updated for all players when it is modified.
     *
     * If you need to have a per-player inventory, you use {@link #InventoryBuilder(InventoryBuilder)}
     * to create a copy.
     *
     * @param player The player to open this inventory for
     * @return The InventoryBuilder
     */
    public InventoryBuilder open(Player player) {
        Inventory inv = Bukkit.createInventory(null, this.size, Colors.color(this.name));
        for(int i : this.items.keySet()) {
            inv.setItem(i, this.items.get(i).getItem());
        }

        this.player = player;
        this.inventory = inv;
        player.openInventory(inv);
        inventories.put(player, this);

        executeOpen(player, this);
        return this;
    }


    /**
     * Forcefully closes the Inventory.
     * @param player The player to close
     * @return The InventoryBuilder
     */
    public InventoryBuilder close(Player player) {
        this.preventClose = false;
        player.closeInventory();

        return this;
    }

    /**
     * Note: The Inventory title will not update unless it is reopened.
     *
     * @param name The new name of this InventoryBuilder
     * @return The InventoryBuilder
     */
    public InventoryBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Note: The Inventory size will not update unless it is reopened.
     *
     * @param size The new size of this InventoryBuilder
     * @return The InventoryBuilder
     */
    public InventoryBuilder setSize(int size) {
        if(size <= 0 || size % 9 != 0 || size > 54) {
            throw new IllegalArgumentException("Invalid Inventory size!");
        }

        this.size = size;
        return this;
    }

    /**
     * When true, the InventoryBuilder cannot be closed by the player.
     * {@see #close} for manually closing the Inventory.
     *
     * @param preventClose Whether the player can or cannot close the Inventory
     * @return The InventoryBuilder
     */
    public InventoryBuilder setPreventsClose(boolean preventClose) {
        this.preventClose = preventClose;
        return this;
    }

    /**
     * Adds the item into the first empty slot into the Inventory.
     * Returns null if the inventory is full.
     *
     * @param item The {@link ItemBuilder} to place into the inventory
     * @return The ItemBuilder that was placed, or null if it could not be placed.
     */
    public ItemBuilder addItem(ItemBuilder item) {
        for(int i = 0; i < this.getSize(); i++) {
            if(this.items.get(i) == null) {
                return setItem(item, i);
            }
        }

        return null;
    }

    /**
     * Adds the item into the first empty slot into the Inventory.
     * Returns null if the inventory is full.
     *
     * @param item The {@link ItemStack} to place into the inventory
     * @return The generated ItemBuilder that was placed, or null if it could not be placed.
     */
    public ItemBuilder addItem(ItemStack item) {
        for(int i = 0; i < this.getSize(); i++) {
            if(this.items.get(i) == null) {
                return setItem(item, i);
            }
        }

        return null;
    }


    /**
     * Adds ALL items into the InventoryBuilder.
     * If the current page is full, the InventoryBuilder will be copied
     * and items will begin to be placed there.
     *
     * @param items A list of {@link ItemBuilder}s to place into the inventory
     * @return The InventoryBuilder
     */
    public InventoryBuilder addItemsAndPages(List<ItemBuilder> items) {
        //Copy the original builder incase we need to add more pages.
        InventoryBuilder copy = new InventoryBuilder(this);

        boolean addPage = false;
        int page = 0;
        int itemsAdded = 0;
        while(itemsAdded != items.size()) {
            if(addPage) {
                this.addPage(new InventoryBuilder(copy));
                addPage = false;
            }

            if(this.getPage(page).addItem(items.get(itemsAdded)) == null) {
                addPage = true;
                page++;
            }

            //Don't increment items added, since we weren't able to add the last item.
            if(!addPage) {
                itemsAdded++;
            }
        }

        return this;
    }

    /**
     * Sets an {@link ItemStack} in a specific slot, regardless of if the slot is empty.
     * Passing null as the item will clear the slot.
     *
     * @param item The {@link ItemStack} to add.
     * @param slot The slot to set the item in
     * @return The placed ItemBuilder.
     */
    public ItemBuilder setItem(ItemStack item, int slot) {
        if(item == null || item.getType() == Material.AIR) {
            this.items.remove(slot);
            this.update();

            return null;
        } else {
            return setItem(new ItemBuilder(item), slot);
        }
    }

    /**
     * Sets an {@link ItemBuilder} in a specific slot, regardless of if the slot is empty.
     * Passing null as the item will clear the slot.
     *
     * @param item The {@link ItemBuilder} to add.
     * @param slot The slot to set the item in
     * @return The placed ItemBuilder.
     */
    public ItemBuilder setItem(ItemBuilder item, int slot) {
        if(item == null || item.getItem().getType() == Material.AIR) {
            this.items.remove(slot);
            this.update();
            return null;
        } else {
            this.items.put(slot, item);
            item.setBuilder(this);
            item.slot = slot;

            this.update();

            return item;
        }
    }

    /**
     * Sets an {@link ItemBuilder} in multiple slots, regardless of if the slots are empty.
     *
     * @param item The {@link ItemBuilder} to add.
     * @param slots The slots to place the ItemBuilder into.
     * @return The InventoryBuilder.
     */
    public InventoryBuilder setItems(ItemBuilder item, Integer... slots) {
        for(int i : slots) {
            setItem(item, i);
        }
        return this;
    }

    /**
     * Sets an {@link ItemStack} in multiple slots, regardless of if the slots are empty.
     *
     * @param item The {@link ItemStack} to add.
     * @param slots The slots to place the ItemBuilder into.
     * @return The InventoryBuilder.
     */
    public InventoryBuilder setItems(ItemStack item, Integer... slots) {
        for(int i : slots) {
            setItem(item, i);
        }
        return this;
    }

    /**
     * Copies the contents from a {@link Inventory} to this InventoryBuilder.
     * @param inventory The Inventory to copy from
     * @return The InventoryBuilder
     */
    public InventoryBuilder setContents(Inventory inventory) {
        return setContents(inventory.getContents());
    }

    /**
     * Sets the contents of the InventoryBuilder in order from the provided array.
     * @param items The items to set.
     * @return The InventoryBuilder
     */
    public InventoryBuilder setContents(ItemStack[] items) {
        for(int i = 0; i < items.length; i++) {
            if(items[i] == null || items[i].getType() == Material.AIR) continue;

            setItem(items[i], i);
        }
        return this;
    }

    /**
     * Sets the contents of the InventoryBuilder in order from the provided list.
     * @param items The items to set.
     * @return The InventoryBuilder
     */
    public InventoryBuilder setContents(List<ItemBuilder> items) {
        return setContents(items, true);
    }


    /**
     * Sets the contents of the InventoryBuilder from the list.
     * @param items The items to set.
     * @param bySlot If the slot within each ItemBuilder should be obeyed,
     *               or just set the contents as the order of the list.
     * @return The InventoryBuilder
     */
    public InventoryBuilder setContents(List<ItemBuilder> items, boolean bySlot) {
        if(bySlot) {
            for(ItemBuilder item : items) {
                setItem(item, item.getSlot());
            }
        } else {
            for(int i = 0; i < items.size() && i < this.getSize(); i++) {
                setItem(items.get(i), i);
            }
        }

        return this;
    }

    /**
     * Copies the contents from a {@link InventoryBuilder} to this InventoryBuilder.
     * @param builder The InventoryBuilder to copy from
     * @return The InventoryBuilder
     */
    public InventoryBuilder setContents(InventoryBuilder builder) {
        setContents(new ArrayList<>(builder.getItems().values()));
        return this;
    }

    /**
     * Clears all items from this InventoryBuilder
     * @return The InventoryBuilder
     */
    public InventoryBuilder clear() {
        this.items.clear();
        this.update();
        return this;
    }

    /**
     * Fills all empty slots with the specified {@link ItemBuilder}
     *
     * @param item The item to set
     * @return The InventoryBuilder
     */
    //Fills all null slots with the specified ItemBuilder.
    public InventoryBuilder fill(ItemBuilder item) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i) == null) {
                setItem(new ItemBuilder(item), i);
            }
        }
        return this;
    }

    /**
     * Shifts all the items to the top most left position possible.
     *
     * @param byItems If the inventory should be sorted by {@link Material}, alphabetically, and by stack size.
     * @return The InventoryBuilder
     */
    public InventoryBuilder organize(boolean byItems) {

        //Move all the items to the top left.
        for(int i = 0; i < this.getSize(); i++) {
            if(this.getItem(i) == null) continue;

            for(int j = 0; j < this.getSize(); j++) {
                if(this.getItem(j) == null) {
                    setItem(this.getItem(i), j);
                    setItem((ItemStack) null, i);
                    break;
                }
            }
        }

        if(byItems) {
            Comparator<ItemBuilder> nameSort = Comparator.comparing(item -> item.getItem().getType().toString());
            Comparator<ItemBuilder> amountSort = Comparator.comparing(ItemBuilder::getAmount).reversed();

            //Sort by name then by amount
            this.setContents(this.getItems().values().stream().map(ItemBuilder::new)
                    .sorted(nameSort.thenComparing(amountSort))
                    .collect(Collectors.toList()), false);
        }

        //Update the inventory.
        this.update();

        return this;
    }

    /**
     * @see #organize(boolean)
     *
     * @return The organized InventoryBuilder
     */
    public InventoryBuilder organize() {
        return organize(false);
    }

    /**
     * Combines all possible ItemStacks together into their maximum stack size.
     * @return The InventoryBuilder
     */
    public InventoryBuilder condense() {
        for(int i = 0; i < this.getSize(); i++) {
            if(!this.getItems().containsKey(i)) continue;
            ItemBuilder item = this.getItem(i);

            //Work through the list backwards so we condense at the most top left item.
            for(int j = this.size - 1; j > 0 && j >= i; j--) {
                if(!this.getItems().containsKey(j)) continue;
                ItemBuilder condensed = this.getItem(j);

                //Don't combine to itself.
                if(item == condensed) continue;

                if (item.isSimilar(condensed) && item.getAmount() <= item.getItem().getMaxStackSize()) {
                    while(condensed.getAmount() > 0 && item.getAmount() < item.getItem().getMaxStackSize()) {
                        item.setAmount(item.getAmount() + 1);

                        if(condensed.getAmount() - 1 != 0) {
                            condensed.setAmount(condensed.getAmount() - 1);
                        } else {
                            condensed.setAmount(0);
                            this.setItem((ItemStack) null, condensed.getSlot());
                        }

                    }
                }
            }
        }

        return this;
    }

    /**
     * Replace all {@link ItemBuilder}s with another.
     * @param oldItem The ItemBuilder to replace
     * @param replacement The replacement ItemBuilder
     * @return The InventoryBuilder
     */
    public InventoryBuilder replaceAll(ItemBuilder oldItem, ItemBuilder replacement) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i).isSimilar(oldItem)) {
                setItem(replacement, i);
            }
        }
        return this;
    }

    /**
     * Finds the first instance of an {@link ItemBuilder}
     * @param item The ItemBuilder to look for.
     * @return The found ItemBuilder, or null if it doesn't exist within the Inventory.
     */
    public ItemBuilder findFirst(ItemBuilder item) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i) == item) {
                return getItem(i);
            }
        }

        return null;
    }

    /**
     * Finds the first instance of an {@link ItemBuilder} with the specified {@link ItemStack}
     * @param item The ItemBuilder to look for that has the required ItemStack.
     * @return The found ItemBuilder, or null if it doesn't exist within the Inventory.
     */
    public ItemBuilder findFirst(ItemStack item) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i).getItem().isSimilar(item)) {
                return getItem(i);
            }
        }

        return null;
    }

    /**
     * Finds the first instance of an {@link ItemBuilder} with the specified {@link Material}
     * @param material The ItemBuilder to look for that has the required Material.
     * @return The found ItemBuilder, or null if it doesn't exist within the Inventory.
     */
    public ItemBuilder findFirst(Material material) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i).getItem().getType() == material) {
                return getItem(i);
            }
        }

        return null;
    }

    /**
     * Called when the Inventory is closed.
     * @param consumer A BiFunction containing the Player that closed the Inventory
     *                 and the InventoryBuilder itself.
     * @return The InventoryBuilder
     */
    public InventoryBuilder onClose(BiConsumer<Player, InventoryBuilder> consumer) {
        this.close = consumer;
        return this;
    }

    /**
     * Called when the Inventory is opened.
     * @param consumer A BiFunction containing the Player that opened the Inventory
     *                 and the InventoryBuilder itself.
     * @return The InventoryBuilder
     */
    public InventoryBuilder onOpen(BiConsumer<Player, InventoryBuilder> consumer) {
        this.open = consumer;
        return this;
    }

    /**
     * Called when the Inventory's page changes.
     * @param consumer A BiFunction containing the Player that changed the page
     *                 and the new page.
     * @return The current InventoryBuilder
     */
    public InventoryBuilder onPageChange(BiConsumer<Player, InventoryBuilder> consumer) {
        this.pageChange = consumer;
        return this;
    }

    /**
     * Forcefully places page arrows into an Inventory
     * @return The InventoryBuilder
     */
    public InventoryBuilder setPageArrows() {
        this.setItem(ItemBuilder.buildItem(Material.ARROW, 1, "&cNext"), this.getSize() - 1)
                .onClick((player, item) -> this.nextPage()).cancel();

        this.setItem(ItemBuilder.buildItem(Material.ARROW, 1, "&cPrevious"), this.getSize() - 9)
                .onClick((player, item) -> this.previousPage()).cancel();

        return this;
    }


    /**
     * Adds an InventoryBuilder as a page to this InventoryBuilder
     * Page arrows will automatically be set on the bottom corner slots.
     *
     * @param builder The builder to add.
     * @return The original InventoryBuilder
     */
    public InventoryBuilder addPage(InventoryBuilder builder) {
        builder.setPageArrows();
        this.setPageArrows();

        this.pages.add(builder);
        builder.pages = this.pages;
        builder.page = this.pages.size() - 1;
        return this;
    }

    /**
     * Adds an InventoryBuilder as a page to this InventoryBuilder
     * Page arrows will automatically be set on the bottom corner slots.
     *
     * @param size The size of the new InventoryBuilder.
     * @param name The name of the new InventoryBuilder.
     * @return The original InventoryBuilder
     */
    public InventoryBuilder addPage(int size, String name) {
        InventoryBuilder newBuilder = new InventoryBuilder(size, name);
        addPage(newBuilder);
        return this;
    }


    /**
     * @param page The page to get
     * @return The InventoryBuilder at that page index
     */
    public InventoryBuilder getPage(int page) {
        try {
            return this.pages.get(page);
        } catch (IndexOutOfBoundsException ignored) {
            throw new IndexOutOfBoundsException("Page does not exist!");
        }
    }

    /**
     * Note: The current InventoryBuilder must be atleast 27 slots.
     * @param item The {@link ItemBuilder} to create the border from.
     * @return The InventoryBuilder
     */
    //Puts the specified ItemBuilder in the Inventory borders.
    //Inventory must be atleast 27 in size.
    public InventoryBuilder createBorder(ItemBuilder item) {
        if(this.getSize() < 18) {
            throw new IllegalStateException("Inventory size must be atleast 27 to add borders!");
        }

        int[] edgeSlots54 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        int[] edgeSlots45 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
        int[] edgeSlots36 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};
        int[] edgeSlots27 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};

        switch (this.getSize()) {
            case 54: {
                for (int i : edgeSlots54) {
                    if (this.getItem(i) == null) setItem(item, i);
                }
                break;
            }

            case 45: {
                for (int i : edgeSlots45) {
                    if (this.getItem(i) == null) setItem(item, i);
                }
                break;
            }

            case 36: {
                for (int i : edgeSlots36) {
                    if (this.getItem(i) == null) setItem(item, i);
                }
                break;
            }

            case 27: {
                for (int i : edgeSlots27) {
                    if (this.getItem(i) == null) setItem(item, i);

                }
                break;
            }
        }
        return this;
    }

    /**
     * @param player The Player to get
     * @return The currently open InventoryBuilder of the player
     */
    public static InventoryBuilder getBuilder(Player player) {
        return inventories.get(player);
    }

    //Internal method.
    //Executes the onClose Runnable
    void executeClose(Player player, InventoryBuilder builder) {
        if(this.close != null) {
            close.accept(player, builder);
        }
    }

    //Internal method.
    //Executes the onOpen Runnable
    void executeOpen(Player player, InventoryBuilder builder) {
        if(this.open != null) {
            this.open.accept(player, builder);
        }
    }

    //Internal method.
    //Opens the next page in the pages list.
    private void nextPage() {
        InventoryBuilder newPage = null;

        if(this.page + 1 <= this.pages.size()) {
            try {
                newPage = this.pages.get(this.page + 1);
            } catch (IndexOutOfBoundsException ignored) {}
        }

        if(newPage != null) {
            if(this.pageChange != null) {
                this.pageChange.accept(this.getPlayer(), newPage);
            }

            newPage.open(this.player);
        }
    }

    //Internal method.
    //Opens the previous page in the pages list.
    private void previousPage() {
        InventoryBuilder newPage = null;

        if(this.page - 1 >= 0) {
            try {
                newPage = this.pages.get(this.page - 1);
            } catch(ArrayIndexOutOfBoundsException e) { e.printStackTrace();}
        }

        if(newPage != null) {
            if(this.pageChange != null) {
                this.pageChange.accept(this.getPlayer(), newPage);
            }
            newPage.open(this.player);
        }
    }

    //Updates the Inventory to reflect the InventoryBuilder if it exists
    void update() {
        if(this.getInventory() != null) {
            for(int i = 0; i < this.getSize(); i++) {
                if(this.getItem(i) == null) {
                    this.getInventory().setItem(i, new ItemStack(Material.AIR));
                } else {
                    this.getInventory().setItem(i, this.items.get(i).getItem());
                }
            }
        }

    }

    //Updates the InventoryBuilder to reflect the Inventory.
    void updateBuilder() {
        for(int i = 0; i < this.getInventory().getSize(); i++) {
            if(this.getItem(i) == null && this.getInventory().getItem(i) == null) continue;

            if(this.getItem(i) == null || !this.getItem(i).getItem().isSimilar(this.getInventory().getItem(i))) {
                //Playerheads sometimes modify their internal MetaData, so it's possible that this was changed.
                //However, the Inventory doesn't need to reflect this internal change, so just continue.
                if(this.getInventory().getItem(i).getType() == Material.PLAYER_HEAD) continue;

                setItem(this.getInventory().getItem(i), i);
            } else {
                //Make sure the amounts are the same as well.
                if(this.getItem(i) != null) {
                    if(this.getItem(i).getAmount() != this.getInventory().getItem(i).getAmount()) {
                        this.getItem(i).setAmount(this.getInventory().getItem(i).getAmount());
                    }
                }
            }
        }
    }

    public static Map<Player, InventoryBuilder> getInventories() {
        return inventories;
    }

}
