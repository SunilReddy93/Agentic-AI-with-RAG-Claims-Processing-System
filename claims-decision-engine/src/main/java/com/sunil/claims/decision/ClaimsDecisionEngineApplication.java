package com.sunil.claims.decision;

import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {
        OpenAiEmbeddingAutoConfiguration.class,
        PgVectorStoreAutoConfiguration.class
})
public class ClaimsDecisionEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimsDecisionEngineApplication.class, args);
    }

}
