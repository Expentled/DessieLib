package me.dessie.dessielib.resourcepack.assets;

import com.google.gson.JsonObject;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * Creates the pack.mcmeta and copies the pack.png for the resource pack.
 */
public class MetaAsset extends Asset {

    private final File mcmetaFile;

    private String description;
    private File icon;
    private int packFormat;

    /**
     * @param name The name of the asset.
     * @param description The description of the resource pack.
     * @param icon The pack icon File. Must be a square resolution and png, 128x128 is preferred.
     */
    public MetaAsset(String name, String description, File icon) {
        this(name, description, icon, 8);
    }

    /**
     * @param name The name of the asset.
     * @param description The description of the resource pack.
     * @param icon The pack icon File. Must be a square resolution and png, 128x128 is preferred.
     * @param packFormat The version of the resource pack.
     */
    public MetaAsset(String name, String description, File icon, int packFormat) {
        super(name);

        this.packFormat = packFormat;
        this.mcmetaFile = new File(this.getResourcePackFolder(), "pack.mcmeta");
        this.description = description;
        this.icon = icon;

        this.setGenerator(new AssetGenerator() {
            @Override
            public void init(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<MetaAsset> assets = this.cast(MetaAsset.class, assetList);

                if(assets.size() > 1) {
                    Bukkit.getLogger().log(Level.INFO, "Multiple MetaAssets were found. Only the first asset that was added will be generated.");
                }

                MetaAsset asset = assets.get(0);

                //Create the MCMETA file.
                asset.getMcmetaFile().createNewFile();

                //Attempt the save the Icon if it was provided.
                if (asset.getIcon() != null) {
                    FileUtils.copyFile(asset.getIcon(), new File(asset.getResourcePackFolder() + "/pack.png"));
                }
            }

            @Override
            public void generate(ResourcePackBuilder builder, List<Asset> assetList) {
                List<MetaAsset> assets = this.cast(MetaAsset.class, assetList);

                //Generate the JSON
                MetaAsset asset = assets.get(0);

                JsonObject object = new JsonObjectBuilder().add("pack", new JsonObjectBuilder()
                        .add("pack_format", asset.getPackFormat())
                        .add("description", asset.getDescription()).build()).build();

                write(object, asset.getMcmetaFile());
            }
        });

    }

    /**
     * @return The pack.mcmeta file.
     */
    public File getMcmetaFile() {
        return mcmetaFile;
    }

    /**
     * @return The pack format, default for 1.18 is 8.
     */
    public int getPackFormat() {
        return packFormat;
    }

    /**
     * @return The pack.png File.
     */
    public File getIcon() {
        return icon;
    }

    /**
     * @return The description of the resource pack.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the pack format to use.
     *
     * @param packFormat The pack format to set.
     */
    public void setPackFormat(int packFormat) {
        this.packFormat = packFormat;
    }
}
