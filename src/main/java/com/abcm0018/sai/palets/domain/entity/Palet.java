package com.abcm0018.sai.palets.domain.entity;

import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entidad que representa un Palet físico en el inventario.
 * <p>
 * CAMBIO ARQUITECTÓNICO:
 * - Relación directa: Palet -> ProductPackLevel (específico del embalaje)
 * - A través de ProductPackLevel accedemos al Product base
 * - El GTIN-14 del palet se obtiene de ProductPackLevel.gtin
 * <p>
 * ESTRUCTURA JERÁRQUICA:
 * Palet -> ProductPackLevel -> Product
 * <p>
 * Esto garantiza que cada palet está vinculado a un nivel de embalaje específico,
 * con toda la información logística (peso, dimensiones, GTIN) necesaria.
 */
@Getter
@Setter
@ToString(exclude = {"productPackLevel", "user", "workshift"})
@Entity
@NoArgsConstructor
@Table(name = "PALETS",
		uniqueConstraints = {@UniqueConstraint(name = "uk_palet_sscc", columnNames = {"SSCC"})},
		indexes = {
			@Index(name = "idx_palet_sscc", columnList = "SSCC"),
			@Index(name = "idx_palet_pack_level", columnList = "PACK_LEVEL_ID"),
			@Index(name = "idx_palet_user", columnList = "EMPLOYEE_NUMBER"),
			@Index(name = "idx_palet_workshift", columnList = "WORKSHIFT_ID"),
			@Index(name = "idx_palet_batch", columnList = "BATCH_NUMBER"),
			@Index(name = "idx_palet_packaging_date", columnList = "PACKAGIN_DATE"),
			@Index(name = "idx_palet_expiry_date", columnList = "PRODUCT_USE_BY_DATE"),
			@Index(name = "idx_palet_created_at", columnList = "CREATE_AT")
		}
)
public class Palet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	/*
	 * Serial Shipping Container Code
	 * Código único de 18 dígitos que identifica al palet en el sistema logístico
	 */
	@Column(name = "SSCC", nullable = false, length = 18, unique = true)
	private String sscc;

	/**
	 * Número de lote del producto
	 * Agrupa múltiples palets del mismo lote para trazabilidad
	 */
    @Column(name = "BATCH_NUMBER", nullable = false)
    private String batchNumber;

	/**
	 * RELACIÓN PRINCIPAL: Palet vinculado a un Nivel de Embalaje específico
	 * <p>
	 * A través de ProductPackLevel accedemos a:
	 * - El GTIN-14 (código del palet)
	 * - Las características del embalaje (peso, dimensiones)
	 * - El Product base (para contexto general)
	 * <p>
	 * VENTAJAS:
	 * - Trazabilidad completa: sabemos el GTIN exacto del palet
	 * - Información logística centralizada en ProductPackLevel
	 * - Consultas más precisas y eficientes
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "PACK_LEVEL_ID", nullable = false)
	private ProductPackLevel productPackLevel;

	/**
	 * Usuario que escaneó el palet (operador que registró la entrada al sistema)
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EMPLOYEE_NUMBER", referencedColumnName = "EMPLOYEE_NUMBER")
	private User user; // Muchos palets pueden ser escaneados por el mismo empleado

	/**
	 * Turno en el que se produjo/registró el palet
	 * Necesario para trazabilidad y análisis de producción
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "WORKSHIFT_ID", referencedColumnName = "ID")
	private Workshift workshift;

	/**
	 * Fecha en la que se empaqueó el producto
	 * Importante para gestión FIFO
	 */
    @Column(name = "PACKAGIN_DATE", nullable = false)
    private LocalDate packagingDate;

	/**
	 * Fecha de caducidad del producto
	 * Crítica para gestión de inventario y alertas
	 */
    @Column(name = "PRODUCT_USE_BY_DATE", nullable = false)
    private LocalDate productUseByDate;

	/**
	 * Hora de producción (formato HH:mm)
	 * Para registros detallados de trazabilidad
	 */
    @Column(name = "PRODUCTION_TIME", nullable = false)
    private String productionTime;

	/**
	 * Fecha en la que el operador escaneó el palet
	 */
	@Column(name = "SCANNED_AT", nullable = false)
	private LocalDate scannedAt;

	/**
	 * Timestamp de creación del registro
	 * Inmutable después de la creación
	 */
    @Column(name = "CREATE_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

	/**
	 * Timestamp de última actualización
	 * Se actualiza en cada cambio
	 */
    @Column(name = "UPDATE_AT")
    private LocalDateTime updatedAt;

	public static Palet createNewPalet(String sscc, String batchNumber, ProductPackLevel packLevel,
			LocalDate packagingDate, LocalDate productUseByDate, String productionTime, LocalDate scannedAt, User user, Workshift workshift) {

		// Validación de los Datos críticos
		if (sscc == null || user == null || workshift == null || packLevel == null) {
			throw new IllegalArgumentException("Todos los datos son obligatorios");
		}

		Palet palet = new Palet();

		palet.setSscc(sscc);
		palet.setBatchNumber(batchNumber);
		palet.setProductPackLevel(packLevel);
		palet.setPackagingDate(packagingDate);
		palet.setProductUseByDate(productUseByDate);
		palet.setProductionTime(productionTime);
		palet.setScannedAt(scannedAt);
		palet.setUser(user);
		palet.setWorkshift(workshift);

		return palet;
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = null;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Para acceder al Producto base
	 * A través de la relación: Palet -> ProductPackLevel -> Product
	 */
	public Product getProduct() {
		return this.productPackLevel.getProduct();
	}

	public String getGtin() {
		return this.productPackLevel.getGtin();
	}

	/**
	 * Verifica si el palet ha caducado
	 * @return TRUE si ha expirado, FALSE en caso contrario
	 */
	public boolean isExpired() {
		return this.productUseByDate.isBefore(LocalDate.now());
	}

	/**
	 * Verifica si el palé está próximo a caducar (dentro de 7 días)
	 */
	public boolean isExpiringSoon() {
		return this.productUseByDate.isBefore(LocalDate.now().plusDays(7));
	}

	/**
	 * Verifica si el palé está en estado crítico de expiración (3 días o menos)
	 */
	public boolean isCriticalExpity() {
		return this.productUseByDate.isBefore(LocalDate.now().plusDays(3));
	}

	/**
	 * Calcula los días restantes hasta la caducidad
	 */
	public long getDaysUntilExpiry() {
		return ChronoUnit.DAYS.between(LocalDate.now(), this.productUseByDate);
	}
}
