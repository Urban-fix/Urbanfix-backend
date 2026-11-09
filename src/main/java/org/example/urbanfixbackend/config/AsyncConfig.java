package org.example.urbanfixbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
// Esta clase sirve para configurar el soporte para tareas asincronas
@Configuration // Indica que esta clase es una configuracion
@EnableAsync // Activa el soporte para tareas asincronas
// De tipo ThreadPoolTaskExecutor que es un executor de tareas asincronas
public class AsyncConfig {

    // Bean que configura el executor de tareas asincronas
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
