package com;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.command.CommandResponse;
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader;
import com.clickhouse.client.api.enums.Protocol;
import com.clickhouse.client.api.internal.DataTypeConverter;
import com.clickhouse.client.api.query.QueryResponse;
import com.clickhouse.data.ClickHouseColumn;
import com.clickhouse.data.ClickHouseDataType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClickHouseDemo {

    private final Client client;

    public ClickHouseDemo(String host, Integer mappedPort, String dbName, String userName, String pwd) {
        client = new Client.Builder()
                .addEndpoint(Protocol.HTTP, host, mappedPort, false)
                .setUsername(userName)
                .setPassword(pwd)
                .setDefaultDatabase(dbName)
                .build();
    }

    public void createTable() throws Exception {

        execute("""
            DROP TABLE IF EXISTS person
            """);

        execute("""
            CREATE TABLE person
            (
                id UInt32,
                name String,
                tags Array(String)
            )
            ENGINE = MergeTree
            ORDER BY id
            """);
    }

    public void insertRows() throws Exception {

        execute("""
            INSERT INTO person VALUES
            (1,'Alice',['java','spring']),
            (2,'Bob',['docker','kotlin']),
            (3,'Carol',['java','clickhouse'])
            """);
    }

    public void selectRows() throws Exception {

        Map<String, Object> params =
                Map.of("tags1",
                        convertToLiteral(List.of("java", "docker", "C++"))
                );

        String sqlQuery1 = """
                SELECT id,name,tags
                FROM person
                WHERE hasAny(tags,{tags1:Array(String)})
                ORDER BY id
                """;
        collectResult(sqlQuery1, params);

        collectResult("select * from person where name in ({tags2:Array(String)})",
                Map.of("tags2", convertToLiteral(List.of("Alice", "Bob", "Carol"))));
    }
//"['java','docker', 'C++']"
    private static String convertToLiteral(Collection<String> java) {
        if(java == null || java.isEmpty()){
            return "[]";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        for (String s : java) {
            stringBuilder.append('\'').append(s).append('\'').append(',');
        }
        stringBuilder.setCharAt(stringBuilder.length() - 1, ']');
        return stringBuilder.toString();
    }

    private void collectResult(String sqlQuery1, Map<String, Object> params) throws Exception {
        try (QueryResponse response =
                     client.query(sqlQuery1,
                                     params)
                             .get(30, TimeUnit.SECONDS)) {

            ClickHouseBinaryFormatReader reader =
                    client.newBinaryFormatReader(response);

            List<ClickHouseColumn> columns = reader.getSchema().getColumns();
            columns.forEach(column -> System.out.println("column: " + column));
            System.out.println("\n\n");
            while (reader.hasNext()) {
                reader.next();

                AtomicInteger atomicInteger = new AtomicInteger(1);
                columns.forEach(column ->
                {
                    int columnIndex = column.getColumnIndex();
                    int andIncrement = atomicInteger.getAndIncrement();
                    Object object = reader.readValue(andIncrement);
                    if(column.getDataType() == ClickHouseDataType.Array){
                        object = new DataTypeConverter().convertToString(object, column);
                    }
                    System.out.print(','+columnIndex+','+ andIncrement+":" + column + ":" + object);
                });

                // Array reading depends on the element type.
                // We can discuss the best API once the connection is working.
                System.out.println("\n");
            }
        }
    }

    private void execute(String sql) throws Exception {

        CompletableFuture<CommandResponse> execute = client.execute(sql);
        try (CommandResponse ignored =
                     execute.get(30, TimeUnit.SECONDS)) {
            // nothing else required
            System.out.println(ignored);
        }
    }

    public void close() {
        client.close();
    }
}
