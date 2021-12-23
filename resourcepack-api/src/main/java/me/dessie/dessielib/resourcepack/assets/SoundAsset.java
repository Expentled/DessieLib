package me.dessie.dessielib.resourcepack.assets;

import me.dessie.dessielib.core.utils.json.JsonArrayBuilder;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.SoundCategory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundAsset extends Asset {

    private String path;
    private File soundFile;
    private SoundCategory category;
    private boolean stream;

    private final File resourceSoundsFolder;

    /**
     * @param path The path name for the sound file.
     *             Examples:
     *             entity.enderman
     *             block.sand
     * @param source The SoundSource this sound plays from
     * @param soundFile The .ogg file to play when this sound is played.
     */
    public SoundAsset(String path, SoundCategory source, File soundFile) {
        this(path, source, false, soundFile);
    }

    /**
     *
     * @param path The path name for the sound file.
     *             Examples:
     *             entity.enderman
     *             block.sand
     * @param source The SoundSource this sound plays from
     * @param stream If the sound should be streamed from the File instead of played at once.
     *               Use this if your sound is long, such as a music disc.
     * @param soundFile The .ogg file to play when this sound is played.
     */
    public SoundAsset(String path, SoundCategory source, boolean stream, File soundFile) {
        super(soundFile.getName().split("\\.ogg")[0]);

        if(!FilenameUtils.getExtension(soundFile.getName()).equalsIgnoreCase("ogg")) {
            throw new IllegalArgumentException("Sound files must be in ogg format!");
        }

        this.resourceSoundsFolder = new File(this.getNamespaceFolder() + "/sounds");

        this.stream = stream;
        this.path = path;
        this.soundFile = soundFile;
        this.category = source;

        this.setGenerator(new AssetGenerator() {
            @Override
            public void init(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<SoundAsset> assets = this.cast(SoundAsset.class, assetList);

                for(SoundAsset asset : assets) {
                    this.createDirectories(asset.getResourceSoundsFolder());

                    String assetPath = asset.getPath().replace(".", "/");
                    //Copy the given Sound File to the proper path.
                    try {
                        FileUtils.copyFile(asset.getSoundFile(), new File(asset.getResourceSoundsFolder() + "/" + assetPath + "/" + asset.getSoundFile().getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void generate(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<SoundAsset> assets = this.cast(SoundAsset.class, assetList);

                JsonObjectBuilder object = new JsonObjectBuilder();

                Map<String, List<SoundAsset>> sounds = new HashMap<>();
                builder.getAssetsOf(SoundAsset.class).forEach(sound -> {
                    sounds.putIfAbsent(sound.getPath(), new ArrayList<>());
                    sounds.get(sound.getPath()).add(sound);
                });

                for(String path : sounds.keySet()) {
                    JsonArrayBuilder array = new JsonArrayBuilder();
                    for(SoundAsset asset : sounds.get(path)) {
                        String assetPath = asset.getPath().replace(".", "/");
                        object.add(asset.getPath(),
                                new JsonObjectBuilder().add("sounds", array
                                        .add(new JsonObjectBuilder()
                                                .add("name", asset.getNamespace() + ":" + assetPath + "/" + asset.getName())
                                                .add("stream", asset.isStreamed()).getObject())
                                        .getArray()).getObject());
                    }
                }
                assets.stream().findAny().ifPresent(asset -> {
                    write(object.getObject(), new File(asset.getNamespaceFolder() + "/sounds.json"));
                });
            }
        });
    }

    /**
     * @return If the sound asset is streamed.
     */
    public boolean isStreamed() {
        return stream;
    }

    /**
     * @return The {@link SoundCategory} that this sound will play from.
     */
    public SoundCategory getCategory() { return category; }

    /**
     * @return The File to the .ogg
     */
    public File getSoundFile() {return soundFile;}

    /**
     * @return The path that is used to play this sound in game.
     */
    public String getPath() {return path;}

    /**
     * @return The folder that contains the namespaced sound resources.
     *         Resolves to serverDir/plugins/pluginName/assets/pluginName/sounds
     */
    public File getResourceSoundsFolder() {return resourceSoundsFolder;}
}
