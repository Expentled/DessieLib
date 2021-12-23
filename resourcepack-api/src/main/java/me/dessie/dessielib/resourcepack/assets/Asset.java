package me.dessie.dessielib.resourcepack.assets;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.dessie.dessielib.resourcepack.ResourcePack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public abstract class Asset {
    private final String name;
    private final File resourcePackFolder;
    private final File assetsFolder;
    private final File namespaceFolder;
    private AssetGenerator generator;

    /**
     * @param name The name of the asset.
     */
    public Asset(String name) {
        this.name = name;
        this.resourcePackFolder = new File(ResourcePack.getPlugin().getDataFolder() + "/" + this.getNamespace());
        this.assetsFolder = new File(this.getResourcePackFolder(), "assets");
        this.namespaceFolder = new File(assetsFolder + "/" + this.getNamespace());
    }

    /**
     * @return The namespace folder of the resource pack.
     *         This folder is serverDir/plugins/pluginName/pluginName/assets/pluginName/
     */
    public File getNamespaceFolder() {return namespaceFolder;}

    /**
     * @return The namespace folder of the resource pack.
     *         This folder is serverDir/plugins/pluginName/pluginName/assets/
     */
    public File getAssetsFolder() {return assetsFolder;}

    /**
     * @return The namespace folder of the resource pack.
     *         This folder is serverDir/plugins/pluginName/pluginName/
     */
    public File getResourcePackFolder() {return resourcePackFolder;}

    /**
     * @return The name of the asset.
     */
    public String getName() { return name; }

    /**
     * @return The name of the Plugin that registered this Asset.
     */
    public String getNamespace() { return ResourcePack.getPlugin().getName().toLowerCase(); }

    /**
     * @return The {@link AssetGenerator} that will generate all the necessary files for this Asset.
     */
    public AssetGenerator getGenerator() {
        return generator;
    }

    /**
     * @param generator The {@link AssetGenerator} that will generate all the necessary files for this Asset.
     */
    public void setGenerator(AssetGenerator generator) {
        this.generator = generator;
    }

    /**
     * Writes a JsonObject to a File.
     * @param object The Object to write.
     * @param file The file to write to.
     */
    public static void write(JsonObject object, File file) {
        write(object, file, true);
    }

    /**
     * Writes a JsonObject to a File.
     * @param object The Object to write.
     * @param file The file to write to.
     * @param disableEscaping Whether to disable HTML escaping or not.
     */
    public static void write(JsonObject object, File file, boolean disableEscaping) {
        try {
            Writer writer = new FileWriter(file, StandardCharsets.UTF_8);
            GsonBuilder builder = new GsonBuilder()
                    .setPrettyPrinting();

            if(disableEscaping) {
                builder.disableHtmlEscaping();
            }

            builder.create()
                    .toJson(object, writer);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
