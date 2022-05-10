package me.dessie.dessielib.resourcepack.assets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Creates a custom texture and model for a {@link Material} block.
 *
 * @see TextureAsset for information on how textures are passed.
 * @see BlockStateAsset for how to also use this to replace specific states.
 *
 */
public class BlockAsset extends Asset {

    private final List<TextureAsset> textures = new ArrayList<>();
    private final File modelJson;
    private final Material block;

    private final File resourceBlockTextureFolder;
    private final File resourceBlockModelFolder;
    private final File minecraftBlockModelFolder;

    //Determines if the Minecraft version of this item should be replaced.
    private boolean replaces;

    //The name of the file in the assets/minecraft folder.
    private final String replacementName;

    //The parent model type to apply to this Block
    //For example, "minecraft:block/fence_post" or "minecraft:block/template_anvil"
    //This also supports custom models.
    private String parentModelType;

    /**
     *
     * @param name The name of the asset.
     * @param block The {@link Material} this asset replaces.
     * @param modelJson A provided default model JSON to use.
     * @param textures The {@link TextureAsset}s
     */
    public BlockAsset(String name, Material block, @Nullable File modelJson, TextureAsset... textures) {
        this(name, block, block.name().toLowerCase(Locale.ROOT), modelJson, true, textures);
    }

    /**
     *
     * @param name The name of the asset.
     * @param block The {@link Material} this asset replaces.
     * @param modelJson A provided default model JSON to use.
     * @param replaceAll If this model replaces all states of this block
     *                   If this is false, you need to use BlockStateAssets to determine when this model is shown.
     * @param textures The {@link TextureAsset}s
     */
    public BlockAsset(String name, Material block, @Nullable File modelJson, boolean replaceAll, TextureAsset... textures) {
        this(name, block, block.name().toLowerCase(Locale.ROOT), modelJson, replaceAll, textures);
    }

    /**
     * @param name The name of the custom block asset.
     * @param block The {@link Material} this asset replaces
     * @param replacementName The name within the resource pack. Generally this can be ignored because it can
     *                        be assumed from the Material.
     *
     *                        However, in some cases, such as the side of a fence, you'll need to force the name to replace
     *                        "oak_fence_side".
     * @param modelJson A provided default model JSON
     * @param replaceAll If this model replaces all states of this block
     *                   If this is false, you need to use BlockStateAssets to determine when this model is shown.
     * @param textures The {@link TextureAsset}s
     */
    public BlockAsset(String name, Material block, String replacementName, @Nullable File modelJson, boolean replaceAll, TextureAsset... textures) {
        super(name);
        if(block == null || !block.isBlock()) throw new IllegalArgumentException("Block must not be null and must be type of Block!");

        this.textures.addAll(new ArrayList<>(Arrays.asList(textures)));
        this.block = block;
        this.replacementName = replacementName;
        this.modelJson = modelJson;
        this.replaces = replaceAll;

        //Define the files.
        this.resourceBlockTextureFolder = new File(this.getNamespaceFolder() + "/textures/block");
        this.resourceBlockModelFolder = new File(this.getNamespaceFolder() + "/models/block");
        this.minecraftBlockModelFolder = new File(this.getAssetsFolder() + "/minecraft/models/block");

        //Set the default parent.
        this.parentModelType = "minecraft:block/cube_all";

        this.setGenerator(new AssetGenerator() {
            @Override
            public void init(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<BlockAsset> assets = this.cast(BlockAsset.class, assetList);

                for(BlockAsset asset : assets) {
                    this.createDirectories(asset.getMinecraftBlockModelFolder(), asset.getResourceBlockModelFolder(), asset.getResourceBlockTextureFolder());

                    //Copy the textures
                    for(TextureAsset texture : asset.getTextures()) {
                        if(texture.getTextureFile() == null) continue;
                        FileUtils.copyFile(texture.getTextureFile(), new File(asset.getResourceBlockTextureFolder() + "/" + texture.getTextureFile().getName()));
                    }
                }
            }

            @Override
            public void generate(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {

                List<BlockAsset> assets = this.cast(BlockAsset.class, assetList);

                for(BlockAsset asset : assets) {
                    String fileName = asset.getReplacementName() + ".json";

                    //Create the Minecraft model replacement file.
                    //Only do this if all states should be overwritten.
                    //------------------------------------------------
                    if(asset.isReplaces()) {
                        File assetFile = new File(asset.getMinecraftBlockModelFolder(), fileName);
                        //Generate the .json for the asset with the appropriate custom model id.
                        JsonObject json = new JsonObjectBuilder().add("parent", asset.getNamespace() + ":block/" + asset.getName())
                                .build();
                        //Save the JSON file.
                        write(json, assetFile);
                    }
                    //------------------------------------------------

                    //Create or Copy the model file.
                    //------------------------------------------------
                    File customModel = new File(asset.getResourceBlockModelFolder(), asset.getName() + ".json");
                    if(asset.getModel() == null) {
                        JsonObjectBuilder textureObject = new JsonObjectBuilder();
                        for(TextureAsset texture : asset.getTextures()) {
                            //If the file is null, the texture is just whatever name they provided.
                            //If the file isn't null, we get the texture from its name.
                            if(texture.getTextureFile() == null) {
                                textureObject.add(texture.getKey(), texture.getName());
                            } else {
                                textureObject.add(texture.getKey(), asset.getNamespace() + ":block/" + texture.getName());
                            }
                        }

                        JsonObject json = new JsonObjectBuilder().add("parent", asset.getParentModel())
                                .add("textures", textureObject.build()).build();

                        write(json, customModel);
                    } else {
                        try {
                            //Load in the provided JSON file.
                            JsonObject modelJson = new JsonParser().parse(new FileReader(asset.getModel())).getAsJsonObject();
                            JsonObject textures = modelJson.get("textures").getAsJsonObject();

                            //Make sure the textures point to the correct .png textures.
                            for(Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                                //Remap to the custom namespace if it's not Minecraft.
                                if(!entry.getValue().getAsString().startsWith("minecraft:")) {
                                    textures.addProperty(entry.getKey(), asset.getNamespace() + ":block/" + asset.getName());
                                }
                            }

                            //Write the file.
                            write(modelJson, customModel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //------------------------------------------------
                }


            }
        });

    }

    /**
     * Sets if this block replaces all of its states or not.
     * If false, you'll need to use {@link BlockStateAsset}s to choose models.
     *
     * @param replaces If the texture replaces all the states.
     * @return The BlockAsset instance.
     */
    public BlockAsset setReplaces(boolean replaces) {
        this.replaces = replaces;
        return this;
    }

    /**
     * Sets the parent model type, for example
     * "minecraft:block/fence_post" or "minecraft:block/template_anvil"
     * You may need to look into the vanilla resource pack to view what model type you need.
     *
     * @param parentModelType The new parent model type.
     * @return The BlockAsset instance.
     */
    public BlockAsset setParentModelType(String parentModelType) {
        this.parentModelType = parentModelType;
        return this;
    }

    /**
     * @return The parent model type.
     */
    public String getParentModel() {return parentModelType;}

    /**
     * @return The model JSON for this asset.
     */
    public File getModel() {
        return modelJson;
    }

    /**
     * @return All the {@link TextureAsset}s for this block.
     */
    public List<TextureAsset> getTextures() {return textures;}

    /**
     * @return The {@link Material} this asset pertains to.
     */
    public Material getBlock() {
        return block;
    }

    /**
     * @return The file name that is used within the resource pack.
     */
    public String getReplacementName() {return replacementName;}

    /**
     * @return If all states are replaced.
     */
    public boolean isReplaces() {return replaces;}

    /**
     * @return The folder that contains the namespaced block model resources.
     *         Resolves to serverDir/plugins/pluginName/assets/pluginName/models/block
     */
    public File getResourceBlockModelFolder() {return resourceBlockModelFolder;}

    /**
     * @return The folder that contains the namespaced block model resources.
     *         Resolves to serverDir/plugins/pluginName/assets/pluginName/textures/block
     */
    public File getResourceBlockTextureFolder() {return resourceBlockTextureFolder;}

    /**
     * @return The folder that contains the minecraft block model resources.
     *         Resolves to serverDir/plugins/pluginName/assets/minecraft/models/block
     */
    public File getMinecraftBlockModelFolder() {return minecraftBlockModelFolder;}
}
