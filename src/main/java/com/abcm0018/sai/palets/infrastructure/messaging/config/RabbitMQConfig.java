package com.abcm0018.sai.palets.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RabbitMQConfig {
	// Nombres constantes para nuestros componentes
	public static final String EXCHANGE_NAME = "inventario.exchange";
	public static final String QUEUE_LECTURAS = "inventario.lecturas.queue";
	public static final String QUEUE_DLQ = "inventario.lecturas.dlq";
	public static final String ROUTING_KEY = "lecturas.etiquetas";

	/**
	 * Exchange principal de tipo topic para enrutamiento flexible de mensajes.
	 *  - durable: sobrevive a reinicios del broker.
	 *  - autoDelete: false, no se borra automáticamente al cerrar el broker.
	 */
	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(EXCHANGE_NAME, true, false);
	}

	/**
	 * Cola principal para almacenar las lecturas recibidas por el exchange.
	 * Con configuración de resiliencia.
	 * Incluye:
	 * - Dead letter queue (DLQ) para mensajes que no se pueden procesar.
	 * - TTL (Time To Live) para mensajes que no se procesan en un tiempo determinado.
	 * - Límite de longitud de la cola.
	 */
	@Bean
	public Queue queueLecturas() {
		return QueueBuilder.durable(QUEUE_LECTURAS)
				// Dead letter queue (DLQ) para mensajes que no se pueden procesar.
				.withArgument("x-dead-letter-exchange", "")
				.withArgument("x-dead-letter-routing-key", QUEUE_DLQ)
				// TTL (Time To Live) para mensajes que no se procesan en 60 segundos.
				.withArgument("x-message-ttl", 60000)
				// Límite de mensajes en la cola
				.withArgument("x-max-length", 10000)
				// Prioridad de mensajes (0-10, mayor = prioridad alta)
				.withArgument("x-max-priority", 10)
				.build();
	}

	/**
	 * Cola para mensajes que fallaron después de reintentos
	 * Útil para debugging y auditoría
	 */
	@Bean
	public Queue deadLetterQueue() {
		return QueueBuilder.durable(QUEUE_DLQ)
				// Mensajes en DLQ expiran después de 7 días
				.withArgument("x-message-ttl", 604800000)
				.build();
	}

	/**
	 * Enlaza la cola con el exchange.
	 * @param queueLecturas - Cola donde se almacenan los mensajes recibidos por el exchange
	 * @param exchange - Exchange que se encarga de enviar mensajes a las colas
	 */
	@Bean
	public Binding bindingLecturas(Queue queueLecturas, TopicExchange exchange) {
		return BindingBuilder.bind(queueLecturas).to(exchange).with(ROUTING_KEY);
	}

	/**
	 * Convertidor JSON para serialización automática de objetos
	 * Usa Jackson para convertir Java Objects <-> JSON
	 */
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());

		// Confirmaciones de publicación (opcional pero recomendado)
		template.setMandatory(true);

		// Callback cuando un mensaje no puede ser enrutado
		template.setReturnsCallback(returned -> {
			log.error("Mensaje no enrutado: {}", returned.getMessage());
			log.error("Reply Code: {}", returned.getReplyCode());
			log.error("Reply Text: {}", returned.getReplyText());
		});

		return template;
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
			SimpleRabbitListenerContainerFactoryConfigurer configurer, LocalValidatorFactoryBean validator) {

		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		configurer.configure(factory, connectionFactory);

		// Habilitar validación automática
		factory.setMessageConverter(jsonMessageConverter());

		return factory;
	}
}
