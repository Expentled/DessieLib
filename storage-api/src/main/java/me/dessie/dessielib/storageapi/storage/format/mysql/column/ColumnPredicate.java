package me.dessie.dessielib.storageapi.storage.format.mysql.column;

import me.dessie.dessielib.storageapi.storage.format.mysql.table.Table;

/**
 * ColumnPredicates are used within the {@link me.dessie.dessielib.storageapi.storage.format.mysql.MySQLContainer}
 * within it's store, retrieve and delete methods.
 *
 * These are used as effective WHERE clauses within the statement.
 *
 * For example, if you pass in a Column Predicate that has "kills = 5", only rows that match that predicate would be affected.
 * Multiple ColumnPredicates can be added to further narrow down which rows are affected.
 *
 * @see me.dessie.dessielib.storageapi.storage.format.mysql.MySQLContainer#retrieve(Table, Column, ColumnPredicate...)
 * @see me.dessie.dessielib.storageapi.storage.format.mysql.MySQLContainer#store(Table, Column, Object, ColumnPredicate...)
 * @see me.dessie.dessielib.storageapi.storage.format.mysql.MySQLContainer#delete(Table, ColumnPredicate...)
 */
public record ColumnPredicate(Column column, Object data) {

    /**
     * @return The Column key
     */
    public Column getColumn() {
        return this.column;
    }

    /**
     * @return The value to check at that Column.
     */
    public Object getData() {
        return this.data;
    }
}
