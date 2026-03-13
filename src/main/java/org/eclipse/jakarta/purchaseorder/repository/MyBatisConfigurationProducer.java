package org.eclipse.jakarta.purchaseorder.repository;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class MyBatisConfigurationProducer {

    @Resource(lookup = "java:jboss/datasources/ExampleDS")
    private DataSource dataSource;

    @Produces
    @ApplicationScoped
    public SqlSessionFactory produceSqlSessionFactory() {
        TransactionFactory transactionFactory = new ManagedTransactionFactory();
        Environment environment = new Environment("default", transactionFactory, dataSource);

        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(PurchaseOrderQueryMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }
}
