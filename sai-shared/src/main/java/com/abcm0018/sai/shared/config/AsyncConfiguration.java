package com.abcm0018.sai.shared.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuración para habilitar la ejecución asíncrona (@Async)
 * y definir un pool de hilos (Executor) dedicado para tareas
 * de importación masiva.
 * <p>
 * Esto evita que los trabajos pesados de importación saturen
 * el pool de hilos web (Tomcat) principal.
 */
@Slf4j
@EnableAsync // Habilita la funcionalidad @Async de Spring
@Configuration
public class AsyncConfiguration {
	/**
	 * Nombre del Bean del TaskExecutor para IMPORTACIONES.
	 * Usar: @Async(AsyncConfig.IMPORT_TASK_EXECUTOR)
	 */
	public static final String IMPORT_TASK_EXECUTOR = "importTaskExecutor";

	/**
	 * Nombre del Bean del TaskExecutor para NOTIFICACIONES (Emails, etc).
	 * Usar: @Async(AsyncConfig.NOTIFICATION_TASK_EXECUTOR)
	 * O simplemente @Async (ya que es @Primary)
	 */
	public static final String NOTIFICATION_TASK_EXECUTOR = "notificationTaskExecutor";

	/**
	 * Pool de Hilos para TAREAS PESADAS (Importaciones).
	 * Configurado para bajo paralelismo y alta capacidad de cola.
	 */
	@Bean(name = IMPORT_TASK_EXECUTOR)
	public TaskExecutor importTaskExecutor() {
		log.info("Configurando TaskExecutor para importaciones (pesadas)...");
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(2); // 2 hilos dedicados
		executor.setMaxPoolSize(5);  // Máximo 5 importaciones concurrentes
		executor.setQueueCapacity(25); // 25 trabajos en cola
		executor.setThreadNamePrefix("ImportWorker-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

	/**
	 * Pool de Hilos para TAREAS LIGERAS (Emails, Notificaciones).
	 * <p>
	 * Configurado para alta concurrencia (muchos hilos) y una cola
	 * más pequeña, para responder rápido.
	 * <p>
	 * Este será el executor por defecto si se usa solo @Async.
	 */
	@Bean(name = NOTIFICATION_TASK_EXECUTOR)
	@Primary // <-- Este es el pool por defecto
	public TaskExecutor notificationTaskExecutor() {
		log.info("Configurando TaskExecutor para notificaciones (ligeras)...");
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(3); // 3 hilos listos para emails
		executor.setMaxPoolSize(10); // Puede crecer a 10 si hay muchos emails
		executor.setQueueCapacity(50); // 50 emails en cola
		executor.setThreadNamePrefix("NotificationWorker-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}
}
