package com;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ParameterizedQueryTest2 {

    static GenericContainer<?> clickHouse;
    static String dbName = "default1";
    static String userName = "test1";
    static String pwd = "test2";

    static {
        clickHouse = new GenericContainer<>("clickhouse/clickhouse-server:head-distroless")
                .withExposedPorts(8123)
                .waitingFor(Wait.forHttp("/ping").forStatusCode(200))
                .withStartupTimeout(java.time.Duration.ofSeconds(1000))
                .withEnv("CLICKHOUSE_DB", dbName)
                .withEnv("CLICKHOUSE_USER", userName)
                .withEnv("CLICKHOUSE_PASSWORD", pwd);
    }
    @AfterEach
    void tearDown() {
        clickHouse.stop();
    }

    @BeforeEach
    void setup() {
        clickHouse.start();
    }


    @Test
    void call() throws Exception {
        ClickHouseDemo clickHouseDemo = new ClickHouseDemo(clickHouse.getHost(),
                clickHouse.getMappedPort(8123), dbName, userName, pwd);
        try {
            clickHouseDemo.createTable();
            clickHouseDemo.insertRows();
            clickHouseDemo.selectRows();
        } finally {
            clickHouseDemo.close();
        }
    }


}
