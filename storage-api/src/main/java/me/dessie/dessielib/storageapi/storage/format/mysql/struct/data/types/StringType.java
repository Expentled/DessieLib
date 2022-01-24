package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;

import java.util.List;

public class StringType extends DataType{
    private final int length;

    public StringType(int length) {
        super("VARCHAR");
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    @Override
    public List<Object> getOptions() {
        return List.of(this.getLength());
    }
}
