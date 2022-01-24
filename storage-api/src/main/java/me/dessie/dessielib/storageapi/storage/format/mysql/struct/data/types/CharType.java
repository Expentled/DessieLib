package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;

import java.util.List;

public class CharType extends DataType {
    private final int length;

    public CharType(int length) {
        super("CHAR");
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
