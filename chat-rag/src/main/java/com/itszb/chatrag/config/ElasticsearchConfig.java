
package com.itszb.chatrag.config;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestClientBuilderCustomizer elasticsearchAuth(
            @Value("c0lxUGdhRUJuQ1ZvNmNmRy1ZcDg6cGpPejdQOUlaUmh0cjk0YUhRRGxnQQ==") String apiKey) {

        return builder -> builder.setDefaultHeaders(
                new Header[]{
                        new BasicHeader(
                                "Authorization",
                                "ApiKey " + apiKey.trim()
                        )
                }
        );
    }
}
