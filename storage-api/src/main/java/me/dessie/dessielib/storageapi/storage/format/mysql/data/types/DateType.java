package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a Date {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class DateType extends DataType {

    /**
     * Creates a Date Column type
     */
    public DateType() {
        super("DATE");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getOptions() {
        return null;
    }
}
