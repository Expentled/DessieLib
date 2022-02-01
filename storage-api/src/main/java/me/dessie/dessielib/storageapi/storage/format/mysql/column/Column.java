package me.dessie.dessielib.storageapi.storage.format.mysql.column;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;
import me.dessie.dessielib.storageapi.storage.format.mysql.data.types.IntType;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * This class represents a MySQL Column within a {@link me.dessie.dessielib.storageapi.storage.format.mysql.table.Table}
 * The name and {@link DataType} will be used when the table is created.
 */
public record Column(String name,
                     DataType type) {

    /**
     * @return The name of the Column
     */
    public String getName() {
        return name;
    }

    /**
     * @return The DataType of the Column.
     */
    public DataType getType() {
        return type;
    }

    /**
     * Creates a fake Column that should not be added into a Table.
     * This Column can be used if a Column is requested, but you don't have one to give.
     *
     * @return A randomly generated Column instance.
     */
    public static Column newFakeColumn() {
        return new Column(RandomStringUtils.random(8, true, true), new IntType());
    }
}
