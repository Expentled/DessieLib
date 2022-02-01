package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a SMALLINT, or Short {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class ShortType extends DataType {

    /**
     * Creates a Short Column type
     */
    public ShortType() {
        super("SMALLINT");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getOptions() {
        return null;
    }
}
