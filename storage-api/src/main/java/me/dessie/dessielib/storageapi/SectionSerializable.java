package me.dessie.dessielib.storageapi;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an instantiable {@link org.bukkit.configuration.ConfigurationSection} that can be serialized to YAML.
 */
@SerializableAs("SectionSerializable")
public class SectionSerializable extends MemoryConfiguration implements ConfigurationSerializable {

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return this.getKeys(false).stream().collect(Collectors.toMap(key -> key, this::get));
    }

    /**
     * Required method for re-obtaining serialized sections from the YAML file.
     * This is called internally, and should not manually be called.
     *
     * @param args The data from the YAML that should be deserialized.
     * @return The deserialized SectionSerializable instance.
     */
    public static SectionSerializable deserialize(@NotNull Map<String, Object> args) {
        SectionSerializable sectionSerializable = new SectionSerializable();

        for(String key : args.keySet()) {
            sectionSerializable.set(key, args.get(key));
        }

        return sectionSerializable;
    }
}
