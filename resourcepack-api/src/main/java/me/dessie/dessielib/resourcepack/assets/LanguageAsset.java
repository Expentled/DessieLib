package me.dessie.dessielib.resourcepack.assets;

import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to change the text of the client that is rendered.
 * Any language can be modified.
 */
public class LanguageAsset extends Asset {

    private final File langFolder;
    private final Map<String, String> lang = new HashMap<>();

    /**
     * Creates a LanguageAsset that replaces English.
     */
    public LanguageAsset() {
        this("en_us", "", "");
    }

    /**
     * @param key The key of the language attribute. You may need to look in the vanilla resource pack to find these.
     * @param value The new String value to render when the key is referenced.
     */
    public LanguageAsset(String key, String value) {
        this("en_us", key, value);
    }

    /**
     *
     * @param language The language this Asset applies to. For example, en_us
     * @param key The key of the language attribute. You may need to look in the vanilla resource pack to find these.
     * @param value The new String value to render when the key is referenced.
     */
    public LanguageAsset(String language, String key, String value) {
        super(language);
        this.add(key, value);

        this.langFolder = new File(this.getAssetsFolder() + "/minecraft/lang");

        this.setGenerator(new AssetGenerator() {
            @Override
            public void init(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<LanguageAsset> assets = this.cast(LanguageAsset.class, assetList);

                assets.stream().findAny().ifPresent(asset -> {
                    createDirectories(asset.getLangFolder());
                });
            }

            @Override
            public void generate(ResourcePackBuilder builder, List<Asset> assetList) throws IOException {
                List<LanguageAsset> assets = this.cast(LanguageAsset.class, assetList);

                Map<String, List<LanguageAsset>> language = new HashMap<>();

                //Sort the Asset files by their specific language folder, (en_us, es_LA, etc)
                for(LanguageAsset asset : assets) {
                    language.putIfAbsent(asset.getName(), new ArrayList<>());
                    language.get(asset.getName()).add(asset);
                }

                //For each language, create the JSON object and write it to its file.
                for(String lang : language.keySet()) {
                    JsonObjectBuilder object = new JsonObjectBuilder();
                    for(LanguageAsset asset : language.get(lang)) {
                        for(String key : asset.getLanguage().keySet()) {
                            object.add(key, asset.getLanguage().get(key));
                        }
                    }

                    assets.stream().findAny().ifPresent(asset -> {
                        write(object.getObject(), new File(asset.getLangFolder() + "/" + lang + ".json"));
                    });
                }
            }
        });
    }

    /**
     * @return The folder that contains the minecraft language resources.
     *         Resolves to serverDir/plugins/pluginName/assets/minecraft/lang
     */
    public File getLangFolder() {return langFolder;}

    /**
     * @return All the keys and values to replace in the resource pack.
     */
    public Map<String, String> getLanguage() {return lang;}

    /**
     * @param key The key of the language attribute. You may need to look in the vanilla resource pack to find these.
     * @param value The new String value to render when the key is referenced.
     * @return The LanguageAsset instance.
     */
    public LanguageAsset add(String key, String value) {
        this.getLanguage().put(key, value);
        return this;
    }

}
