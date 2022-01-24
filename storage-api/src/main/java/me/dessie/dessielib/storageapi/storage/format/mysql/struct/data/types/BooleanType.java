package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;

import java.util.List;

public class BooleanType extends DataType {
    public BooleanType() {
        super("boolean");
    }

    @Override
    public List<Object> getOptions() {
        return null;
    }
}
