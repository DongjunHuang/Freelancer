package com.example.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import com.example.repos.UserRepo;

/**
 * Basic verification for mocked H2 database to be successfully used in
 * Integration tests.
 */
public class VerifyBaseApiIT extends BaseApiIT {

        @Autowired
        DataSource dataSource;

        @Autowired
        UserRepo userRepo;

        @Autowired
        JdbcTemplate jdbc;

        @Autowired(required = false)
        Flyway flyway;

        @Autowired
        Environment env;

        @Test
        void flywayBeanAndLocationsAreOk() {
                assertThat(flyway)
                                .as("Flyway bean should exist. If null, auto-config is disabled or Flyway not on classpath.")
                                .isNotNull();

                String loc = env.getProperty("spring.flyway.locations");
                assertThat(loc).as("spring.flyway.locations should be set").isNotBlank();

                var dir = new ClassPathResource("db/migration-h2");
                assertThat(dir.exists())
                                .as("classpath:db/migration-h2 must exist. Put SQL under src/test/resources/db/migration/h2/")
                                .isTrue();
        }

        @Test
        void flywaySeesMigrations() {
                assertThat(flyway).isNotNull();

                var info = flyway.info();
                assertThat(info.all().length)
                                .as("Flyway should see at least 1 migration in locations=%s",
                                                env.getProperty("spring.flyway.locations"))
                                .isGreaterThan(0);
        }

        @Test
        void h2IsUsed() throws Exception {
                try (Connection c = dataSource.getConnection()) {
                        String url = c.getMetaData().getURL();
                        assertThat(url)
                                        .as("DataSource JDBC URL should indicate H2, but was: %s", url)
                                        .containsIgnoringCase("jdbc:h2:");
                }
        }

        @Test
        void findFlywayHistoryAnywhere() {
                var rows = jdbc.queryForList("""
                                    SELECT TABLE_SCHEMA, TABLE_NAME
                                    FROM INFORMATION_SCHEMA.TABLES
                                    WHERE LOWER(TABLE_NAME) LIKE '%flyway%'
                                """);

                assertThat(rows)
                                .as("Tables with flyway in name: %s", rows)
                                .isNotEmpty();
        }

        @Test
        void flywayAndJdbcUseSameDatabase() throws Exception {
                String url1;
                try (var c = dataSource.getConnection()) {
                        url1 = c.getMetaData().getURL();
                }

                String url2;
                try (var c = flyway.getConfiguration().getDataSource().getConnection()) {
                        url2 = c.getMetaData().getURL();
                }

                assertThat(url1).as("App DataSource URL").contains("jdbc:h2:mem:testdb");
                assertThat(url2).as("Flyway DataSource URL").contains("jdbc:h2:mem:testdb");
                assertThat(url2).as("Flyway and app must use same DB").isEqualTo(url1);
        }

        @Test
        void manualFlywayMigrateCreatesHistoryTable() {
                flyway.migrate();

                Integer history = jdbc.queryForObject("""
                                    SELECT COUNT(*)
                                    FROM INFORMATION_SCHEMA.TABLES
                                    WHERE TABLE_NAME='flyway_schema_history'
                                """, Integer.class);

                assertThat(history).isEqualTo(1);
        }

        @Test
        void verifyFlywayHistoryTableExists() {
                Integer c = jdbc.queryForObject(
                                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='flyway_schema_history'",
                                Integer.class);
                assertThat(c)
                                .as("Flyway should create flyway_schema_history. If 0, Flyway did not run at all.")
                                .isEqualTo(1);
        }

        @Test
        void verifyUsersTableExists() {
                Integer c = jdbc.queryForObject(
                                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='USERS'",
                                Integer.class);
                assertThat(c)
                                .as("USERS table should exist after migrations.")
                                .isEqualTo(1);
        }

        @Test
        void repoIsWired() {
                assertThat(userRepo)
                                .as("UserRepo should be injected by Spring")
                                .isNotNull();

                assertThat(userRepo.count())
                                .as("UserRepo.count() should be callable (repo should be functional)")
                                .isGreaterThanOrEqualTo(0);
        }

        @Test
        void showTables() {
                var tables = jdbc.queryForList(
                                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'",
                                String.class);

                assertThat(tables)
                                .as("Should be able to read table list from H2 INFORMATION_SCHEMA")
                                .isNotNull();

                assertThat(tables.size())
                                .as("Expected at least one table in PUBLIC schema, but was: %s", tables)
                                .isGreaterThan(0);
        }
}