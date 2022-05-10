package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a Integer {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class IntType extends DataType {

    /**
     * Creates a Date Column type
     */
    public IntType() {
        super("INT");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getOptions() {
        return null;
    }
}
