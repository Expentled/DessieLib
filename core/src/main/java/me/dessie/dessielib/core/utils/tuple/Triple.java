package me.dessie.dessielib.core.utils.tuple;

public class Triple<L, M, R> {
    L left;
    M middle;
    R right;

    public Triple(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }
    public M getMiddle() {
        return middle;
    }
    public R getRight() {
        return right;
    }
}
