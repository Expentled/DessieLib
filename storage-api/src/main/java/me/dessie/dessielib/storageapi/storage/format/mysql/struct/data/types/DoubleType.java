package me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.struct.data.DataType;

import java.util.List;

public class DoubleType extends DataType {
    private final int digits;
    private final int decimals;

    public DoubleType(int digits, int decimals) {
        super("double");
        this.digits = digits;
        this.decimals = decimals;
    }

    public DoubleType() {
        super("double");
        this.digits = -1;
        this.decimals = -1;
    }

    public int getDigits() {
        return digits;
    }

    public int getDecimals() {
        return decimals;
    }

    @Override
    public List<Object> getOptions() {
        if(this.getDigits() == -1 || this.getDecimals() == -1) {
            return null;
        }
        return List.of(this.getDigits(), this.getDecimals());
    }
}
