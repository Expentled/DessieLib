package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a VARCHAR, or String {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class StringType extends DataType{
    private final int length;

    /**
     * Creates a String Column type.
     * @param length The maximum length of String that can be stored.
     */
    public StringType(int length) {
        super("VARCHAR");
        this.length = length;
    }

    /**
     * @return The maximum length of String that can be stored.
     */
    public int getLength() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getOptions() {
        return List.of(this.getLength());
    }
}
