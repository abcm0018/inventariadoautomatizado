package com.abcm0018.sai.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuración de Infraestructura Global para WebSocket (STOMP)
 * <p>
 * Habilita el servidor Websocket y el bróker de mensajes en memoria.
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final ChannelInterceptor jwtStompInterceptor;

	@Autowired
	public WebSocketConfig(ChannelInterceptor jwtStompInterceptor) {
		this.jwtStompInterceptor = jwtStompInterceptor;
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {

		// 1. Prefijos del Bróker (Destinos del Servidor al Cliente)
		// El cliente se suscribirá a destinos que empiezan con "/topics"
		// Ejemplo: stomClient.suscribe("/topic/palets", ...)
		registry.enableSimpleBroker("/topic");

		// 2. PRefijo de la Aplicación (DESDE el cliente)
		// Si el cliente envía un mensaje al backend, lo enviará a destinos
		// que empiezan con "/app"
		// Ejemplo: stomClient.send("/app/palets", ...)
		// No lo usaremos mucho para nuestro sistema, pero es obligatorio
		registry.setApplicationDestinationPrefixes("/app");
	}

	/**
	 * Define los "endpoints" a los que se conectará el cliente.
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// El cliente JS se conectará a esta URL:
		// "http://localhost:8080/wssai"
		registry.addEndpoint("/wssai")
				// Permite conexiones desde cualquier origen
				// En producción se debe restringir a la URL del frontend
				.setAllowedOriginPatterns("*")
				.withSockJS();
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		log.info("Registrando JwtStompInterceptor en el clientInboundChannel...");
		registration.interceptors(jwtStompInterceptor);
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
	}
}
