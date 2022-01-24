package me.dessie.dessielib.storageapi.storage.format.mysql.struct.column;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;
import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types.IntType;
import org.apache.commons.lang3.RandomStringUtils;

public class Column {

    private final String name;
    private final DataType type;

    public Column(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public DataType getType() { return type; }

    public static Column newFakeColumn() {
        return new Column(RandomStringUtils.random(8, true, true), new IntType());
    }
}
