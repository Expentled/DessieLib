package me.dessie.dessielib.storageapi.storage.format.mysql.data.types;

import me.dessie.dessielib.storageapi.storage.format.mysql.data.DataType;

import java.util.List;

/**
 * Represents a Character {@link me.dessie.dessielib.storageapi.storage.format.mysql.column.Column}
 */
public class CharType extends DataType {
    private final int length;

    /**
     * Creates a Character Column type
     * @param length The fixed length of the characters that are stored.
     */
    public CharType(int length) {
        super("CHAR");
        this.length = length;
    }

    /**
     * @return The fixed length of characters that are stored within the column.
     */
    public int getLength() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getOptions() {
        return List.of(this.getLength());
    }
}
