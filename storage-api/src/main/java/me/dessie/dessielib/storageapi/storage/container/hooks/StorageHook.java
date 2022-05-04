package me.dessie.dessielib.storageapi.storage.container.hooks;

public abstract class StorageHook<T extends StorageHook<T>> {
    private Runnable complete;

    @SuppressWarnings("unchecked")
    public T onComplete(Runnable runnable) {
        this.complete = runnable;
        return (T) this;
    }

    public Runnable getComplete() {
        return complete;
    }

    public void complete() {
        if(this.getComplete() != null) this.getComplete().run();
    }
}
