package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a Time {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class TimeType extends DataType {

    private final int fractionalSeconds;

    /**
     * Creates a Time Column type with SQL default fractional seconds.
     */
    public TimeType() {
        this(-1);
    }

    /**
     * Creates a Time Column type with the fractional seconds option
     * @param fractionalSeconds How many decimal places to store to. (Max 6)
     */
    public TimeType(int fractionalSeconds) {
        super("TIME");
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
