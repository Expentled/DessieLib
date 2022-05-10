package me.dessie.dessielib.core.utils.tuple;

/**
 * Holds 3 different types of objects into the same object.
 *
 * @param <L> The type of the left object
 * @param <M> The type of the middle object
 * @param <R> The type of the right object
 */
public class Triple<L, M, R> {
    L left;
    M middle;
    R right;

    /**
     * Creates a Triple with the provided objects.
     *
     * @param left The left object
     * @param middle The middle object
     * @param right The right object
     */
    public Triple(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    /**
     * Returns the left object
     * @return The left object
     */
    public L getLeft() {
        return left;
    }

    /**
     * Returns the middle object
     * @return The middle object
     */
    public M getMiddle() {
        return middle;
    }

    /**
     * Returns the right object
     * @return The right object
     */
    public R getRight() {
        return right;
    }
}
