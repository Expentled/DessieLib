package me.dessie.dessielib.storageapi.storage.container.hooks;

/**
 * Provides a generic interface for interacting with a {@link me.dessie.dessielib.storageapi.storage.container.StorageContainer}
 *
 * @see StoreHook
 * @see RetrieveHook
 * @see DeleteHook
 *
 * @param <T> A StorageHook type
 */
public abstract class StorageHook<T extends StorageHook<T>> {
    private Runnable complete;

    /**
     * Determines a {@link Runnable} that will run when a container call has completed.
     * Usually, this will be saving the file or data structure.
     *
     * @param runnable The runnable
     * @return This StorageHook instance, for chaining purposes.
     */
    @SuppressWarnings("unchecked")
    public T onComplete(Runnable runnable) {
        this.complete = runnable;
        return (T) this;
    }

    /**
     * Returns the complete runnable
     *
     * @see StorageHook#onComplete(Runnable)
     * @return The complete runnable
     */
    public Runnable getComplete() {
        return complete;
    }

    /**
     * Runs the complete runnable if it exists
     */
    public void complete() {
        if(this.getComplete() != null) this.getComplete().run();
    }
}
