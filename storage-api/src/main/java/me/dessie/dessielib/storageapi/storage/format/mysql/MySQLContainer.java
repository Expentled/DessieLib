package me.dessie.dessielib.storageapi.storage.format.mysql;

import me.dessie.dessielib.storageapi.StorageAPI;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.container.decomposition.DecomposedObject;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.format.mysql.struct.column.Column;
import me.dessie.dessielib.storageapi.storage.format.mysql.struct.column.ColumnPredicate;
import me.dessie.dessielib.storageapi.storage.format.mysql.struct.table.Table;
import org.bukkit.Bukkit;
import oshi.util.tuples.Pair;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class MySQLContainer extends StorageContainer {

    //Pattern that tests for non-alphanumeric or underscore characters.
    private final static Pattern injection = Pattern.compile("\\W+");

    private final Connection connection;
    private final List<Table> tables = new ArrayList<>();

    public MySQLContainer(String database, String address, int port, String username, String password) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/" + database, username, password);
    }

    public void set(Table table, Column column, Object data, ColumnPredicate... predicates) {
        this.set(table, new HashMap<>() {{ put(column, data); }}, predicates);
    }

    public void set(Table table, Map<Column, Object> columns, ColumnPredicate... predicates) {
        Pair<String, Object[]> path = createPath(table, columns, predicates);
        this.set(path.getA(), path.getB());
    }

    public void store(Table table, Column column, Object data, ColumnPredicate... predicates) throws IOException {
        this.store(table, new HashMap<>() {{ put(column, data); }}, predicates);
    }

    public void store(Table table, Map<Column, Object> columns, ColumnPredicate... predicates) throws IOException {
        Pair<String, Object[]> path = createPath(table, columns, predicates);
        super.store(path.getA(), path.getB());
    }

    public void remove(Table table, ColumnPredicate... predicates) {
        Pair<String, Object[]> path = createPath(table, null, predicates);
        super.remove(path.getA());
    }

    public void delete(Table table, ColumnPredicate... predicates) throws IOException {
        Pair<String, Object[]> path = createPath(table, null, predicates);
        super.delete(path.getA());
    }

    public <T> T get(Table table, Column column, ColumnPredicate... predicates) {
        Pair<String, Object[]> path = createPath(table, new HashMap<>() {{ put(column, 0); }}, predicates);
        return super.get(path.getA());
    }

    public <T> CompletableFuture<T> retrieve(Table table, Column column, ColumnPredicate... predicates) {
        Pair<String, Object[]> path = createPath(table, new HashMap<>() {{ put(column, 0); }}, predicates);
        return super.retrieve(path.getA());
    }

    public <T> CompletableFuture<T> retrieve(Class<T> type, Table table, ColumnPredicate... predicates) {
        if(StorageContainer.getDecomposer(type) != null) {
            StringBuilder predicateBuilder = new StringBuilder();
            for(ColumnPredicate predicate : predicates) {
                predicateBuilder.append(predicate.column().getName()).append("=").append(predicate.getData()).append(";");
            }

            predicateBuilder.setLength(predicateBuilder.length() - 1);
            return super.retrieve(type, table.getName() + "." + predicateBuilder + "#%path%");
        } else throw new IllegalArgumentException("A decomposer for type " + type.getSimpleName() + " does not exist.");
    }

    @Override
    protected StoreHook storeHook() {
        return new StoreHook((path, data) -> {
            //Only accept if the path has paths and the data is an array of objects.
            if(!path.contains(".") || !path.contains("#") || !(data instanceof Object[] objects)) throw new IllegalArgumentException("Path should be in the form of table.column=value;column=value#columntoset,columntoset");

            String table;
            Map<String, Object> mappedPath = new HashMap<>();
            Map<String, String> predicates = new HashMap<>();

            //Get the clause, which is the table name, and primary key column and value.
            String clause = path.split("#")[0];
            String toSet = path.split("#")[1];

            //Get all the columns that we're going to set.
            //For example, uuid,kills,deaths
            String[] paths = (toSet.contains(",") ? toSet.split(",") : new String[] { toSet });

            //Map these to the object array.
            //For example, [1ea..., 5, 3]
            //So the mappedPath should be, {uuid=1ea..., kills=5, deaths=3}
            for(int i = 0; i < paths.length; i++) {
                mappedPath.put(paths[i], objects[i]);
            }

            //Map the predicates, for the WHERE clause.
            String[] splitClause = clause.split("\\.");
            table = splitClause[0];
            for (String predicate : splitClause[1].split(";")) {
                predicates.put(predicate.split("=")[0], predicate.split("=")[1]);
            }

            //Check if that row exists
            try {
                PreparedStatement query;
                StringBuilder builder = new StringBuilder();
                List<String> predicateKeys = new ArrayList<>(predicates.keySet());
                List<String> keys = new ArrayList<>(mappedPath.keySet());

                if (!rowExists(table, predicates)) {
                    //The row does not exist, so we should insert.
                    //These also should be within the path if we're inserting.
                    predicateKeys.forEach(predicate -> {
                        mappedPath.putIfAbsent(predicate, predicates.get(predicate));
                        if(!keys.contains(predicate)) {
                            keys.add(predicate);
                        }
                    });

                    //Build the query
                    keys.forEach(s -> {
                        builder.append(s).append(",");
                    });
                    builder.setLength(builder.length() - 1);

                    String params = "?,".repeat(mappedPath.size());
                    params = params.substring(0, params.length() - 1);

                    query = this.getConnection().prepareStatement("INSERT INTO " + table + "(" + builder + ") VALUES (" + params + ")");

                    //Fill the parameters.
                    for (int i = 0; i < keys.size(); i++) {
                        query.setObject(i + 1, mappedPath.get(keys.get(i)));
                    }
                } else {
                    //The row does exist, so we should update.
                    //Don't update if no predicates were provided.
                    if(predicates.size() == 0) throw new IllegalStateException("No predicates provided for update statement!");

                    //Build the predicate statement.
                    String predicateBuilder = createPredicateString(predicateKeys);

                    keys.forEach(s -> {
                        builder.append(s).append("=?,");
                    });
                    builder.setLength(builder.length() - 1);

                    //If it does exist, update the table.
                    query = this.getConnection().prepareStatement("UPDATE " + table + " SET " + builder + " WHERE " + predicateBuilder);

                    //Fill the parameters.
                    int i;
                    for (i = 0; i < keys.size(); i++) {
                        query.setObject(i + 1, mappedPath.get(keys.get(i)));
                    }

                    for(String key : predicateKeys) {
                        query.setString(++i, predicates.get(key));
                    }
                }

                query.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected DeleteHook deleteHook() {
        return new DeleteHook(path -> {
            if(!path.contains(".")) throw new IllegalArgumentException("Path should be in the form of table.column=value;column=value");

            String table = path.split("\\.")[0];
            Map<String, String> predicates = new HashMap<>();
            List<String> predicateKeys;
            PreparedStatement statement;
            String builder;

            for (String predicate : path.split("\\.")[1].split(";")) {
                predicates.put(predicate.split("=")[0], predicate.split("=")[1]);
            }

            predicateKeys = new ArrayList<>(predicates.keySet());
            builder = createPredicateString(predicateKeys);
            try {
                statement = this.getConnection().prepareStatement("DELETE FROM " + table + " WHERE " + builder);
                for(int i = 0; i < predicateKeys.size(); i++) {
                    statement.setString(i + 1, predicates.get(predicateKeys.get(i)));
                }

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected RetrieveHook retrieveHook() {
        return new RetrieveHook(path -> {
            if(!path.contains(".") || !path.contains("#")) throw new IllegalArgumentException("Path should be in the form of table.column=value;column=value#columntoget");

            String table = path.split("\\.")[0];
            String clause = path.split("\\.")[1];
            Map<String, String> predicates = new HashMap<>();
            String column = clause.split("#")[1];
            PreparedStatement statement;
            List<String> predicateKeys;
            ResultSet results;

            for(String predicate : clause.split("#")[0].split(";")) {
                predicates.put(predicate.split("=")[0], predicate.split("=")[1]);
            }

            predicateKeys = new ArrayList<>(predicates.keySet());

            try {
                statement = this.getConnection().prepareStatement("SELECT " + column + " FROM " + table + " WHERE " + createPredicateString(predicateKeys));
                for(int i = 0; i < predicateKeys.size(); i++) {
                    statement.setString(i + 1, predicates.get(predicateKeys.get(i)));
                }

                results = statement.executeQuery();

                if(results != null && results.next()) {
                    return results.getObject(column);
                } else return null;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //How
            return null;
        });
    }

    private Pair<String, Object[]> createPath(Table table, Map<Column, Object> columns, ColumnPredicate... predicates) {
        StringBuilder path = new StringBuilder(table.getName() + ".");
        for(ColumnPredicate predicate : predicates) {
            path.append(predicate.getColumn().getName()).append("=").append(predicate.getData().toString()).append(";");
        }
        path.setLength(path.length() - 1);

        if(columns != null) {
            path.append("#");
            List<Object> data = new ArrayList<>();
            for(Column column : columns.keySet()) {
                if(StorageContainer.getDecomposer(columns.get(column).getClass()) != null) {
                    DecomposedObject decomposedObject = StorageContainer.getDecomposer(columns.get(column).getClass()).applyDecompose(columns.get(column));
                    for(String s : decomposedObject.getDecomposedMap().keySet()) {
                        path.append(s).append(",");
                        data.add(decomposedObject.getDecomposedMap().get(s));
                    }
                } else {
                    path.append(column.getName()).append(",");
                    data.add(columns.get(column));
                }
            }
            path.setLength(path.length() - 1);

            return new Pair<>(path.toString(), data.toArray());
        }

        return new Pair<>(path.toString(), null);
    }

    public boolean rowExists(String table, Map<String, String> predicates) throws SQLException {
        List<String> predicateKeys = new ArrayList<>(predicates.keySet());
        String predicateBuilder = createPredicateString(predicateKeys);

        int index = 1;
        PreparedStatement statement = this.getConnection().prepareStatement("SELECT EXISTS(SELECT * FROM " + table + " WHERE " + predicateBuilder + ") AS result");

        for(String key : predicateKeys) {
            statement.setString(index++, predicates.get(key));
        }

        ResultSet results = statement.executeQuery();
        return results.next() && results.getBoolean("result");
    }

    private String createPredicateString(List<String> predicates) {
        StringBuilder builder = new StringBuilder();
        for(String key : predicates) {
            builder.append(key).append("=?").append(" AND ");
        }
        builder.setLength(Math.max(0, builder.length() - 5));

        return builder.toString();
    }

    /**
     * @param path The path of the data.
     * @param data The data to set.
     *
     * @deprecated Use {@link MySQLContainer#store(Table, Column, Object, ColumnPredicate...)} to store objects using this container type.
     */
    @Deprecated
    @Override
    public void store(String path, Object data) throws IOException {
        super.store(path, data);
    }

    /**
     * @param path The path of the data.
     * @param data The data to set.
     *
     * @deprecated Use {@link MySQLContainer#set(Table, Column, Object, ColumnPredicate...)} to set objects using this container type.
     */
    @Deprecated
    @Override
    public void set(String path, Object data) {
        super.set(path, data);
    }

    public void executeSQL(String sql) {
        Bukkit.getScheduler().runTaskAsynchronously(StorageAPI.getPlugin(), () -> {
            try {
                this.getConnection().prepareStatement(sql).executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Connection getConnection() {
        return connection;
    }

    public List<Table> getTables() {return tables;}

    public static Pattern getInjectionPattern() {
        return injection;
    }

    public Table getTable(String name) {
        return this.getTables().stream().filter(table -> table.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public MySQLContainer addTable(Table table) {
        table.createTable();
        this.tables.add(table);
        return this;
    }
}
