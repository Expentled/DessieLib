package me.dessie.dessielib.core.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A Builder style class for creating Gson {@link JsonObject}s
 */
public class JsonObjectBuilder {

    private JsonObject object;

    /**
     * Creates an empty builder with an empty json object
     */
    public JsonObjectBuilder() {
        this.object = new JsonObject();
    }

    /**
     * Adds a {@link JsonElement} to the builder at the specified key
     *
     * @param key The key to add the JsonElement to
     * @param element The JsonElement to add
     * @return The JsonObjectBuilder instance for chaining.
     */
    public JsonObjectBuilder add(String key, JsonElement element) {
        this.object.add(key, element);
        return this;
    }

    /**
     * Adds a {@link String} to the builder at the specified key
     *
     * @param key The key to add the String to
     * @param property The String to add
     * @return The JsonObjectBuilder instance for chaining.
     */
    public JsonObjectBuilder add(String key, String property) {
        this.object.addProperty(key, property);
        return this;
    }

    /**
     * Adds a {@link Number} to the builder at the specified key
     *
     * @param key The key to add the Number to
     * @param property The Number to add
     * @return The JsonObjectBuilder instance for chaining.
     */
    public JsonObjectBuilder add(String key, Number property) {
        this.object.addProperty(key, property);
        return this;
    }

    /**
     * Adds a {@link Boolean} to the builder at the specified key
     *
     * @param key The key to add the Boolean to
     * @param property The Boolean to add
     * @return The JsonObjectBuilder instance for chaining.
     */
    public JsonObjectBuilder add(String key, Boolean property) {
        this.object.addProperty(key, property);
        return this;
    }

    /**
     * Adds a {@link Character} to the builder at the specified key
     *
     * @param key The key to add the Character to
     * @param property The Character to add
     * @return The JsonObjectBuilder instance for chaining.
     */
    public JsonObjectBuilder add(String key, Character property) {
        this.object.addProperty(key, property);
        return this;
    }

    /**
     * Builds the builder to get the resulting JsonObject
     *
     * @return The JsonObject from the builder
     */
    public JsonObject build() {
        return object;
    }
}
