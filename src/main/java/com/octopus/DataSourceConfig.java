package com.octopus;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@Profile(Profiles.PROD)
public class DataSourceConfig {

  @Bean
  public DataSource getDataSource() {
    final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
    dsLookup.setResourceRef(true);
    DataSource dataSource = dsLookup.getDataSource("jdbc/DefaultDB");
    return dataSource;
  }

  @Bean
  public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);
    return transactionManager;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
    entityManagerFactoryBean.setPackagesToScan(this.getClass().getPackage().getName());
    entityManagerFactoryBean.setDataSource(this.getDataSource());
    entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    entityManagerFactoryBean.setJpaProperties(this.hibProperties());
    return entityManagerFactoryBean;
  }

  private Properties hibProperties() {
    Properties properties = new Properties();
    properties.put("hibernate.dialect", "org.hibernate.dialect.mysql5dialect");
    properties.put("hibernate.hbm2ddl.auto", "update");
    return properties;
  }
}
