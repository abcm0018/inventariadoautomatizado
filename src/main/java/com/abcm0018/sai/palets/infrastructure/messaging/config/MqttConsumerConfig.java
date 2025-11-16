package com.abcm0018.sai.palets.infrastructure.messaging.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import com.abcm0018.sai.palets.infrastructure.messaging.consumer.MqttPaletConsumerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuración para el consumidor de mensajes MQTT (Inbound).
 * Esta clase define cómo se recibe y procesa los mensajes del broker.
 * Se activa/desactiva en application.properties usando la propiedad 'mqtt.consumer.enable'
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
// Esta anotación activa toda la clase soli si la propiedad es 'true'
@ConditionalOnProperty(prefix = "mqtt.consumer", name = "enable", havingValue = "true", matchIfMissing = true)
public class MqttConsumerConfig {

	private final MqttProperties mqttProperties;
	private final MqttPahoClientFactory mqttClientFactory;

	/**
	 * Define el canal por donde fluirán los mensajes MQTT entrantes
	 */
	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}

	/**
	 * Define el Adaptador de Entrada (Inbound Adapter).
	 * Este es el componente que se conecta, se suscribe y recibe los mensajes.
	 */
	@Bean
	public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter() {
		String clientId = mqttProperties.getClientId();

		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
				clientId,
				mqttClientFactory,
				mqttProperties.getTopic().getAll() // Se suscribe al topic 'all' (ej. sai/palets/#)
		);

		adapter.setCompletionTimeout(5000); // Timeout para operaciones
		adapter.setConverter(new DefaultPahoMessageConverter()); // Conversor de payload por defecto
		adapter.setQos(mqttProperties.getQos());
		adapter.setOutputChannelName("mqttInputChannel"); // Enviar mensajes al canal 'mqttInputChannel'


		log.info("▶️ Consumidor MQTT [ClientID: {}] configurado para escuchar en topics: {}",
				clientId, mqttProperties.getTopic().getAll());

		return adapter;
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttInputChannel")
	public MessageHandler mqttMessageHandler(MqttPaletConsumerService mqttPaletConsumerService) {
		return mqttPaletConsumerService::handleMqttMessage;
	}

}
