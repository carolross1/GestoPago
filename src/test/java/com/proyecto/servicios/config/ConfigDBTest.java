package com.proyecto.servicios.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigDBTest {

    @Test
    @DisplayName("sfEntityManagerFactory y sfTransactionManager se construyen con dependencias")
    void configuracionBaseDeDatos() {
        Environment env = mock(Environment.class);
        ConfigDB configDB = new ConfigDB(env);

        DataSource ds = mock(DataSource.class);
        LocalContainerEntityManagerFactoryBean em = configDB.sfEntityManagerFactory(ds);
        assertNotNull(em);
        assertEquals(ds, em.getDataSource());

        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        PlatformTransactionManager tm = configDB.sfTransactionManager(emf);
        assertNotNull(tm);
        assertInstanceOf(JpaTransactionManager.class, tm);
    }

    @Test
    @DisplayName("sfDatasource lanza IllegalStateException si las propiedades son invalidas")
    void sfDatasourceError() {
        Environment env = mock(Environment.class);
        when(env.getProperty("spring.datasource.url")).thenReturn(null);

        ConfigDB configDB = new ConfigDB(env);
        assertThrows(IllegalStateException.class, configDB::sfDatasource);
    }
}
