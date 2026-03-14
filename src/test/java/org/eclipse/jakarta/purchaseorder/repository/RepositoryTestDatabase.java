package org.eclipse.jakarta.purchaseorder.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

final class RepositoryTestDatabase {
    private static final String JDBC_URL_TEMPLATE =
        "jdbc:h2:mem:%s;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";

    private RepositoryTestDatabase() {
    }

    static SqlSessionFactory createSqlSessionFactory() {
        DataSource dataSource = createDataSource();
        initializeSchema(dataSource);

        Environment environment = new Environment("test", new JdbcTransactionFactory(), dataSource);
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(PurchaseOrderQueryMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private static DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(JDBC_URL_TEMPLATE.formatted("purchase_order_test_" + UUID.randomUUID()));
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private static void initializeSchema(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection(); Reader scriptReader = openInitialDataScript()) {
            RunScript.execute(connection, scriptReader);
        } catch (SQLException | IOException exception) {
            throw new IllegalStateException("Failed to initialize H2 test database", exception);
        }
    }

    private static Reader openInitialDataScript() {
        var scriptStream = RepositoryTestDatabase.class.getResourceAsStream("/META-INF/initial-data.sql");
        if (scriptStream == null) {
            throw new IllegalStateException("Could not find META-INF/initial-data.sql on the test classpath");
        }

        return new BufferedReader(new InputStreamReader(scriptStream, StandardCharsets.UTF_8));
    }
}