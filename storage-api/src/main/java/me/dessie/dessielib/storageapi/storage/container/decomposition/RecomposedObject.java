package me.dessie.dessielib.storageapi.storage.container.decomposition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RecomposedObject<T> {

    private final Map<String, Function<String, CompletableFuture<Object>>> recomposedMap = new HashMap<>();
    private Function<RecomposedObject<T>, T> completeFunction;
    private final Map<String, CompletableFuture<Object>> pathFutures = new HashMap<>();

    public RecomposedObject<T> addRecomposeKey(String path, Function<String, CompletableFuture<Object>> data) {
        this.getRecomposedMap().putIfAbsent(path, data.andThen(after -> {
            this.getCompletedPath().put(path, after);
            return after;
        }));

        //Add an empty completable future, in-case the path is cached.
        this.getCompletedPath().put(path, new CompletableFuture<>());

        return this;
    }

    public RecomposedObject<T> removeRecomposedKey(String path) {
        this.getRecomposedMap().remove(path);
        return this;
    }

    public CompletableFuture<T> onComplete(Function<RecomposedObject<T>, T> function) {
        this.completeFunction = function;
        return new CompletableFuture<>();
    }

    public T complete() {
        return this.completeFunction.apply(this);
    }

    public Object getCompletedObject(String path) {return pathFutures.get(path).join();}
    public Set<String> getKeys() { return this.getRecomposedMap().keySet(); }
    public Map<String, Function<String, CompletableFuture<Object>>> getRecomposedMap() {
        return recomposedMap;
    }
    public Map<String, CompletableFuture<Object>> getCompletedPath() {return pathFutures;}
}
