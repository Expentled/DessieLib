package me.dessie.dessielib.storageapi.storage.format.mysql;

import me.dessie.dessielib.core.utils.tuple.Pair;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.decomposition.DecomposedObject;
import me.dessie.dessielib.storageapi.storage.decomposition.StorageDecomposer;
import me.dessie.dessielib.storageapi.storage.format.mysql.column.Column;
import me.dessie.dessielib.storageapi.storage.format.mysql.column.ColumnPredicate;
import me.dessie.dessielib.storageapi.storage.format.mysql.table.Table;
import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * A {@link StorageContainer} that stores using a MySQL Database.
 *
 * Note: Using this Container will likely not be the most efficient, powerful, or fastest way
 * of utilizing a MySQL Database. This is for basic uses, but should generally be okay for most applications.
 */
public class MySQLContainer extends StorageContainer {

    //Pattern that tests for non-alphanumeric or underscore characters.
    private final static Pattern injection = Pattern.compile("\\W+");

    private final Connection connection;
    private final List<Table> tables = new ArrayList<>();

    /**
     * Creates a MySQLContainer that can be stored and retrieved from using the provided database credentials.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param database The name of the database to connect to.
     * @param address The IP address of the database.
     * @param port The port of the database.
     * @param username The username of the account to access the database with.
     * @param password The password of the account to access the database with.
     * @throws SQLException In the case that a connection cannot be made.
     */
    public MySQLContainer(String database, String address, int port, String username, String password) throws SQLException {
        this(database, address, port, username, password, new StorageSettings());
    }

    /**
     * Creates a MySQLContainer that can be stored and retrieved from using the provided database credentials.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param database The name of the database to connect to.
     * @param address The IP address of the database.
     * @param port The port of the database.
     * @param username The username of the account to access the database with.
     * @param password The password of the account to access the database with.
     * @param settings The StorageSettings for this Container.
     * @throws SQLException In the case that a connection cannot be made.
     */
    public MySQLContainer(String database, String address, int port, String username, String password, StorageSettings settings) throws SQLException {
        super(settings);
        this.connection = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/" + database, username, password);
    }

    /**
     * Sets data into the cache that will eventually be updated into the database.
     * Data that is set may be lost in the case of a hard crash.
     *
     * To determine how often this data is pushed to the structure, change the {@link StorageSettings}.
     *
     * @param table The MySQL {@link Table} to set data to.
     * @param column The {@link Column} within the Table to set data to.
     * @param data The data to set.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void set(Table table, Column column, Object data, ColumnPredicate... predicates) {
        this.set(table, new HashMap<>() {{ put(column, data); }}, predicates);
    }

    /**
     * Sets data into the cache that will eventually be updated into the database.
     * Data that is set may be lost in the case of a hard crash.
     *
     * To determine how often this data is pushed to the structure, change the {@link StorageSettings}.
     *
     * @param table The MySQL {@link Table} to set data to.
     * @param columns A map of {@link Column} and the data to set into that column.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void set(Table table, Map<Column, Object> columns, ColumnPredicate... predicates) {
        Pair<String, Object[]> path = createPath(table, columns, predicates);
        this.set(path.getKey(), path.getValue());
    }

    /**
     * Stores data to the data structure. This method is executed asynchronously.
     * @see MySQLContainer#set(Table, Column, Object, ColumnPredicate...) for caching objects instead of writing directly to the structure.
     *
     * @param table The MySQL {@link Table} to store data to.
     * @param column The {@link Column} within the Table to set data to.
     * @param data The data to set.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void store(Table table, Column column, Object data, ColumnPredicate... predicates) {
        Objects.requireNonNull(table, "Cannot store to null table!");
        Objects.requireNonNull(column, "Cannot store to null column!");

        this.store(table, new HashMap<>() {{ put(column, data); }}, predicates);
    }

    /**
     * Stores data to the data structure. This method is executed asynchronously.
     * @see MySQLContainer#set(Table, Map, ColumnPredicate...) for caching objects instead of writing directly to the structure.
     *
     * @param table The MySQL {@link Table} to store data to.
     * @param columns A map of {@link Column} and the data to set into that column.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void store(Table table, Map<Column, Object> columns, ColumnPredicate... predicates) {
        Objects.requireNonNull(table, "Cannot store to null table!");
        Objects.requireNonNull(columns, "Cannot store to null columns!");
        if(columns.isEmpty()) { throw new IllegalArgumentException("You must provide atleast one column to store to!"); }

        Pair<String, Object[]> path = createPath(table, columns, predicates);
        super.store(path.getKey(), path.getValue());
    }

    /**
     *
     * Puts data into a cache that will eventually be updated into the database to remove them.
     * Data that is removed may be lost in the case of a hard crash.
     *
     * To determine how often this data is pushed to the structure, change the {@link StorageSettings}.
     *
     * @param table The MySQL {@link Table} to remove data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void remove(Table table, ColumnPredicate... predicates) {
        Objects.requireNonNull(table, "Cannot remove from null table!");

        Pair<String, Object[]> path = createPath(table, null, predicates);
        super.remove(path.getKey());
    }

    /**
     * Removes a path from the data source. This method is executed asynchronously.
     *
     * @param table The MySQL {@link Table} to remove data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     */
    public void delete(Table table, ColumnPredicate... predicates) {
        Objects.requireNonNull(table, "Cannot delete from null table!");

        Pair<String, Object[]> path = createPath(table, null, predicates);
        super.delete(path.getKey());
    }

    /**
     * Returns a cached object.
     * Objects are only cached after they've initially been retrieved.
     * Therefore, this method will always return null if you haven't retrieved a path yet.
     *
     * @see MySQLContainer#retrieve(Table, Column, ColumnPredicate...) for retrieving data
     *
     * @param table The MySQL {@link Table} to get data from.
     * @param column The {@link Column} to get data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The type to cast to
     * @return The cached object, or null if none exists at the path.
     */
    public <T> T get(Table table, Column column, ColumnPredicate... predicates) {
        Objects.requireNonNull(table, "Cannot get from null table!");
        Objects.requireNonNull(column, "Cannot get from null column!");

        Pair<String, Object[]> path = createPath(table, new HashMap<>() {{ put(column, 0); }}, predicates);
        return super.get(path.getKey());
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * Note: This method is blocking, and will block for up to 5 seconds.
     * It is highly recommended to only use this method if you know your data structure will not block
     *
     * @see MySQLContainer#retrieve(Class, Table, ColumnPredicate...)  for retrieving with explicit casting, or to recompose decomposed objects.
     * @see MySQLContainer#retrieveAsync(Table, Column, ColumnPredicate...)  for retrieving data asynchronously.
     *
     * @param table The MySQL {@link Table} to get data from.
     * @param column The {@link Column} to get data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The type to cast to
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> T retrieve(Table table, Column column, ColumnPredicate... predicates) {
        Objects.requireNonNull(table, "Cannot retrieve from null table!");
        Objects.requireNonNull(column, "Cannot retrieve from null column!");

        Pair<String, Object[]> path = createPath(table, new HashMap<>() {{ put(column, 0); }}, predicates);
        return super.retrieve(path.getKey());
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * This method will ONLY retrieve {@link StorageDecomposer}s.
     * Attempting to retrieve data that doesn't have a StorageDecompose will result in a {@link IllegalArgumentException}.
     *
     * Note: This method is blocking, and will block until the data structure returns an object.
     * It is highly recommended to only use this method if you know your data structure will not block
     *
     * @see MySQLContainer#retrieve(Table, Column, ColumnPredicate...) to get the value with implicit casting.
     * @see MySQLContainer#retrieveAsync(Class, Table, ColumnPredicate...) for retrieving data asynchronously.
     *
     * @param type The type of {@link StorageDecomposer} to obtain. If no StorageComposer exists with this type, the method will throw an error.
     * @param table The MySQL {@link Table} to get data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The explicit type that will be cast to.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> T retrieve(Class<T> type, Table table, ColumnPredicate... predicates) {
        Objects.requireNonNull(type, "Cannot retrieve null type!");
        Objects.requireNonNull(table, "Cannot retrieve from null table!");

        if(StorageContainer.getDecomposer(type) != null) {
            StringBuilder predicateBuilder = new StringBuilder();
            for(ColumnPredicate predicate : predicates) {
                if(predicate == null) continue;
                predicateBuilder.append(predicate.column().getName()).append("=").append(predicate.getData()).append(";");
            }

            predicateBuilder.setLength(predicateBuilder.length() - 1);
            return super.retrieve(type, table.getName() + "." + predicateBuilder + "#%path%");
        } else throw new IllegalArgumentException("A decomposer for type " + type.getSimpleName() + " does not exist.");
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * @see MySQLContainer#retrieveAsync(Class, Table, ColumnPredicate...) for retrieving with explicit casting, or to recompose decomposed objects.
     *
     * @param table The MySQL {@link Table} to get data from.
     * @param column The {@link Column} to get data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The explicit type that will be cast to.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveAsync(Table table, Column column, ColumnPredicate... predicates) {
        return CompletableFuture.supplyAsync(() -> this.retrieve(table, column, predicates));
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * This method will ONLY retrieve {@link StorageDecomposer}s.
     * Attempting to retrieve data that doesn't have a StorageDecompose will result in a {@link IllegalArgumentException}.
     *
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     *
     * @see MySQLContainer#retrieveAsync(Table, Column, ColumnPredicate...)  to get the value with implicit casting, or to get a non-composed object.
     *
     * @param type The type of {@link StorageDecomposer} to obtain. If no StorageComposer exists with this type, the method will throw an error.
     * @param table The MySQL {@link Table} to get data from.
     * @param predicates An array of at-least 1 {@link ColumnPredicate}. These act as your WHERE clauses in the SQL statement.
     * @param <T> The explicit type that will be cast to.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveAsync(Class<T> type, Table table, ColumnPredicate... predicates) {
        return CompletableFuture.supplyAsync(() -> this.retrieve(type, table, predicates));
    }

    /**
     * Adds a {@link Table} into the MySQL database.
     *
     * @param table The Table to add.
     * @return The MySQLContainer instance.
     */
    public MySQLContainer addTable(Table table) {
        table.createTable();
        this.tables.add(table);
        return this;
    }

    /**
     * Checks if any row in the provided table matches all of the provided predicates.
     *
     * @param table The {@link Table} to check if a row exists.
     * @param predicates A Column=Value map that act as the WHERE clause for the SQL Statement.
     * @return If any row matches the predicates within the table.
     * @throws SQLException If an SQLException is thrown.
     */
    public CompletableFuture<Boolean> rowExists(String table, Map<String, String> predicates) throws SQLException {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        List<String> predicateKeys = new ArrayList<>(predicates.keySet());
        String predicateBuilder = createPredicateString(predicateKeys);

        int index = 1;
        PreparedStatement statement = this.getConnection().prepareStatement("SELECT EXISTS(SELECT * FROM " + table + " WHERE " + predicateBuilder + ") AS result");

        for(String key : predicateKeys) {
            statement.setString(index++, predicates.get(key));
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                return statement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(results -> {
            try {
                results.next();
                future.complete(results.getBoolean("result"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return future;
    }

    /**
     * @return The MySQL {@link Connection}
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return All of the registered {@link Table}s for this Container.
     */
    public List<Table> getTables() {return tables;}

    /**
     * A Regex Pattern that should be used to check for SQL Injection. It is not recommended executing statements
     * that fail to pass this regex.
     *
     * @return A Regex {@link Pattern} that matches only alphanumeric characters and underscores.
     */
    public static Pattern getInjectionPattern() {
        return injection;
    }

    /**
     * Gets a registered {@link Table} from the Container by name.
     *
     * @param name The name of the Table to get.
     * @return The registered Table with the name, or null if it does not exist.
     */
    public Table getTable(String name) {
        return this.getTables().stream().filter(table -> table.getName().equalsIgnoreCase(name)).findAny().orElse(null);
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

                StringBuilder builder = new StringBuilder();
                List<String> predicateKeys = new ArrayList<>(predicates.keySet());
                List<String> keys = new ArrayList<>(mappedPath.keySet());

                rowExists(table, predicates).thenAccept(exists -> {
                    try {
                        PreparedStatement query;
                        if (!exists) {
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

    @Override
    public Set<String> getKeys(String path) {
        return new HashSet<>();
    }

    private Pair<String, Object[]> createPath(Table table, Map<Column, Object> columns, ColumnPredicate... predicates) {
        StringBuilder path = new StringBuilder(table.getName() + ".");
        for(ColumnPredicate predicate : predicates) {
            if(predicate == null) continue;
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

    private String createPredicateString(List<String> predicates) {
        StringBuilder builder = new StringBuilder();
        for(String key : predicates) {
            builder.append(key).append("=?").append(" AND ");
        }
        builder.setLength(Math.max(0, builder.length() - 5));

        return builder.toString();
    }

    /**
     * @deprecated Use {@link MySQLContainer#store(Table, Column, Object, ColumnPredicate...)} to store objects using this container type.
     */
    @Deprecated
    @Override
    public CompletableFuture<Void> store(String path, Object data) {
        return super.store(path, data);
    }

    /**
     * @deprecated Use {@link MySQLContainer#set(Table, Column, Object, ColumnPredicate...)} to set objects using this container type.
     */
    @Deprecated
    @Override
    public void set(String path, Object data) {
        super.set(path, data);
    }

    /**
     * @deprecated Use {@link MySQLContainer#get(Table, Column, ColumnPredicate...)} to get objects using this container type.
     */
    @Deprecated
    @Override
    public <T> T get(String path) throws ClassCastException {
        return super.get(path);
    }

    /**
     * @deprecated Use {@link MySQLContainer#retrieve(Class, Table, ColumnPredicate...)} to retrieve objects using this container type.
     */
    @Deprecated
    @Override
    public <T> T retrieve(Class<T> type, String path) {
        return super.retrieve(type, path);
    }

    /**
     * @deprecated Use {@link MySQLContainer#retrieve(Table, Column, ColumnPredicate...)} to retrieve objects using this container type.
     */
    @Deprecated
    @Override
    public <T> T retrieve(String path) {
        return super.retrieve(path);
    }

    /**
     * @deprecated Use {@link MySQLContainer#retrieveAsync(Table, Column, ColumnPredicate...)} to retrieve objects using this container type.
     */
    @Deprecated
    @Override
    public <T> CompletableFuture<T> retrieveAsync(String path) {
        return super.retrieveAsync(path);
    }

    /**
     * @deprecated Use {@link MySQLContainer#retrieveAsync(Class, Table, ColumnPredicate...)} to retrieve objects using this container type.
     */
    @Deprecated
    @Override
    public <T> CompletableFuture<T> retrieveAsync(Class<T> type, String path) {
        return super.retrieveAsync(type, path);
    }

    /**
     * @deprecated Use {@link MySQLContainer#remove(Table, ColumnPredicate...)} to remove objects using this container type.
     */
    @Deprecated
    @Override
    public void remove(String path) {
        super.remove(path);
    }

    /**
     * @deprecated Use {@link MySQLContainer#remove(Table, ColumnPredicate...)} to remove objects using this container type.
     */
    @Deprecated
    @Override
    public CompletableFuture<Void> delete(String path) {
        return super.delete(path);
    }
}
