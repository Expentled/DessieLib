package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a Boolean {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class BooleanType extends DataType {

    /**
     * Creates a Boolean Column type
     */
    public BooleanType() {
        super("boolean");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getOptions() {
        return null;
    }
}
