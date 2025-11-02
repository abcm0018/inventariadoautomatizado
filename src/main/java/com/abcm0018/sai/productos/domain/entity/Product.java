package com.abcm0018.sai.productos.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.abcm0018.sai.productos.domain.enums.PackingLevel;
import com.abcm0018.sai.productos.domain.enums.ProductStatus;

@Getter
@Setter
@ToString(exclude = {"packLevels"})
@Entity
@NoArgsConstructor
@Table(name = "PRODUCTS",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_brand_format_code", columnNames = {"BRAND", "FORMAT_CODE"})
		},
		indexes = {
				@Index(name = "idx_product_name", columnList = "NAME"),
				@Index(name = "idx_product_brand", columnList = "BRAND"),
				@Index(name = "idx_product_status", columnList = "STATUS")
		}
)
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "BRAND", nullable = false, length = 200)
	private String brand;

	@Column(name = "DESCRIPTION", length = 500)
	private String description;

	@Column(name = "FORMAT_CODE", nullable = false)
	private String formatCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false)
	private ProductStatus status;

	@Column(name = "MANUFACTURED_IN", nullable = false)
	private String manufacturedIn;

	@OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST, orphanRemoval = true)
	private List<ProductPackLevel> packLevels = new ArrayList<>();

	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;

	// ========== CICLO DE VIDA ==========

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = null;

		if (this.status == null) {
			this.status = ProductStatus.ACTIVE;
		}

		if (this.packLevels == null) {
			this.packLevels = new ArrayList<>();
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// ========== MÉTODOS DE NEGOCIO ==========

	/**
	 * Verifica si el producto está activo y disponible para operaciones
	 *
	 * @return true si el estado es ACTIVE
	 */
	public boolean isActive() {
		return this.status == ProductStatus.ACTIVE;
	}

	/**
	 * Verifica si el producto tiene al menos un nivel de embalaje configurado
	 *
	 * @return true si la lista de packLevels no está vacía
	 */
	public boolean hasPackLevels() {
		return this.packLevels != null && !this.packLevels.isEmpty();
	}

	/**
	 * Obtiene la cantidad de niveles de embalaje configurados
	 *
	 * @return número de ProductPackLevel asociados, 0 si no hay ninguno
	 */
	public Integer countPackLevels() {
		return this.packLevels != null ? this.packLevels.size() : 0;
	}

	/**
	 * Obtiene el nivel de embalaje de un tipo específico
	 *
	 * @param type tipo de embalaje (UNIDAD, CAJA, PALET)
	 * @return Optional con el ProductPackLevel si existe, Optional vacío si no
	 */
	public Optional<ProductPackLevel> getPackLevelByType(PackingLevel type) {
		if (type == null) {
			return Optional.empty();
		}

		if (this.packLevels == null || this.packLevels.isEmpty()) {
			return Optional.empty();
		}

		return this.packLevels.stream()
				.filter(pl -> pl.getPackingLevel() == type)
				.findFirst();
	}

	/**
	 * Verifica si el producto tiene un nivel de embalaje de tipo específico
	 *
	 * @param type tipo de embalaje a validar
	 * @return true si existe un nivel de ese tipo
	 */
	public boolean hasPackLevelType(PackingLevel type) {
		return getPackLevelByType(type).isPresent();
	}

	/**
	 * Valida si el producto es apto para operaciones de inventario
	 * Requiere estar activo y tener al menos un nivel de embalaje
	 *
	 * @return true si es válido para inventario (activo + tiene niveles)
	 */
	public boolean isValidForInventory() {
		return isActive() && hasPackLevels();
	}

	/**
	 * Verifica si el producto ha sido modificado desde su creación
	 *
	 * @return true si updatedAt no es null
	 */
	public boolean hasBeenUpdated() {
		return this.updatedAt != null;
	}

	// ========== GESTIÓN DE RELACIONES BIDIRECCIONALES ==========

	public void addPackLevel(ProductPackLevel packLevel) {
		if (packLevel == null) {
			return;
		}

		if (this.packLevels == null) {
			this.packLevels = new ArrayList<>();
		}

		if (!this.packLevels.contains(packLevel)) {
			this.packLevels.add(packLevel);
			packLevel.setProduct(this);
		}
	}

	public void removePackLevel(ProductPackLevel packLevel) {
		if (packLevel == null || this.packLevels == null) {
			return;
		}

		if (this.packLevels.remove(packLevel)) {
			packLevel.setProduct(null);
		}
	}

	public void clearPackLevels() {
		if (this.packLevels != null) {
			this.packLevels.forEach(pl -> pl.setProduct(null));
			this.packLevels.clear();
		}
	}

}
