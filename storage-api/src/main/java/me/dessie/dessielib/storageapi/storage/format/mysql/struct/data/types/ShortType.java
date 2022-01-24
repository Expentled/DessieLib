package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;

import java.util.List;

public class ShortType extends DataType {
    public ShortType() {
        super("SMALLINT");
    }

    @Override
    public List<Object> getOptions() {
        return null;
    }
}
