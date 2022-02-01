package me.dessie.dessielib.storageapi.storage.format.mysql.data;

import java.util.List;

/**
 * A DataType is used to specify which type of data a {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 * should be made with in the SQL Database.
 *
 * This class also will accept optional arguments that will be passed when creating the column.
 */
public abstract class DataType {
    private final String type;

    /**
     * Creates the DataType with the specified Database data type.
     * For example, VARCHAR or INT.
     *
     * @param type The database Column type.
     */
    public DataType(String type) {
        this.type = type;
    }

    /**
     * @return The database column type.
     */
    public String getType() {
        return type;
    }

    /**
     * The optional options to pass along with the data type when creating the column.
     * For example, this may be VARCHAR's length option.
     *
     * These are parsed automatically when creating the table.
     *
     * Can be null if no options should be provided.
     *
     * @return The List of Options that should be used.
     */
    public abstract List<Object> getOptions();
}
