package com.ai.sre.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/sre_platform}")
    private String url;

    @Value("${spring.datasource.username:sre_user}")
    private String user;

    @Value("${spring.datasource.password:sre_password}")
    private String password;

    @Bean
    public EmbeddingModel embeddingModel() {
        // Fast, local embedding model that doesn't require API keys
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host(extractHost(url))
                .port(extractPort(url))
                .database(extractDatabase(url))
                .user(user)
                .password(password)
                .table("incident_embeddings")
                .dimension(384) // Dimension for AllMiniLmL6V2
                .useIndex(true)
                .indexListSize(100)
                .createTable(true)
                .dropTableFirst(false)
                .build();
    }

    private String extractHost(String jdbcUrl) {
        try {
            return jdbcUrl.split("://")[1].split(":")[0];
        } catch (Exception e) {
            return "localhost";
        }
    }

    private Integer extractPort(String jdbcUrl) {
        try {
            return Integer.parseInt(jdbcUrl.split("://")[1].split(":")[1].split("/")[0]);
        } catch (Exception e) {
            return 5432;
        }
    }

    private String extractDatabase(String jdbcUrl) {
        try {
            return jdbcUrl.split("://")[1].split("/")[1].split("\\?")[0];
        } catch (Exception e) {
            return "sre_platform";
        }
    }
}
