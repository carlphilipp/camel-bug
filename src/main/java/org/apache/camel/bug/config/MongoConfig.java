package org.apache.camel.bug.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Value("${db.uri}")
    private String uri;

    @Bean(name = "cosmosdb")
    public MongoClient mongo() {
        return new MongoClient(new MongoClientURI(uri));
    }
}
