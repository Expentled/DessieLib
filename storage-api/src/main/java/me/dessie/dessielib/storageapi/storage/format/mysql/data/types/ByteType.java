package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a Tinyint, or Byte {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class ByteType extends DataType {

    /**
     * Creates a Byte Column type
     */
    public ByteType() {
        super("TINYINT");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getOptions() {
        return null;
    }
}
