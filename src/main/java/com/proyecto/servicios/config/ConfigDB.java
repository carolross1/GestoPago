package com.proyecto.servicios.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.proyecto.servicios.repositorys.sf",
                "com.proyecto.servicios.repositorys.gestopago",
                "com.proyecto.servicios.repositorys.producto",
                "com.proyecto.servicios.repositorys.cliente",
                "com.proyecto.servicios.repositorys.catalogo"
        },
        transactionManagerRef = "sfTransactionManager",
        entityManagerFactoryRef = "sfEntityManagerFactory"
)
public class ConfigDB {

    private final Environment env;

    public ConfigDB(Environment env) {
        this.env = env;
    }

    @Bean(name = "sfDatasource")
    public DataSource sfDatasource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(env.getProperty("spring.datasource.url"));
            config.setPassword(env.getProperty("spring.datasource.password"));
            config.setUsername(env.getProperty("spring.datasource.username"));
            config.setMaximumPoolSize(10);
            config.setMaxLifetime(18800);
            config.setConnectionTimeout(5000);
            config.setValidationTimeout(5000);
            config.setMinimumIdle(2);
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("sfDatasource");
            return new HikariDataSource(config);
        } catch (Exception e) {
            log.error("Ha ocurrido un error en la conexion a base de datos, a causa de:", e);
            throw new IllegalStateException("No fue posible inicializar sfDatasource", e);
        }
    }

    @Bean(name = "sfEntityManagerFactory")
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean sfEntityManagerFactory(@Qualifier("sfDatasource") DataSource dataSource) {
        try {
            LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource);
            em.setPackagesToScan(
                    "com.proyecto.servicios.entity.sf",
                    "com.proyecto.servicios.entity.gestopago",
                    "com.proyecto.servicios.entity.producto",
                    "com.proyecto.servicios.entity.cliente",
                    "com.proyecto.servicios.entity.catalogo"
            );
            em.setPersistenceUnitName("sfDatasource");
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            Map<String, Object> properties = new HashMap<>();
            properties.put("hibernate.hbm2ddl.auto", "none");
            properties.put("hibernate.show-sql", false);
            properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            properties.put("jakarta.persistence.query.timeout", 600000);
            properties.put("hibernate.jdbc.batch_size", 50);
            properties.put("hibernate.order_inserts", true);
            em.setJpaPropertyMap(properties);
            return em;
        } catch (Exception e) {
            log.error("Ha ocurrido un error en la conexion a base de datos, a causa de:", e);
            throw new IllegalStateException("No fue posible inicializar sfEntityManagerFactory", e);
        }
    }

    @Bean(name = "sfTransactionManager")
    public PlatformTransactionManager sfTransactionManager(@Qualifier("sfEntityManagerFactory") EntityManagerFactory sfEntityManagerFactory) {
        return new JpaTransactionManager(sfEntityManagerFactory);
    }
}
