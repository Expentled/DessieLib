package me.dessie.dessielib.storageapi.storage.format.flatfile;

import me.dessie.dessielib.storageapi.storage.container.ArrayContainer;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.File;
import java.io.IOException;

/**
 * A {@link StorageContainer} that stores using YAML format using {@link YamlConfiguration}.
 */
public class YAMLContainer extends StorageContainer implements ArrayContainer {

    private final File yaml;
    private final YamlConfiguration configuration;

    /**
     * Creates a YAMLContainer that can be stored and retrieved from using the provided file.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param yamlFile The YAML {@link File} that will be used for this Container.
     */
    public YAMLContainer(File yamlFile) {
        this(yamlFile, new StorageSettings());
    }

    /**
     * Creates a YAMLContainer that can be stored and retrieved from using the provided file.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param yamlFile The YAML file that will be used for this Container.
     * @param settings The StorageSettings for this Container.
     */
    public YAMLContainer(File yamlFile, StorageSettings settings) {
        super(settings);
        this.yaml = yamlFile;
        this.configuration = new YamlConfiguration();

        try {
            //Create the file.
            if(this.getYaml().getParentFile() != null) {
                this.getYaml().getParentFile().mkdirs();
            }
            this.getYaml().createNewFile();

            if(this.getYaml().exists()) {
                this.getConfiguration().load(this.getYaml());
            } else {
                throw new IOException("Unable to create file " + this.getYaml().getName());
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The YAML {@link File} that is being used for the container.
     */
    public File getYaml() {
        return yaml;
    }

    /**
     * @return The {@link YamlConfiguration} instance.
     */
    public YamlConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    protected StoreHook storeHook() {
        return new StoreHook((path, data) -> {
            this.getConfiguration().set(path, data);
        }).onComplete(() -> {
            try {
                this.getConfiguration().save(this.getYaml());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected DeleteHook deleteHook() {
        return new DeleteHook((path) -> {
            this.getConfiguration().set(path, null);
        }).onComplete(() -> {
            try {
                this.getConfiguration().save(this.getYaml());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected RetrieveHook retrieveHook() {
        return new RetrieveHook(path -> this.getConfiguration().get(path));
    }

    @Override
    public boolean isSupported(Class<?> clazz) {
        return super.isSupported(clazz) || ConfigurationSerializable.class.isAssignableFrom(clazz);
    }
}
