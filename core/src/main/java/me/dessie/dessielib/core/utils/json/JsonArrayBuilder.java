package me.dessie.dessielib.core.utils.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * A Builder style class for creating Gson {@link JsonArray}s
 */
public class JsonArrayBuilder {

    private JsonArray array;

    /**
     * Creates an empty builder with an empty array
     */
    public JsonArrayBuilder() {
        this.array = new JsonArray();
    }

    /**
     * Adds a {@link JsonElement} to the builder.
     *
     * @param property The JsonElement to add
     * @return The JsonArrayBuilder instance for chaining.
     */
    public JsonArrayBuilder add(JsonElement property) {
        array.add(property);
        return this;
    }

    /**
     * Adds a {@link String} to the builder.
     *
     * @param property The String to add
     * @return The JsonArrayBuilder instance for chaining.
     */
    public JsonArrayBuilder add(String property) {
        array.add(property);
        return this;
    }

    /**
     * Adds a {@link Number} to the builder.
     *
     * @param property The Number to add
     * @return The JsonArrayBuilder instance for chaining.
     */
    public JsonArrayBuilder add(Number property) {
        array.add(property);
        return this;
    }

    /**
     * Adds a {@link Character} to the builder.
     *
     * @param property The Character to add
     * @return The JsonArrayBuilder instance for chaining.
     */
    public JsonArrayBuilder add(Character property) {
        array.add(property);
        return this;
    }

    /**
     * Adds a {@link Boolean} to the builder.
     *
     * @param property The Boolean to add
     * @return The JsonArrayBuilder instance for chaining.
     */
    public JsonArrayBuilder add(Boolean property) {
        array.add(property);
        return this;
    }

    /**
     * Builds the builder to get the resulting JsonArray
     *
     * @return The JsonArray from the builder
     */
    public JsonArray build() {
        return array;
    }
}
