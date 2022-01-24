package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;

import java.util.List;

public class DateType extends DataType {
    public DateType() {
        super("DATE");
    }

    @Override
    public List<Object> getOptions() {
        return null;
    }
}
