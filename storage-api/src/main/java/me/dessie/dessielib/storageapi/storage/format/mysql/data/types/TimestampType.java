package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a Timestamp {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class TimestampType extends DataType {

    private final int fractionalSeconds;

    /**
     * Creates a Timestamp Column type with SQL default fractional seconds.
     */
    public TimestampType() {
        this(-1);
    }

    /**
     * Creates a Timestamp Column type with the fractional seconds option
     * @param fractionalSeconds How many decimal places to store to. (Max 6)
     */
    public TimestampType(int fractionalSeconds) {
        super("TIMESTAMP");
        this.fractionalSeconds = fractionalSeconds;
    }

    /**
     * @return How many decimal places are stored to in this column.
     */
    public int getFractionalSeconds() {
        return fractionalSeconds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getOptions() {
        if(this.getFractionalSeconds() == -1) return null;

        return List.of(this.getFractionalSeconds());
    }
}
