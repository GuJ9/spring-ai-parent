
package com.itszb.chatrag.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingModelPrimaryConfig {

    @Bean
    public static BeanFactoryPostProcessor primaryEmbeddingModel() {

        return beanFactory -> {

            // 指定 DashScope 作为默认 EmbeddingModel
            String beanName = "dashscopeEmbeddingModel";

            if (!beanFactory.containsBeanDefinition(beanName)) {
                throw new IllegalStateException(
                        "没有找到 DashScope EmbeddingModel"
                );
            }

            // 让 Spring 优先选择这个 Bean
            beanFactory.getBeanDefinition(beanName)
                    .setPrimary(true);

            System.out.println(
                    "========== DashScope EmbeddingModel 已设为 Primary =========="
            );
        };
    }
}
