package me.dessie.dessielib.resourcepack.assets;

import me.dessie.dessielib.resourcepack.ResourcePackBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Defines the implementation for generating an asset for the resource pack.
 *
 * This class involves 2 phases of generation, one for initializing all the files (for example, creating necessary folders)
 * and a phase for generating necessary files for the asset. (such as a .json file).
 *
 * You can create and generate any file that is needed for your asset to properly function.
 *
 * @see Asset
 */
public abstract class AssetGenerator {

    /**
     * The first phase of generating an asset within a resource pack.
     * This method should be used to verify that all the necessary folders
     * have been created for the {@link AssetGenerator#generate(ResourcePackBuilder, List)} method.
     *
     * @param builder The ResourcePackBuilder that is initializing this asset
     * @param assets The assets that are being initialized.
     *               This list can be cast to your specific asset using {@link AssetGenerator#cast(Class, List)}
     * @throws IOException If an IOException occurs.
     */
    public abstract void init(ResourcePackBuilder builder, List<Asset> assets) throws IOException;

    /**
     * The second phase of generating assets within the resource pack.
     * This method generally will take care of generating necessary .json files.
     * Also should generate anything else that is necessary for the respective Asset to function.
     *
     * @param builder The ResourcePackBuilder that is generating this asset
     * @param assets The assets that are being generated.
     *               This list can be cast to your specific asset using {@link AssetGenerator#cast(Class, List)}
     * @throws IOException If an IOException occurs.
     */
    public abstract void generate(ResourcePackBuilder builder, List<Asset> assets) throws IOException;

    /**
     * Creates a parent directory and all non-existing subdirectories for all {@link File}s provided.
     *
     * @param files The directories to create.
     */
    public void createDirectories(File... files) {
        for(File file : files) {
            if(!file.exists()) {
                file.mkdirs();
            }
        }
    }

    /**
     * Casts your provided List of {@link Asset}s from {@link AssetGenerator#generate(ResourcePackBuilder, List)}
     * to a specific class that you want.
     *
     * @param type The Class type to cast to.
     * @param assets The assets to cast.
     * @param <V> The class type that must be of type {@link Asset}
     * @return The casted Assets.
     */
    public <V extends Asset> List<V> cast(Class<V> type, List<Asset> assets) {
        return assets.stream().map(type::cast).toList();
    }
}
