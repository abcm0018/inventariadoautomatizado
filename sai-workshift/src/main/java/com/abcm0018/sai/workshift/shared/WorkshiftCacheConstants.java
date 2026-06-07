package com.abcm0018.sai.workshift.shared;

import java.time.Duration;

/**
 * Constantes de caché Redis compartidas entre {@code WorkshiftServiceImpl}
 * y {@code WorkshiftCacheScheduler}.
 * <p>
 * Centralizar aquí evita que ambas clases tengan copias independientes que
 * puedan divergir silenciosamente.
 */
public final class WorkshiftCacheConstants {

    private WorkshiftCacheConstants() {}

    /** Patrón de clave Redis: {@code workshift:user:{userId}:date:{date}} */
    public static final String KEY_PATTERN = "workshift:user:%d:date:%s";

    /** TTL estándar para turnos del día actual: 24 horas. */
    public static final Duration TTL = Duration.ofHours(24);

    /** TTL extendido para precarga de turnos de la semana siguiente: 7 días. */
    public static final Duration TTL_WEEK = Duration.ofDays(7);
}
