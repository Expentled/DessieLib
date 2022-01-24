package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;

import java.util.List;

public class ByteType extends DataType {
    public ByteType() {
        super("TINYINT");
    }

    @Override
    public List<Object> getOptions() {
        return null;
    }
}
