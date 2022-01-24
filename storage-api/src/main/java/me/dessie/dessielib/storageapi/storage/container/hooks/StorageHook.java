package me.dessie.dessielib.storageapi.storage.container.hooks;

public abstract class StorageHook {

    private Runnable complete;

    public StorageHook onComplete(Runnable runnable) {
        this.complete = runnable;
        return this;
    }

    public Runnable getComplete() {
        return complete;
    }

    public void complete() {
        if(this.getComplete() != null) this.getComplete().run();
    }
}
