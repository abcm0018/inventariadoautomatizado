package com.abcm0018.sai.shared.config;

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

	/**
	 * Orígenes permitidos para CORS
	 * Formato: <a href="http://localhost:8000,http://localhost:3000">...</a>
	 * En producción: <a href="https://app.ejemplo.com">...</a>
	 */
	@Value("${allowed.origins:http://localhost:8000}")
	private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(autRequest -> autRequest
                        .requestMatchers("/api/v1/auth/**",
								"/swagger-ui.html",
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/api-docs/**",
								"/wssai/**")
						.permitAll()
                        .anyRequest()
						.authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Orígenes permitidos (configurable desde application.properties)
		List<String> origins = Arrays.asList(allowedOrigins.split(","));
		configuration.setAllowedOrigins(origins);

		// Métodos HTTP permitidos
		// GET, POST, OPTIONS: Necesarios para SockJS y peticiones normales
		// PUT, DELETE: Para operaciones REST completas
		configuration.setAllowedMethods(Arrays.asList(
				"GET",
				"POST",
				"PUT",
				"DELETE",
				"OPTIONS",
				"PATCH"
		));

		// Permitir todos los headers
		// Incluye: Authorization, Content-Type, y headers personalizados de SockJS
		configuration.setAllowedHeaders(List.of("*"));

		// Permitir credenciales (cookies, Authorization header)
		// CRÍTICO para WebSocket y autenticación JWT
		configuration.setAllowCredentials(true);

		// Headers expuestos al cliente
		// Útil si necesitas leer headers personalizados desde el frontend
		configuration.setExposedHeaders(Arrays.asList(
				"Authorization",
				"Content-Type",
				"X-Total-Count" // Ejemplo: para paginación
		));

		// Tiempo de caché para preflight requests (OPTIONS)
		configuration.setMaxAge(3600L); // 1 hora

		// Registrar la configuración CORS
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		// Aplicar CORS a TODAS las rutas
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	/**
	 * Customizer de seguridad web
	 * <p>
	 * Ignora completamente Spring Security para ciertos endpoints.
	 * IMPORTANTE: Usa esto solo para endpoints que NO necesitan ningún tipo de seguridad.
	 */
    @Bean
    public WebSecurityCustomizer securityCustomizer() {
		return web -> web.ignoring().requestMatchers(
				"/swagger-ui/**", // Swagger UI
				"/api-docs/**" // API documentation
				/*"/wssai/**" */ // WebSocket endpoint (handshake anónimo)
		);
    }
}