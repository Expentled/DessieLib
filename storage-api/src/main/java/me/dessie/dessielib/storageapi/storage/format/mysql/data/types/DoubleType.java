package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a Double {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class DoubleType extends DataType {
    private final int digits;
    private final int decimals;

    /**
     * Creates a Double Column type with the SQL default digits and decimal places.
     */
    public DoubleType() {
        super("double");
        this.digits = -1;
        this.decimals = -1;
    }

    /**
     * Creates a Double Column type
     * @param digits How many total digits are stored in the column.
     * @param decimals How many decimal places are stored in the column.
     */
    public DoubleType(int digits, int decimals) {
        super("double");
        this.digits = digits;
        this.decimals = decimals;
    }

    /**
     * @return How many total digits are stored in the column.
     */
    public int getDigits() {
        return digits;
    }

    /**
     * @return How many decimal places are stored in the column.
     */
    public int getDecimals() {
        return decimals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getOptions() {
        if(this.getDigits() == -1 || this.getDecimals() == -1) {
            return null;
        }
        return List.of(this.getDigits(), this.getDecimals());
    }
}
