package com.abcm0018.sai.productos.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.abcm0018.sai.productos.domain.enums.PackingLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad que representa el Nivel de Embalaje específico de un producto (Unidad, Caja, Palet)
 * para un producto base. Contiene el GTIN, dimensiones y propiedades logísitcas del producto.
 */
@Getter
@Setter
@Entity
@ToString(exclude = {"product"})
@NoArgsConstructor
@Table(name = "PRODUCT_PACK_LEVELS",
		// Aseguramos que el GTIN sea único para cualquier nivel (EAN-13 o GTIN-14)
		uniqueConstraints = {@UniqueConstraint (name = "uk_ppl_gtin", columnNames = {"GTIN"})},
		// Un ídice para las búsquedas rápidas por GTIN (que será la clave de escaneo)
		indexes = {@Index(name = "idx_ppl_gtin", columnList = "GTIN")}
)
public class ProductPackLevel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Relación N:1 con la entidad Product (El artículo base)
	// Indica a qué producto pertenece este nivel de embalaje
	@ManyToOne
	@JoinColumn(name = "PRODUCT_ID", nullable = false, foreignKey = @ForeignKey(name = "fk_ppl_product"))
	private Product product;

	// Almacena el EAN-13 (Para Caja/Unidad) o el GTIN-14 (Para Palet).
	// Es la clave principal para la relación con el Inventario_Pallets
	@Column(name = "GTIN", nullable = false, length = 14, unique = true)
	private String gtin;

	@Enumerated(EnumType.STRING)
	@Column(name = "PACKING_LEVEL", nullable = false, length = 10)
	private PackingLevel packingLevel; // UNIDAD, CAJA, PALLET

	// Peso neto de este nivel de embalaje (Peso de la lata, peso de la caja, peso del palet)
	@Column(name = "NET_WEIGHT", nullable = false)
	@NotNull(message = "El peso neto es obligatorio")
	@Positive(message = "El peso debe ser mayor a 0")
	@Max(value = 1000000, message = "El peso no puede exceder 1,000,000 kg")
	private BigDecimal netWeight;

	// Alto de esta unidad de embalaje (Alto de la lata, alto de la caja, alto del palet)
	@Column(name = "HEIGHT_MM", nullable = false)
	@Positive(message = "La altura debe ser mayor a 0 mm")
	@Max(value = 3000, message = "La altura no puede exceder 3000 mm")
	private BigDecimal heightMM;

	// Ancho de esta unidad de embalaje
	@Column(name = "WIDTH_MM", nullable = false)
	@Positive(message = "El ancho debe ser mayor a 0 mm")
	@Max(value = 3000, message = "El ancho no puede exceder 3000 mm")
	private BigDecimal widthMM;

	// Cuántas unidades del nivel ANTERIOR contiene este nivel
	// e.g., 2496 para PALLET, 24 para CAJA, 1 para UNIDAD
	@Column(name = "UNITS_IN_LEVEL", nullable = false)
	@NotNull(message = "Unidades por nivel es obligatorio")
	@Min(value = 1, message = "Debe haber al menos 1 unidad por nivel")
	@Max(value = 10000, message = "No pueden haber más de 10,000 unidades por nivel")
	private Integer unitsInLevel;

	// Solo para niveles de agrupación (PALLET/CAJA): Límite de apilado
	@Column(name = "STACKING_LIMIT", nullable = false)
	@NotNull(message = "Límite de apilado es obligatorio")
	@Min(value = 1, message = "El límite de apilado debe ser al menos 1")
	@Max(value = 20, message = "El límite de apilado no puede exceder 20")
	private Integer stackingLimit;

	// Solo para el nivel PALLET: Número de cajas por palet
	@Column(name = "BOXES_PER_PALET", nullable = false)
	@NotNull(message = "Cajas por palet es obligatorio")
	@Min(value = 0, message = "No pueden haber cajas negativas")
	@Max(value = 10000, message = "No pueden haber más de 10,000 cajas por palet")
	private Integer boxesPerPalet;

	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;

	/**
	 * Se ejecuta automáticamente antes de persistir la entidad
	 */
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = null;
	}

	/**
	 * Se ejecuta automáticamente antes de actualizar la entidad
	 */
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// ========== MÉTODOS DE NEGOCIO (MVP FASE 1) ==========
	/**
	 * Valida que la configuración logística sea consistente y válida
	 * <p>
	 * Verifica:
	 * - Peso positivo
	 * - Dimensiones dentro de límites
	 * - Unidades por nivel >= 1
	 * - Límite de apilado >= 1
	 * - Cajas por palet >= 0
	 *
	 * @return true si la configuración es válida, false en caso contrario
	 */
	public boolean isValidConfiguration() {
		return this.netWeight.signum() > 0
				&& this.netWeight.compareTo(BigDecimal.valueOf(1000000)) <= 0
				&& this.heightMM.signum() > 0
				&& this.heightMM.compareTo(BigDecimal.valueOf(3000)) <= 0
				&& this.widthMM.signum() > 0
				&& this.widthMM.compareTo(BigDecimal.valueOf(3000)) <= 0
				&& this.unitsInLevel >= 1
				&& this.unitsInLevel <= 10000
				&& this.stackingLimit >= 1
				&& this.stackingLimit <= 20
				&& this.boxesPerPalet >= 0
				&& this.boxesPerPalet <= 10000;
	}

	/**
	 * Calcula el peso total de un palet completamente lleno
	 * <p>
	 * Fórmula: peso neto × cajas por palet
	 * <p>
	 * Ejemplo: netWeight=500kg, boxesPerPalet=100
	 *          Resultado: 50,000 kg
	 *
	 * @return peso total en kilogramos
	 */
	public BigDecimal getTotalWeightPerPalet() {
		return this.netWeight.multiply(BigDecimal.valueOf(this.boxesPerPalet));
	}

	/**
	 * Genera una descripción legible y completa del nivel de embalaje
	 * <p>
	 * Útil para:
	 * - Logs y auditoría
	 * - Reportes
	 * - Mensajes de error
	 * - Dashboard de operaciones
	 * <p>
	 * Formato:
	 * "PALET | GTIN: 12345678901234 | Peso: 500.00kg | Dims: 1200×1000mm |
	 *  Stack: 5 | Cajas: 100"
	 *
	 * @return descripción formateada del ProductPackLevel
	 */
	public String getFullDescription() {
		return String.format(
				"%s | GTIN: %s | Peso: %.2fkg | Dims: %.0f×%.0fmm | Stack: %d | Cajas: %d",
				this.packingLevel.name(),
				this.gtin,
				this.netWeight.doubleValue(),
				this.heightMM.doubleValue(),
				this.widthMM.doubleValue(),
				this.stackingLimit,
				this.boxesPerPalet
		);
	}
}
