package me.dessie.dessielib.storageapi.storage.container.decomposition;

import me.dessie.dessielib.storageapi.storage.container.StorageContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StorageDecomposer<T> {

    private final Class<T> type;
    private final Function<T, DecomposedObject> decomposeFunction;
    private final BiFunction<StorageContainer, RecomposedObject<T>, CompletableFuture<T>> recomposeFunction;

    public StorageDecomposer(Class<T> type, Function<T, DecomposedObject> decomposeFunction) {
        this(type, decomposeFunction, null);
    }

    public StorageDecomposer(Class<T> type, Function<T, DecomposedObject> decomposeFunction, BiFunction<StorageContainer, RecomposedObject<T>, CompletableFuture<T>> recomposeFunction) {
        this.type = type;
        this.decomposeFunction = decomposeFunction;
        this.recomposeFunction = recomposeFunction;
    }

    @SuppressWarnings("unchecked")
    public DecomposedObject applyDecompose(Object object) throws ClassCastException {
        return this.getDecomposeFunction().apply((T) object);
    }

    public CompletableFuture<T> applyRecompose(StorageContainer container, String path) {
        if(this.getRecomposeFunction() == null) return null;
        RecomposedObject<T> recomposedObject = new RecomposedObject<>();
        CompletableFuture<T> completed = this.getRecomposeFunction().apply(container, recomposedObject);

        List<CompletableFuture<?>> composedFutures = new ArrayList<>();
        for(String compose : recomposedObject.getRecomposedMap().keySet()) {
            String composedPath = path.replace("%path%", compose);

            if(container.isCached(composedPath)) {
                //Get the cached object
                T cached = container.get(composedPath);

                //Add the composed future with the cached object.
                composedFutures.add(CompletableFuture.completedFuture(cached));

                //Make sure the completed path is also notified of this completion, since we're not actually completing via retrieve.
                recomposedObject.getCompletedPath().get(compose).complete(cached);
            } else {
                CompletableFuture<Object> future = recomposedObject.getRecomposedMap().get(compose).apply(composedPath);
                composedFutures.add(future);

                //Cache the object once its returned.
                future.thenAccept(obj -> {
                    container.cache(composedPath, obj);
                });
            }
        }

        CompletableFuture.allOf(composedFutures.toArray(new CompletableFuture<?>[]{})).thenRun(() -> {
            completed.complete(recomposedObject.complete());
        });

        return completed;
    }

    public Class<T> getType() {
        return type;
    }
    public Function<T, DecomposedObject> getDecomposeFunction() {return decomposeFunction;}
    public BiFunction<StorageContainer, RecomposedObject<T>, CompletableFuture<T>> getRecomposeFunction() {return recomposeFunction;}
}
