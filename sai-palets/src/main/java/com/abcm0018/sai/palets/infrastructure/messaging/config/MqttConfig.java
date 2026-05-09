package com.abcm0018.sai.palets.infrastructure.messaging.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuración de MQTT con Spring Integration
 * Gestiona la conexión al broker EMQX y la recuperación de mensajes
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MqttConfig {

	private final MqttProperties mqttProperties;

	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions connectOptions = new MqttConnectOptions();

		// Configuración de conexión
		connectOptions.setServerURIs(new String[] { mqttProperties.getBroker().getUrl() });
		connectOptions.setCleanSession(mqttProperties.isCleanSession());
		connectOptions.setAutomaticReconnect(mqttProperties.isAutoReconnect());
		connectOptions.setConnectionTimeout(mqttProperties.getConnectionTimeout());
		connectOptions.setKeepAliveInterval(mqttProperties.getKeepAliveInterval());
		connectOptions.setMaxInflight(mqttProperties.getMaxInflight());

		// Autenticación
		if (mqttProperties.getUsername() != null && mqttProperties.getPassword() != null) {
			connectOptions.setUserName(mqttProperties.getUsername());
			connectOptions.setPassword(mqttProperties.getPassword().toCharArray());
		}

		factory.setConnectionOptions(connectOptions);

		log.debug("MQTT Client Factory configurado para: {} con ClientID: {}", mqttProperties.getBroker().getUrl(), mqttProperties.getClientId());

		return factory;

	}
}
