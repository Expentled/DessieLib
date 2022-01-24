package me.dessie.dessielib.storageapi.storage.container.hooks;

import java.util.function.Function;

public class RetrieveHook extends StorageHook {

    private final Function<String, Object> function;

    public RetrieveHook(Function<String, Object> function) {
        this.function = function;
    }

    public Function<String, Object> getFunction() {
        return function;
    }
}
