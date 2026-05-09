package com.abcm0018.sai.palets.infrastructure.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Propiedades de configuración
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

	private final Broker broker = new Broker();
	private String clientId;
	private String producerClientId = "inventario-producer-springboot";
	private String username;
	private String password;
	private final Topic topic = new Topic();
	private int qos = 1;
	private boolean cleanSession = false;
	private boolean autoReconnect = true;
	private int connectionTimeout = 30;
	private int keepAliveInterval = 60;
	private int maxInflight = 10;
	private final Consumer consumer = new Consumer();

	@Getter
	@Setter
	public static class Broker {
		private String url;
	}

	@Getter
	@Setter
	public static class Topic {
		private String escaneos;
		private String alertas;
		private String all;
	}

	@Getter
	@Setter
	public static class Consumer {
		private boolean enabled = true;
		private int concurrency = 3;
	}
}
