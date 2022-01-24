package me.dessie.dessielib.storageapi.storage.format.mysql.struct.table;

import me.dessie.dessielib.storageapi.storage.format.mysql.MySQLContainer;
import me.dessie.dessielib.storageapi.storage.format.mysql.struct.column.Column;
import me.dessie.dessielib.storageapi.storage.format.mysql.struct.column.ColumnPredicate;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Table {

    private final List<Column> columns = new ArrayList<>();
    private final String name;
    private final MySQLContainer container;

    public Table(MySQLContainer container, String name) throws IllegalArgumentException {
        if(MySQLContainer.getInjectionPattern().matcher(name).find()) {
            throw new IllegalArgumentException("Name must contain only alphanumeric and underscore characters!");
        }
        this.container = container;
        this.name = name;
    }

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

        this.getContainer().executeSQL("CREATE TABLE IF NOT EXISTS " + this.getName() + " (" + columns + ")");
    }

    public Table addColumn(Column column) {
        this.columns.add(column);
        return this;
    }

    public void set(Column column, Object data, ColumnPredicate... predicates) {
        this.set(new HashMap<>() {{ put(column, data); }}, predicates);
    }

    public void set(Map<Column, Object> columns, ColumnPredicate... predicates) {
        this.getContainer().set(this, columns, predicates);
    }

    public void store(Column column, Object data, ColumnPredicate... predicates) throws IOException {
        this.store(new HashMap<>() {{ put(column, data); }}, predicates);
    }

    public void store(Map<Column, Object> columns, ColumnPredicate... predicates) throws IOException {
        this.getContainer().store(this, columns, predicates);
    }

    public void remove(ColumnPredicate... predicates) {
        this.getContainer().remove(this, predicates);
    }

    public void delete(ColumnPredicate... predicates) throws IOException {
        this.getContainer().delete(this, predicates);
    }

    public <T> T get(Column column, ColumnPredicate... predicates) {
        return this.getContainer().get(this, column, predicates);
    }

    public <T> CompletableFuture<T> retrieve(Column column, ColumnPredicate... predicates) {
        return this.getContainer().retrieve(this, column, predicates);
    }

    public <T> CompletableFuture<T> retrieve(Class<T> type, ColumnPredicate... predicates) {
        return this.getContainer().retrieve(type, this, predicates);
    }

    public Column getColumn(String name) {return this.columns.stream().filter(column -> column.getName().equals(name)).findAny().orElse(null);}
    public List<Column> getColumns() {return columns;}
    public String getName() {return name;}
    public MySQLContainer getContainer() {return container;}
}
