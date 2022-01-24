package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;

import java.util.List;

public class TimestampType extends DataType {

    private final int fractionalSeconds;

    public TimestampType() {
        this(-1);
    }

    public TimestampType(int fractionalSeconds) {
        super("TIMESTAMP");
        this.fractionalSeconds = fractionalSeconds;
    }

    public int getFractionalSeconds() {
        return fractionalSeconds;
    }

    @Override
    public List<Object> getOptions() {
        if(this.getFractionalSeconds() == -1) return null;

        return List.of(this.getFractionalSeconds());
    }
}
