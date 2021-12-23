package me.dessie.dessielib.resourcepack.assets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dessie.dessielib.core.utils.json.JsonArrayBuilder;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates a model and texture for an ItemStack with a specific model ID attached to it.
 */
public class ItemAsset extends Asset {

    private final File texture;
    private final File modelJson;
    private final ItemStack item;
    private int customModelId;

    private final File resourceItemTextureFolder;
    private final File resourceItemModelFolder;
    private final File minecraftItemModelFolder;

    /**
     * @param texture The texture png that should be applied to the ItemStack.
     * @param item The {@link ItemStack} to apply the texture to.
     */
    public ItemAsset(File texture, ItemStack item) {
        this(texture, null, item);
    }

    /**
     *
     * @param texture The texture png that should be applied to the ItemStack.
     * @param modelJson A default model.json that applies to this ItemStack. Can be used if you're creating a 3D model.
     *                  Can also be null if the texture should be applied without a model.
     * @param item The {@link ItemStack} to apply the texture to.
     */
    public ItemAsset(File texture, File modelJson, ItemStack item) {
        this(Objects.requireNonNull(texture.getName()).split("\\.png")[0], texture, modelJson, item);
    }

    /**
     * @param name The name of the asset.
     * @param texture The texture png that should be applied to the ItemStack.
     * @param modelJson A default model.json that applies to this ItemStack. Can be used if you're creating a 3D model.
     *                  Can also be null if the texture should be applied without a model.
     * @param item The {@link ItemStack} to apply the texture to.
     */
    public ItemAsset(String name, File texture, @Nullable File modelJson, ItemStack item) {
        super(name);
        if(texture == null || !texture.exists()) throw new IllegalArgumentException("Texture is null or does not exist");
        if(item == null || item.getItemMeta() == null) throw new IllegalArgumentException("Item & ItemMeta cannot be null");

        this.texture = texture;
        this.item = item;
        this.modelJson = modelJson;

        this.resourceItemTextureFolder = new File(this.getNamespaceFolder() + "/textures/item");
        this.resourceItemModelFolder = new File(this.getNamespaceFolder() + "/models/item");
        this.minecraftItemModelFolder = new File(this.getAssetsFolder() + "/minecraft/models/item");

        this.setGenerator(new AssetGenerator() {
            @Override
            public void init(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<ItemAsset> assets = this.cast(ItemAsset.class, assetList);

                //Create the files.
                for(ItemAsset asset : assets) {
                    this.createDirectories(asset.getMinecraftItemModelFolder(), asset.getResourceItemModelFolder(), asset.getResourceItemTextureFolder());

                    //Copy the texture png file to it's proper folder.
                    FileUtils.copyFile(asset.getTexture(), new File(asset.getResourceItemTextureFolder() + "/" + asset.getTexture().getName()));
                }
            }

            @Override
            public void generate(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<ItemAsset> assets = this.cast(ItemAsset.class, assetList);

                //Generate the Minecraft overrides.
                //For example, create the file for stick.json to replace sticks.
                Map<Material, List<ItemAsset>> materials = new HashMap<>();

                AtomicInteger counter = new AtomicInteger();
                assets.forEach(asset -> {
                    materials.putIfAbsent(asset.getItem().getType(), new ArrayList<>());
                    materials.get(asset.getItem().getType()).add(asset);

                    //Set the Custom Model data for this item.
                    //If this is the first one, start at 0
                    if(counter.get() == 0) {
                        asset.setCustomModelId(1);
                    } else {
                        //Otherwise, increment it by 1
                        asset.setCustomModelId(assets.get(assets.size() - 1).getCustomModelId() + 1);
                    }
                    counter.getAndIncrement();
                });

                for(Material material : materials.keySet()) {
                    JsonArrayBuilder array = new JsonArrayBuilder();
                    for(ItemAsset asset : materials.get(material)) {
                        String materialName = asset.getItem().getType().name().toLowerCase();
                        File assetFile = new File(asset.getMinecraftItemModelFolder(), materialName + ".json");
                        //Generate the .json for the asset with the appropriate custom model id.
                        JsonObject object = new JsonObjectBuilder().add("parent", "minecraft:item/generated")
                                .add("textures", new JsonObjectBuilder().add("layer0", "minecraft:item/" + materialName).getObject())
                                .add("overrides", array
                                        .add(new JsonObjectBuilder().add("predicate", new JsonObjectBuilder()
                                                        .add("custom_model_data", asset.getCustomModelId()).getObject())
                                                .add("model", asset.getNamespace() + ":item/" + asset.getName().split("\\.png")[0]).getObject()).getArray())
                                .getObject();
                        //Save the JSON file.
                        write(object, assetFile);
                    }
                }


                for(ItemAsset asset : assets) {
                    File customModel = new File(asset.getResourceItemModelFolder(), asset.getName() + ".json");
                    if(asset.getModel() == null) {
                        JsonObject object = new JsonObjectBuilder().add("parent", "minecraft:item/generated")
                                .add("textures", new JsonObjectBuilder()
                                        .add("layer0", asset.getNamespace() + ":item/" + asset.getName()).getObject()).getObject();

                        write(object, customModel);
                    } else {
                        try {
                            //Load in the provided JSON file.
                            JsonObject modelJson = new JsonParser().parse(new FileReader(asset.getModel())).getAsJsonObject();
                            JsonObject textures = modelJson.get("textures").getAsJsonObject();

                            //Make sure the textures point to the correct .png textures.
                            for(Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                                if(!entry.getValue().getAsString().startsWith("minecraft:")) {
                                    textures.addProperty(entry.getKey(), asset.getNamespace() + ":item/" + asset.getName());
                                }
                            }

                            //Write the file.
                            write(modelJson, customModel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * Sets the custom model id of the ItemStack and asset.
     * If an ItemStack matches with the same material and model ID
     * are in-game, this asset will be used.
     *
     * @param customModelId The new model ID.
     */
    public void setCustomModelId(int customModelId) {
        this.customModelId = customModelId;

        ItemMeta meta = this.getItem().getItemMeta();
        meta.setCustomModelData(customModelId);
        this.getItem().setItemMeta(meta);
    }

    /**
     * @return The current model ID this asset is using.
     */
    public int getCustomModelId() {
        return customModelId;
    }

    /**
     * @return The texture file
     */
    public File getTexture() {return texture;}

    /**
     * Returns a copy of the ItemStack this asset references.
     * When giving your custom items to a Player, this method should be used
     * to guarantee the model is applied properly.
     *
     * @return A copy of the ItemStack that is used.
     */
    public ItemStack getItem() {
        ItemStack cloned = item.clone();
        ItemMeta meta = cloned.getItemMeta();
        meta.setCustomModelData(this.getCustomModelId());
        cloned.setItemMeta(meta);
        return cloned;
    }

    /**
     * @return The model json.
     */
    public File getModel() { return modelJson; }

    /**
     * @return The folder that contains the minecraft item model resources.
     *         Resolves to serverDir/plugins/pluginName/assets/minecraft/models/item
     */
    public File getMinecraftItemModelFolder() {return minecraftItemModelFolder;}

    /**
     * @return The folder that contains the namespaced item model resources.
     *         Resolves to serverDir/plugins/pluginName/assets/pluginName/models/item
     */
    public File getResourceItemModelFolder() {return resourceItemModelFolder;}

    /**
     * @return The folder that contains the namespaced item texture resources.
     *         Resolves to serverDir/plugins/pluginName/assets/pluginName/textures/item
     */
    public File getResourceItemTextureFolder() {return resourceItemTextureFolder;}
}
