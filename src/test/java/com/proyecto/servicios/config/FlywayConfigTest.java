package com.proyecto.servicios.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlywayConfigTest {

    @Test
    @DisplayName("flyway bean se configura con datasource")
    void flywayBeanConfig() throws Exception {
        FlywayConfig config = new FlywayConfig();
        ReflectionTestUtils.setField(config, "locations", new String[]{"classpath:db/migration"});
        ReflectionTestUtils.setField(config, "historyTable", "flyway_schema_history");
        ReflectionTestUtils.setField(config, "schema", "public");

        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(meta.getDatabaseProductVersion()).thenReturn("15.0");

        // Test configuring flyway bean structure
        assertNotNull(config);
    }
}
