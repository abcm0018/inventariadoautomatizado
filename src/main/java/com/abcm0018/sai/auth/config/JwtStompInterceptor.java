package com.abcm0018.sai.auth.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.abcm0018.sai.auth.application.JWTService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class JwtStompInterceptor implements ChannelInterceptor {

	private final JWTService jwtService;
	private final UserDetailsService userDetailsService;

	// En JwtStompInterceptor.java

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

			String authHeader = accessor.getFirstNativeHeader("Authorization");

			if (authHeader != null && authHeader.startsWith("Bearer ")) {

				try {
					String token = authHeader.substring(7);
					log.debug("WebSocket: Intentando autenticar con token...");

					String employeeNumber = jwtService.getEmployeeNumberFromToken(token);

					if (employeeNumber != null) {
						UserDetails userDetails = userDetailsService.loadUserByUsername(employeeNumber);

						if (jwtService.isTokenValid(token, userDetails)) {
							// ... (Autenticación exitosa)
							UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
									userDetails, null, userDetails.getAuthorities());
							accessor.setUser(authentication);
							log.info("✅ Usuario autenticado en WebSocket {}", employeeNumber);
						} else {
							// Token es inválido (firma, etc.)
							log.warn("❌ Token JWT inválido para WebSocket");
							// LANZAR EXCEPCIÓN PARA RECHAZAR EL CONNECT
							throw new org.springframework.security.access.AccessDeniedException("Token JWT inválido");
						}
					}
				} catch (Exception e) { // Captura genérica (UsernameNotFound, JwtException, etc.)
					// Si el token el inválido, RECHAZAMOS la conexión.
					log.warn("❌ Error de autenticación en WebSocket: {}", e.getMessage());
					// LANZAR EXCEPCIÓN PARA RECHAZAR EL CONNECT
					throw new AccessDeniedException("Autenticación fallida: " + e.getMessage());
				}
			} else {
				log.warn("❌ Intento de conexión a WebSocket sin token");
				// LANZAR EXCEPCIÓN PARA RECHAZAR EL CONNECT
				throw new AccessDeniedException("Falta cabecera Authorization");
			}
		}

		// Continuamos con el mensaje (solo si no es un CONNECT o si el CONNECT fue exitoso)
		return message;
	}
}
