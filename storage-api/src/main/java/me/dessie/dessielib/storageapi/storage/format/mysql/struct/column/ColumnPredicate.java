package me.dessie.dessielib.storageapi.storage.format.mysql.struct.column;

public record ColumnPredicate(Column column, Object data) {
    public Column getColumn() {
        return this.column;
    }

    public Object getData() {
        return this.data;
    }
}
