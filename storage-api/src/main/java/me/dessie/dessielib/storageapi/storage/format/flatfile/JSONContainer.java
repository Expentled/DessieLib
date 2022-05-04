package me.dessie.dessielib.storageapi.storage.format.flatfile;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import me.dessie.dessielib.core.utils.tuple.Pair;
import me.dessie.dessielib.storageapi.storage.container.RetrieveArrayContainer;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A {@link StorageContainer} that stores using JSON format using {@link Gson}.
 */
public class JSONContainer extends RetrieveArrayContainer<JsonArray, JsonObject> {

    private final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
    private final File json;
    private JsonObject object;

    /**
     * Creates a JSONContainer that can be stored and retrieved from using the provided file.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param jsonFile The JSON {@link File} that will be used for this Container.
     */
    public JSONContainer(File jsonFile) {
        this(jsonFile, new StorageSettings());
    }

    /**
     * Creates a JSONContainer that can be stored and retrieved from using the provided file.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param jsonFile The JSON {@link File} that will be used for this Container.
     * @param settings The StorageSettings for this Container.
     */
    public JSONContainer(File jsonFile, StorageSettings settings) {
        super(settings);
        this.json = jsonFile;

        try {
            //Create the file.
            if(this.getJson().getParentFile() != null) {
                this.getJson().getParentFile().mkdirs();
            }
            this.getJson().createNewFile();

            //If it's empty and exists, setup the basic object structure.
            if(this.getJson().exists() && this.getJson().length() == 0) {
                FileWriter writer = new FileWriter(this.getJson());
                writer.write("{}");
                writer.close();
            } else if(!this.getJson().exists()) {
                throw new IOException("Unable to find file " + this.getJson().getName());
            }

            this.object = JsonParser.parseReader(new FileReader(this.getJson())).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The JSON {@link File} that is being used for the container.
     */
    public File getJson() {
        return json;
    }

    /**
     * @return The {@link JsonObject} that Gson is using to parse JSON.
     */
    public JsonObject getObject() {
        return object;
    }

    /**
     * @return The Gson instance.
     */
    public Gson getGson() {
        return gson;
    }

    @Override
    protected StoreHook storeHook() {
        return new StoreHook((path, data) -> {
            String[] tree = path.split("\\.");
            if(this.getElement(String.join(".", Arrays.copyOfRange(tree, 0, tree.length - 1)), true) instanceof JsonObject object) {
                object.add(tree[tree.length - 1], this.getGson().toJsonTree(data));
            }
        }).onComplete(() -> {
            this.write();
        });
    }

    @Override
    protected RetrieveHook retrieveHook() {
        return new RetrieveHook(path -> {
            String[] tree = path.split("\\.");
            if(this.getElement(path, false) instanceof JsonObject object) {
                return retrieveCorrectly(object, tree[tree.length - 1]);
            }
            return null;
        });
    }

    @Override
    protected DeleteHook deleteHook() {
        return new DeleteHook(path -> {
            String[] tree = path.split("\\.");
            if(this.getElement(path, false) instanceof JsonObject object) {
                object.remove(tree[tree.length - 1]);
            }
        }).onComplete(this::write);
    }


    @Override
    protected BiConsumer<JsonArray, JsonObject> add() {
        return JsonArray::add;
    }

    @Override
    protected Stream<Object> getHandlerStream(JsonArray handler) {
        Stream.Builder<Object> builder = Stream.builder();
        handler.forEach(builder::add);
        return builder.build();
    }

    @Override
    protected Stream<String> getNestedKeys(JsonObject nested) {
        return nested.keySet().stream();
    }

    @Override
    protected boolean isHandler(Object object) {
        return object instanceof JsonArray;
    }

    @Override
    protected boolean isNested(Object object) {
        return object instanceof JsonObject;
    }

    @Override
    protected Object getObjectFromNested(JsonObject nested, String key) {
        return retrieveCorrectly(nested, key);
    }

    @Override
    protected Object getPrimitive(Object object) {
        return this.getGson().fromJson((JsonElement) object, Object.class);
    }

    @Override
    protected BiConsumer<JsonArray, List<Pair<String, Object>>> handleListObject() {
        return ((array, list) -> {
            JsonObject object = new JsonObject();

            for(Pair<String, Object> pair : list) {
                //If the path is null, then it's not a decomposer, so we just add that to the array directly.
                if(pair.getKey() == null) {
                    array.add(this.getGson().toJsonTree(pair.getValue()));
                    continue;
                }

                //Handle nested paths.
                if(pair.getKey().contains(".")) {
                    String[] tree = pair.getKey().split("\\.");
                    JsonObject temp = object;

                    for(int i = 0; i < tree.length; i++) {
                        String path = tree[i];

                        if (!(temp.has(path))) {
                            temp.add(path, new JsonObject());
                        }

                        if(i != tree.length - 1) {
                            temp = temp.getAsJsonObject(path);
                        } else {
                            temp.add(path, this.getGson().toJsonTree(pair.getValue()));
                        }
                    }
                    if(temp != object) {
                        object.add(tree[0], temp);
                    }
                } else {
                    object.add(pair.getKey(), this.getGson().toJsonTree(pair.getValue()));
                }
            }

            if(object.keySet().size() != 0) {
                array.add(object);
            }
        });
    }

    @Override
    protected JsonArray getStoreListHandler() {
        return new JsonArray();
    }

    @Override
    protected JsonArray getRetrieveListHandler(String path) {
        if(this.getElement(path, false) instanceof JsonArray array) return array;
        return new JsonArray();
    }

    private Object retrieveCorrectly(JsonObject object, String key) {
        Object retrieve = this.getGson().fromJson(object.get(key), Object.class);
        if(retrieve.getClass() == Double.class && !object.get(key).getAsString().contains(".")) {
            return object.get(key).getAsInt();
        } else if(retrieve instanceof LinkedTreeMap<?,?> || retrieve instanceof ArrayList) {
            return object.get(key);
        } else return retrieve;
    }

    private Map<String, JsonElement> getTree(String path, boolean create) {
        Map<String, JsonElement> tree = new LinkedHashMap<>();
        JsonElement current = this.getObject();
        String[] branches = path.split("\\.");

        for (String branch : branches) {
            if (!(current instanceof JsonObject object)) continue;

            if (!object.has(branch) && create) {
                object.add(branch, new JsonObject());
            }
            tree.put(branch, current);
            current = object.get(branch);
        }

        //Add the last branch.
        tree.put(branches[branches.length - 1], current);
        return tree;
    }

    private JsonElement getElement(String path, boolean create) {
        if(path.equals("")) return this.getObject();

        Map<String, JsonElement> tree = this.getTree(path, create);
        List<String> keys = new ArrayList<>(tree.keySet());

        return tree.get(keys.get(keys.size() - 1));
    }

    private void write() {
        try {
            FileWriter writer = new FileWriter(this.getJson());
            this.getGson().toJson(this.getObject(), writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isListSupported(Class<?> clazz) {
        return super.isListSupported(clazz) || StorageContainer.getDecomposer(clazz) != null;
    }
}
