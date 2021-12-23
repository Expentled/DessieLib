package me.dessie.dessielib.resourcepack.assets;

import org.bukkit.Material;

import java.io.File;
import java.util.Locale;

public class TextureAsset {

    private String key;
    private String name;
    private File textureFile;

    /**
     * @param key The texture key
     *            For example, full blocks (such as dirt) have a key of "all".
     *            To find the keys for the block you're texturing, you may need to
     *            look within the vanilla resource pack.
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
     *            For example, full blocks (such as dirt) have a key of "all".
     *            To find the keys for the block you're texturing, you may need to
     *            look within the vanilla resource pack.
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
     *            For example, full blocks (such as dirt) have a key of "all".
     *            To find the keys for the block you're texturing, you may need to
     *            look within the vanilla resource pack.
     * @param texture The texture value, useful if you want to reference a vanilla texture such as "minecraft:block/dirt"
     */
    public TextureAsset(String key, String texture) {
        this.key = key;
        this.name = texture;

        //No file to get.
        this.textureFile = null;
    }

    public String getKey() {
        return key;
    }
    public String getName() {
        return name;
    }
    public File getTextureFile() {
        return textureFile;
    }

}
