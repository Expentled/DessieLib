package me.dessie.dessielib.storageapi.storage.format.flatfile;

import me.dessie.dessielib.storageapi.storage.container.ArrayContainer;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.container.settings.StorageSettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class YAMLContainer extends StorageContainer implements ArrayContainer {

    private final File yaml;
    private final YamlConfiguration configuration;

    public YAMLContainer(File yamlFile) {
        this(yamlFile, new StorageSettings());
    }

    public YAMLContainer(File yamlFile, StorageSettings settings) {
        super(settings);
        this.yaml = yamlFile;
        this.configuration = new YamlConfiguration();

        try {
            if(!this.getYaml().exists() && !this.getYaml().createNewFile()) {
                Bukkit.getLogger().severe("Unable to create YAML file " + this.getYaml().getName());
            }

            this.getConfiguration().load(this.getYaml());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public File getYaml() {
        return yaml;
    }
    public YamlConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    protected StoreHook storeHook() {
        return (StoreHook) new StoreHook((path, data) -> {
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
        return (DeleteHook) new DeleteHook((path) -> {
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
}
