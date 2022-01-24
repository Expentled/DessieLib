package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data;

import java.util.List;

public abstract class DataType {
    private final String type;

    public DataType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public abstract List<Object> getOptions();
}
