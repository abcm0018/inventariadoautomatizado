package com.abcm0018.sai.palets.infrastructure.messaging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MqttProducerConfig {

	/**
	 * Define la "Puerta de Enlace" (Gateway).
	 * Nuestros servicios usarán esta interfaz para enviar mensajes
	 * de forma desacoplada, sin conocer los detalles de MQTT.
	 */
	@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
	public interface MqttOutboundGateway {
		void sendToMqtt(String payload, @Header(MqttHeaders.TOPIC) String topic);
	}

	/**
	 * Define el canal de salida. Los mensajes puestos en este canal
	 * serán recogidos por el 'mqttOutboundAdapter'.
	 */
	@Bean
	public MessageChannel mqttOutboundChannel() {
		return new DirectChannel();
	}

	/**
	 * Define el Adaptador de Salida (Outbound Adapter).
	 * Este es el componente que realmente se conecta al broker y publica el mensaje.
	 */
	@Bean
	@ServiceActivator(inputChannel = "mqttOutboundChannel")
	public MessageHandler mqttOutboundAdapter(MqttPahoClientFactory clientFactory, MqttProperties mqttProperties) {

		// Usamos el nuevo Client ID específico para el productor
		String producerClientId = mqttProperties.getProducerClientId();

		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(producerClientId, clientFactory);

		messageHandler.setAsync(true); // Envío asíncrono
		messageHandler.setDefaultQos(mqttProperties.getQos()); // QoS por defecto
		messageHandler.setCompletionTimeout(5000); // Timeout

		log.info("Productor MQTT [ClientID: {}] configurado.", producerClientId);

		return messageHandler;
	}
}
