package com.abcm0018.sai.shared.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate; // Importado
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer; // Importado
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer; // Importado

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.micrometer.common.lang.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

	// Define los TTL (Tiempos de Vida) para diferentes cachés
	private static final Duration TTL_DEFAULT = Duration.ofMinutes(60);
	private static final Duration TTL_USERS = Duration.ofMinutes(120);
	private static final Duration TTL_SHIFTS = Duration.ofMinutes(240);
	private static final Duration TTL_PALETS = Duration.ofMinutes(30);
	private static final Duration TTL_PRODUCTOS = Duration.ofHours(8);

	// ===================================================================
	// ¡¡AQUÍ ESTÁ EL BEAN QUE FALTABA!!
	// ===================================================================
	/**
	 * Define el bean RedisTemplate<String, Long> que el WorkshiftCacheScheduler
	 * inyecta manualmente.
	 * <p>
	 * Esto asegura que las claves se serialicen como Strings y los valores
	 * (Longs) también se guarden como Strings (ej. "123").
	 */
	@Bean
	public RedisTemplate<String, Long> workshiftRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Long> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);

		// Serializador para las CLAVES (Keys)
		template.setKeySerializer(new StringRedisSerializer());

		// Serializador para los VALORES (Values)
		// Usamos GenericToStringSerializer para convertir el Long a un String
		template.setValueSerializer(new GenericToStringSerializer<>(Long.class));

		// (Opcional pero buena práctica)
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));

		template.afterPropertiesSet();

		log.info("✅ Bean workshiftRedisTemplate (String, Long) configurado.");
		return template;
	}

	// ===================================================================
	// CONFIGURACIÓN PARA EL CacheManager (Anotaciones @Cacheable)
	// ===================================================================

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

		// Configuración para diferentes cachés
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();


		// Cachés del módulo 'users'
		cacheConfigurations.put("users", redisCacheConfiguration(TTL_USERS));

		// Cachés del módulo 'shift'
		cacheConfigurations.put("shifts_all", redisCacheConfiguration(TTL_SHIFTS));
		cacheConfigurations.put("shifts_active", redisCacheConfiguration(TTL_SHIFTS));
		cacheConfigurations.put("shifts_inactive", redisCacheConfiguration(TTL_SHIFTS));

		// Cachés del módulo 'palets' y 'products' (¡Añadidas de nuevo!)
		cacheConfigurations.put("palets", redisCacheConfiguration(TTL_PALETS));
		cacheConfigurations.put("productos", redisCacheConfiguration(TTL_PRODUCTOS));
		cacheConfigurations.put("product_pack_levels", redisCacheConfiguration(TTL_PRODUCTOS));

		log.info("Configurando Redis Cache Manager con TTLs personalizados...");

		return RedisCacheManager.builder(redisConnectionFactory)
				.cacheDefaults(redisCacheConfiguration(TTL_DEFAULT)) // Configuración por defecto
				.withInitialCacheConfigurations(cacheConfigurations) // Aplicar TTLs específicos
				.build();
	}

	/**
	 * Configuración de caché por defecto (TTL_DEFAULT y Serializador JSON)
	 */
	private RedisCacheConfiguration redisCacheConfiguration(Duration ttlDefault) {
		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(ttlDefault) // TTL por defecto
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer()))
				.disableCachingNullValues(); // No cachear nulos
	}

	/**
	 * Serializador JSON (para el CacheManager)
	 */
	private GenericJackson2JsonRedisSerializer jsonSerializer() {
		return new GenericJackson2JsonRedisSerializer(objectMapper());
	}

	/**
	 * ObjectMapper configurado para incluir tipos (para polimorfismo)
	 * y manejar correctamente las fechas (JavaTimeModule).
	 */
	private ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
				ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
		return objectMapper;
	}

	// ===================================================================
	// MANEJADOR DE ERRORES DE CACHÉ
	// ===================================================================

	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new SimpleCacheErrorHandler() {
			@Override
			public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
				log.warn("Error obteniendo del cache '{}' con clave '{}': {}", cache.getName(), key, exception.getMessage());
			}

			@Override
			public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key, Object value) {
				log.warn("Error guardando en cache '{}' con clave '{}': {}", cache.getName(), key, exception.getMessage());
			}

			@Override
			public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
				log.warn("Error eliminando del cache '{}' con clave '{}': {}", cache.getName(), key, exception.getMessage());
			}

			@Override
			public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
				log.warn("Error limpiando cache '{}': {}", cache.getName(), exception.getMessage());
			}
		};
	}
}