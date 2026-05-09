//package com.abcm0018.sai.auth.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.simp.SimpMessageType;
//import org.springframework.security.authorization.AuthorizationManager;
//import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
//import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
//
//@Configuration
//@EnableWebSocketSecurity
//public class WebSocketSecurityConfig {
//
//	@Bean
//	public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
//
//		messages
//				// REGLA 1: Requerir authenticación para SUSCRIBIRSE
//				.simpSubscribeDestMatchers("/topic/**").authenticated()
//
//				// REGLA 2: Requerir autenticación para ENVIAR
//				.simpDestMatchers("/app/**").authenticated()
//
//				// REGLA 3: Permitir mensajes del protocolo STOMP
//				.simpTypeMatchers(
//						SimpMessageType.CONNECT,
//						SimpMessageType.HEARTBEAT,
//						SimpMessageType.UNSUBSCRIBE,
//						SimpMessageType.DISCONNECT
//				).permitAll()
//
//				// REGLA 4: Denegar el resto
//				.anyMessage().denyAll();
//
//		return messages.build();
//	}
//}
