package me.dessie.dessielib.resourcepack.assets;

import org.bukkit.Material;

import java.io.File;
import java.util.Locale;

/**
 * Represents a Texture for a {@link BlockAsset} or an {@link ItemAsset}
 */
public class TextureAsset {

    private String key;
    private String name;
    private File textureFile;

    /**
     * @param key The texture key
     *            This key behaves slightly differently depending on the type of Asset it's being applied to.
     *            For {@link BlockAsset}s:
     *              The key acts as the texture's block property.
     *              For example, full blocks (such as dirt) have a key of "all".
     *              To find the keys for the block you're texturing, you may need to
     *              look within the vanilla resource pack.
     *
     *            For {@link ItemAsset}s:
     *              The key acts as the texture name for the item.
     *              For example, if you want the texture to be applied to texture name "layer0", the key should be named "layer0".
     *              This key only matters if you're providing a model json to the ItemAsset, otherwise, the key will be named properly.
     *
     * @param texture The custom texture file, points to a custom texture that is copied into assets/custom/textures/block/ folder.
     */
    public TextureAsset(String key, File texture) {
        if(texture == null || !texture.exists()) throw new IllegalArgumentException("Texture is null or does not exist");

        this.key = key;
        this.name = texture.getName().split("\\.png")[0];
        this.textureFile = texture;
    }

    /**
     *
     * @param key The texture key
     *            This key behaves slightly differently depending on the type of Asset it's being applied to.
     *            For {@link BlockAsset}s:
     *              The key acts as the texture's block property.
     *              For example, full blocks (such as dirt) have a key of "all".
     *              To find the keys for the block you're texturing, you may need to
     *              look within the vanilla resource pack.
     *
     *            For {@link ItemAsset}s:
     *              The key acts as the texture name for the item.
     *              For example, if you want the texture to be applied to texture name "layer0", the key should be named "layer0".
     *              This key only matters if you're providing a model json to the ItemAsset, otherwise, the key will be named properly.
     *
     * @param material A {@link Material} to use as the Texture.
     */
    public TextureAsset(String key, Material material) {
        this.key = key;
        this.name = "minecraft:block/" + material.name().toLowerCase(Locale.ROOT);

        //No file to get.
        this.textureFile = null;
    }

    /**
     * @param key The texture key
     *            This key behaves slightly differently depending on the type of Asset it's being applied to.
     *            For {@link BlockAsset}s:
     *              The key acts as the texture's block property.
     *              For example, full blocks (such as dirt) have a key of "all".
     *              To find the keys for the block you're texturing, you may need to
     *              look within the vanilla resource pack.
     *
     *            For {@link ItemAsset}s:
     *              The key acts as the texture name for the item.
     *              For example, if you want the texture to be applied to texture name "layer0", the key should be named "layer0".
     *              This key only matters if you're providing a model json to the ItemAsset, otherwise, the key will be named properly.
     *
     * @param texture The texture value, useful if you want to reference a vanilla texture such as "minecraft:block/dirt"
     */
    public TextureAsset(String key, String texture) {
        this.key = key;
        this.name = texture;

        //No file to get.
        this.textureFile = null;
    }

    /**
     * Returns the key for this asset
     * @return The key
     */
    public String getKey() {return key;}

    /**
     * Returns the name of this asset
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the texture file for this asset
     * @return The texture file
     */
    public File getTextureFile() {
        return textureFile;
    }

}
