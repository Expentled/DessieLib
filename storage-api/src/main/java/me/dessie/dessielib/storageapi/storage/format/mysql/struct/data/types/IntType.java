package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;

import java.util.List;

public class IntType extends DataType {
    public IntType() {
        super("INT");
    }

    @Override
    public List<Object> getOptions() {
        return null;
    }
}
