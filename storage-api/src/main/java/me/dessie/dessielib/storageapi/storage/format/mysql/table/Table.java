package me.dessie.dessielib.storageapi.storage.format.mysql.table;

import me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer;
import me.dessie.dessielib.storageapi.storage.format.mysql.MySQLContainer;
import me.dessie.dessielib.storageapi.storage.format.mysql.column.Column;
import me.dessie.dessielib.storageapi.storage.format.mysql.column.ColumnPredicate;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents a MySQL Table for the MySQL Database.
 *
 * It stores numerous {@link Column}s where the data is stored, and can be used to interact with that data.
 */
public class Table {

    private final List<Column> columns = new ArrayList<>();
    private final String name;
    private final MySQLContainer container;

    /**
     * Creates a table inside the provided {@link MySQLContainer}.
     *
     * @param container The MySQLContainer that this table will be attached to
     * @param name The name of the table.
     * @throws IllegalArgumentException In the case of an invalid table name.
     */
    public Table(MySQLContainer container, String name) throws IllegalArgumentException {
        if(MySQLContainer.getInjectionPattern().matcher(name).find()) {
            throw new IllegalArgumentException("Name must contain only alphanumeric and underscore characters!");
        }
        this.container = container;
        this.name = name;
    }

    /**
     * Attempts to create the Table in the MySQL database.
     *
     * It is recommended to always call this method, to 100% ensure that the table exists.
     * If a table with the name of this table already exists, nothing will change.
     */
    public void createTable() {
        StringBuilder columns = new StringBuilder();
        for(Column column : this.getColumns()) {
            columns.append(column.getName())
                    .append(" ")
                    .append(column.getType().getType())
                    .append(column.getType().getOptions() != null ? "(" + StringUtils.join(column.getType().getOptions(), ',') + ")" : "")
                    .append(",");
        }

        columns.setLength(columns.length() - 1);

        try {
            this.getContainer().getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName() + " (" + columns + ")")
                    .executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a {@link Column} to the Table.
     *
     * @param column The Column to add.
     * @return The Table instance.
     */
    public Table addColumn(Column column) {
        this.columns.add(column);
        return this;
    }

    /**
     * Delegate method for {@link MySQLContainer#set(Table, Column, Object, ColumnPredicate...)}
     *
     * @param column The {@link Column} within the Table to set data to.
     * @param data The data to set.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void set(Column column, Object data, ColumnPredicate... predicates) {
        this.set(new HashMap<>() {{ put(column, data); }}, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#set(Table, Map, ColumnPredicate...)}
     *
     * @param columns A map of {@link Column} and the data to set into that column.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void set(Map<Column, Object> columns, ColumnPredicate... predicates) {
        this.getContainer().set(this, columns, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#store(Table, Column, Object, ColumnPredicate...)}
     *
     * @param column The {@link Column} within the Table to set data to.
     * @param data The data to set.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void store(Column column, Object data, ColumnPredicate... predicates) {
        this.store(new HashMap<>() {{ put(column, data); }}, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#store(Table, Map, ColumnPredicate...)}
     *
     * @param columns A map of {@link Column} and the data to set into that column.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void store(Map<Column, Object> columns, ColumnPredicate... predicates) {
        this.getContainer().store(this, columns, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#remove(Table, ColumnPredicate...)}
     *
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void remove(ColumnPredicate... predicates) {
        this.getContainer().remove(this, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#delete(Table, ColumnPredicate...)}
     *
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void delete(ColumnPredicate... predicates) {
        this.getContainer().delete(this, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#get(Table, Column, ColumnPredicate...)}
     *
     * @param column The {@link Column} to get data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The type to cast to
     * @return The cached object, or null if none exists at the path.
     */
    public <T> T get(Column column, ColumnPredicate... predicates) {
        return this.getContainer().get(this, column, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#retrieve(Table, Column, ColumnPredicate...)}
     *
     * @param column The {@link Column} to get data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The type to cast to
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> T retrieve(Column column, ColumnPredicate... predicates) {
        return this.getContainer().retrieve(this, column, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#retrieve(Class, Table, ColumnPredicate...)}
     *
     * @param type The type of {@link StorageDecomposer} to obtain. If no StorageComposer exists with this type, the method will throw an error.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The explicit type that will be cast to.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> T retrieve(Class<T> type, ColumnPredicate... predicates) {
        return this.getContainer().retrieve(type, this, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#retrieveAsync(Table, Column, ColumnPredicate...)}
     *
     * @param column The {@link Column} to get data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The explicit type that will be cast to.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveAsync(Column column, ColumnPredicate... predicates) {
        return this.getContainer().retrieveAsync(this, column, predicates);
    }

    /**
     * Delegate method for {@link MySQLContainer#retrieveAsync(Class, Table, ColumnPredicate...)}
     *
     * @param type The type of {@link StorageDecomposer} to obtain. If no StorageComposer exists with this type, the method will throw an error.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The explicit type that will be cast to.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveAsync(Class<T> type, ColumnPredicate... predicates) {
        return this.getContainer().retrieveAsync(type, this, predicates);
    }

    /**
     * Gets a registered {@link Column} by the specified name in the Table.
     *
     * @param name The name of the Column to get.
     * @return The Column with the specified name, or null if it doesn't exist.
     */
    public Column getColumn(String name) {return this.columns.stream().filter(column -> column.getName().equals(name)).findAny().orElse(null);}

    /**
     * @return All {@link Column}s in the Table
     */
    public List<Column> getColumns() {return columns;}

    /**
     * @return The name of the Table
     */
    public String getName() {return name;}

    /**
     * @return The {@link MySQLContainer} that this Table is contained in.
     */
    public MySQLContainer getContainer() {return container;}
}
