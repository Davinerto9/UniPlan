package edu.co.icesi.eventsmanager.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
public class MongoAtlasConfig {

    private static final String URI =
            "mongodb+srv://giuseppe_db_user:123@cluster0.lfnesef.mongodb.net/UniPlan?retryWrites=true&w=majority&appName=Cluster0";

    @Bean
    @Primary
    public MongoClient mongoClient() {
        return MongoClients.create(new ConnectionString(URI));
    }

    @Bean
    @Primary
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), "UniPlan");
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDatabaseFactory());
    }

    @Bean(name = "mongoTransactionManager")
    public MongoTransactionManager mongoTransactionManager() {
        return new MongoTransactionManager(mongoDatabaseFactory());
    }

    @Bean(name = "transactionManager")
    @Primary
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}