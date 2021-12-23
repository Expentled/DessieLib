## DessieLib
A generic Spigot API for making life easier

## :books: Requirements
- Java 17

## :newspaper: API

### :package: Installation / Download

To depend on DessieLib with a build manager, you'll need to also authenticate to GitHub Packages.

This involves generating an access token from [GitHub](https://github.com/settings/tokens)

#### Gradle
```groovy
maven {
  url "https://maven.pkg.github.com/dessie0/dessielib"
  credentials {
    username = "<your_github_username>"
    password = "<your_github_token>"
  }
}

dependencies {
  compileOnly 'me.dessie.dessielib:dessielib:1.2.0'
}
```

#### Maven
```xml
<dependencies>
  <dependency>
    <groupId>me.dessie.dessielib</groupId>
    <artifactId>dessielib</artifactId>
    <version>1.2.0</version>
  </dependency>
</dependencies>
```

### :ledger: Documentation

The complete JavaDocs can be found [here](https://dessie0.github.io/DessieLib/) <br><br>

### :iphone: Features

DessieLib provides many features that are cumbersome in CraftBukkit and Spigot, but easily contained within DessieLib.

- `InventoryAPI` is a powerful way to manage inventories without having to mess with events
- `ScoreboardAPI` used to create organized tablists & scoreboards without the hassle of dealing with teams.
- `ParticleAPI` can easily create complex particle patterns or shapes, and animate them dynamically.
- `EnchantmentAPI` can manage fully customizable Enchantments
- `ResourcepackAPI` Allows you to generate an entire resource pack by just dropping in files.
- `Packeteer` Allows you to listen for incoming and outgoing packets, and fire events for these packets.

<details>
<summary>Basic InventoryAPI Usage</summary>

```java
public class Main extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        InventoryAPI.register(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("testinventory")) {
            //Creates an ItemBuilder as 1 Diamond, named "Diamond" with AQUA color. 
            ItemBuilder item = new ItemBuilder(ItemBuilder.buildItem(Material.DIAMOND, 1, "&bDiamond"));
            //Does not allow the item to be picked up
            item.cancel();
            //When we click, the item will turn into this Emerald. Then back into the Diamond when we click again.
            item.cyclesWith(ItemBuilder.buildItem(Material.EMERALD, 1, "&aEmerald"));

            //When we click the item, we'll tell the player which item they clicked.
            item.onClick(((player, itemBuilder) -> {
                player.sendMessage("Clicked " + itemBuilder.getName() + "!");
            }));

            //Create the InventoryBuilder as a size of 9 and named "Test Inventory"
            InventoryBuilder inventoryBuilder = new InventoryBuilder(9, "Test Inventory");

            //Set the diamond item stack in slot 4
            inventoryBuilder.setItem(item, 4);

            //Tell the player they opened the inventory
            inventoryBuilder.onOpen((player, builder) -> {
                player.sendMessage("Opened " + builder.getName());
            });

            //Tell the player when they close the inventory
            inventoryBuilder.onClose((player, builder) -> {
                player.sendMessage("Closed " + builder.getName());
            });

            //Tell the player when the page changes
            inventoryBuilder.onPageChange((player, newPage) -> {
                player.sendMessage("Opening page " + newPage.getName());
            });

            //Add a new page to the Inventory with a size of 18 and name of "Page 2"
            inventoryBuilder.addPage(18, "Page 2");

            //Open the Inventory
            inventoryBuilder.open((Player) sender);
            return true;
        }
        return false;
    }
}
```
</details>

<details>
<summary>Basic ScoreboardAPI Usage</summary>

```java
public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        ScoreboardAPI.register(this);

        //Using a BukkitTask, we can get everyone's Scoreboard and update it with THEIR
        //information. Such as their current Ping and Location!

        //You can do this with any part of the Scoreboard, Header, Footer, Scores, Titles, etc.
        Bukkit.getScheduler().runTaskTimer(this, () -> {

            //Loop through all active boards
            for(ScoreboardAPI scoreboardAPI : ScoreboardAPI.getBoards()) {
                Player player = scoreboardAPI.getPlayer();

                //Get & set the footer string.
                String footer = "&6Current Ping: &a" + ((CraftPlayer) player).getHandle().ping
                        + " &2X: " + (int) player.getLocation().getX() + " &2Y: " + (int) player.getLocation().getY()
                        + " &2Z: " + (int) player.getLocation().getZ();
                scoreboardAPI.setTabFooter(footer);
            }
        }, 20, 20);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        //Get the player that joined
        Player player = event.getPlayer();

        //Create the new Scoreboard, the name is just the default title.
        ScoreboardAPI scoreboard = new ScoreboardAPI(player, "Scoreboard");

        //Add a line on the first score that says hello
        scoreboard.setLine("Hello, " + player.getName(), 0);

        //Add an empty line
        scoreboard.setLine("", 1);

        //Create a score animation. This will make the line on Score 2 go from 1, 2, 3, 4, 5 every half second (10 ticks)
        List<String> animation = Arrays.asList("&11", "&22", "&33", "&44", "&55");
        scoreboard.animateScore(animation, 10, 2);

        /*
        Create a title animation. This method will loop through each element in the array and display
        it every 20 ticks (1 second)
         */
        List<String> titleAnimation = Arrays.asList("&3Server &6Name", "&6Server &3Name");
        scoreboard.animateTitle(titleAnimation, 20);

        /*
        Add the teams to the Scoreboard, this is how we're going to organize our tablist!
        The name doesn't matter too much, but you will be using this later, so don't forget what it is!
        The Color white makes it so our player's names are white.
        The prefix is what appears before the Player's names.

        The weight is the ordering of the tablist. A lower weight means they're higher on the list.
        After team weight, players are organized alphabetically.
        */
        scoreboard.addTablistTeam("owner", ChatColor.WHITE, "&c&lOwner &f", 0);
        scoreboard.addTablistTeam("member", ChatColor.WHITE, "&7Member &f", 50);

        /*
        Set this player's team. This can be determined any way you want, and you can have up to 100 teams.
        We're using a basic permission to determine whether we add to owner or member.
         */
        if (player.hasPermission("tablist.owner")) {
            //Set their team
            scoreboard.setPlayerTeam("owner");

            //Player's on the "owner" team cannot be pushed around
            scoreboard.setCollidable(false);
        } else {
            //Set their team
            scoreboard.setPlayerTeam("member");
            //Players on the "member" team CAN be pushed around
            scoreboard.setCollidable(true);
        }

        //Set the criteria to health. This will update the number next to a player's name in tab
        //to accurately represent their current health.
        scoreboard.setTablistCriteria("health");
    }
}
```

</details>

<details>
<summary>Basic ParticleAPI Usage</summary>

```java
public class Main extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        //Register the API
        ParticleAPI.register(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("testparticle") && sender instanceof Player player) {

            //Create the CircleParticle with green Redstone, 50 particles and a radius of 2.
            CircleParticle particle = new CircleParticle(new ParticleData(Particle.REDSTONE, new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1))
                    , 50, 2);

            //Make the Particle animator forever (since 0 loops), and follow the player with a y offset.
            particle.setAnimator(new EntityFollowAnimation(player, 5, new Vector(0, 1, 0)));

            //Add a Block collider, such that when particles collide with blocks, they fly into the air
            particle.addCollider(new BlockCollider(block -> {
                FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0, 0.5), block.getBlockData());
                fallingBlock.setVelocity(new Vector(0, 2, 0));

                block.setType(Material.AIR);
            }, 5, false));

            //Slowly oscillate the circle to be bigger and smaller.
            particle.addTransform(new ParticleScale(TransformType.OSCILLATE, 10, (location, step) -> {
                return new Vector(step * 0.05, 0, step * 0.05);
            }));

            //Finally, display the particle to all players.
            particle.display(player.getLocation().add(0, 1,0));

            return true;
        }
        return false;
    }
}

```

</details>

<details>
<summary>Basic EnchantmentAPI Usage</summary>

```java
public class Main extends JavaPlugin implements CommandExecutor {

    private CEnchantment soulbound;

    @Override
    public void onEnable() {
        //Register the API
        CEnchantmentAPI.register(this);

        soulbound = new CEnchantment("Soulbound") //Instantiate the enchantment.
                .setMaxLevel(1) //Set the max level to 1.
                .setDisplayName("&6Soulbound") //Set how the lore is displayed.
                .setEnchantmentActivator(new EnchantmentActivator(Activator.ALL)) //Enchantment will always trigger, since it doesn't matter where it is in their inventory when they die.
                .setEnchantmentTarget(EnchantmentTarget.ALL) //Can be enchanted to all tools, armor and bows/crossbows.
                .addEnchantables(Material.FLOWER_POT) //We'll also allow Flower Pots to be enchanted with it
                .setCursed(false) //It's not a curse
                .setUsesRomanNumerals(false) // Use numbers instead of roman numerals
                .setEnchantProperties(new CEnchantProperties()
                        .setAsNormalEnchant() // Sets properties such that they're similar to default enchantments.
                        .setCanBeOnBook(false) //However, we can then say it cannot be on a book
                        .setCanEnchantWithTable(false)) // And cannot be enchanted using the enchanting table.

                //Add the logic for the enchantment.
                //This is executed when a Player dies.
                .onDeath((event, result) -> { 
                    if(!(event instanceof PlayerDeathEvent)) return;
                    event.getDrops().remove(result.getItem());

                    int slot = -1;
                    PlayerInventory inventory = ((Player) event.getEntity()).getInventory();
                    for(int i = 0; i < 41; i++) {
                        if(SlotEventHelper.isNullOrAir(inventory.getItem(i))) continue;
                        if(inventory.getItem(i).equals(result.getItem())) {
                            slot = i;
                            break;
                        }
                    }

                    int finalSlot = slot;
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        if (finalSlot == -1) return;
                        if (!SlotEventHelper.isNullOrAir(inventory.getItem(finalSlot))) {
                            inventory.addItem(result.getItem());
                        } else {
                            inventory.setItem(finalSlot, result.getItem());
                        }
                    }, 2);
                }); 
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("cenchant") && sender instanceof Player player) {
            soulbound.enchant(player.getInventory().getItemInMainHand(), 1);
        }
        return false;
    }
}
```

</details>

<details>
<summary>Basic ResourcePack API Usage</summary>

```java
public class Main extends JavaPlugin {

    private ResourcePack pack;
    
    @Override
    public void onEnable() {
        //Register the API
        ResourcePack.register(this);

        pack = new ResourcePackBuilder()
                .setIcon(new File(this.getDataFolder() + "/pack.png")) // Set the pack icon
                .setDescription("A test resource pack!") // Set the pack description
                .setDisplayName("&5Dessie's Pack") // Set the name of the pack

                //Add an asset so all sand looks like amethyst blocks
                .addAsset(new BlockAsset("crystal_sand", Material.SAND, null, new TextureAsset("all", Material.AMETHYST_BLOCK)))

                //Add a state specific asset, where a note block with the note of 3 looks like gold.
                .addAsset(new BlockStateAsset("golden_note_block", Material.NOTE_BLOCK, Material.GOLD_BLOCK)
                        .addPredicate("note", "3")
                        .addDrops(new ItemStack(Material.GOLD_BLOCK)) //If broken, it drops a gold block

                        //We then can add an event listener that triggers when the player right clicks this block
                        .addEventListener(PlayerInteractEvent.class, (asset, event) -> {
                            return event.getAction() == Action.RIGHT_CLICK_BLOCK && asset.blockMatches(event.getClickedBlock());
                        }, (event) -> {
                            //You can then use the event to do whatever you want
                            event.setCancelled(true);
                            event.getPlayer().sendMessage("No changing the golden one!");
                        })
                        //Make the block extremely difficult to destroy
                        .setStrength(100)
                        //Diamond and Netherite Pickaxe will mine it faster
                        .addPreferredItems(Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE)

                        //The gold block only drops if a diamond or netherite pickaxe is used.
                        .setPreferredItemRequiredToDrop(true))

                //Makes the Heart of the Sea ItemStack look like a gold ingot
                .addAsset(new ItemAsset(new File(this.getDataFolder() + "/textures/gold_ingot.png"), new ItemStack(Material.HEART_OF_THE_SEA)))
                //Creates a unicode asset that renders the apple
                .addAsset(new BitmapUnicodeAsset("apple", Material.APPLE))
                //Create the webhost for the pack so players receive it when they join.
                .createWebhost("localhost", 8080, true).build();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("testresource") && sender instanceof Player player) {

            //Look for all the ItemAssets in the pack, and give the Player it's ItemStack
            for(ItemAsset asset : this.getPack().getBuilder().getAssetsOf(ItemAsset.class)) {
                player.getInventory().addItem(asset.getItem());
            }

            //Look for all the Bitmap assets and send the unicode to the player.
            for(BitmapUnicodeAsset asset : this.getPack().getBuilder().getAssetsOf(BitmapUnicodeAsset.class)) {
                player.sendMessage(String.valueOf((char) asset.getUnicode()));
            }
            return true;
        }
        return false;
    }

    public ResourcePack getPack() {
        return pack;
    }
}
```

</details>

<details>
<summary>Basic Packeteer Usage</summary>

```java
public class Main extends JavaPlugin implements PacketListener {

    @Override
    public void onEnable() {
        //Register Packeteer
        Packeteer packeteer = Packeteer.register(this);
        packeteer.addListener(this); // Register the listener
    }

    @PacketeerHandler
    public void onPickup(ServerboundPickItemPacket packet, Player player) {
        //Now we can just use the packet as we would a normal event.
        //This event will fire if the player middle clicks an item that is in their inventory.
        Bukkit.getLogger().info(player.getName() + " picked item in slot " + packet.getSlot());
    }
}

```

</details>

DessieLib also features small API elements such as
- `ConfigManager`, which is used for basic external configuration loading
- `Base64`, which can read and write Base64 strings into `ItemStack`s or `List<ItemStack>`
- `LoopedRunnable` which will run a BukkitRunnable a set number of times before automatically stopping.
- `SoundUtil` which can grab the Break/Place sound of specific blocks.
- `Colors` which can parse both & and HEX formats.
 