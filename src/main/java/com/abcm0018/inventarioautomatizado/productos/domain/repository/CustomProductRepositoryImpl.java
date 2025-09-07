package com.abcm0018.inventarioautomatizado.productos.domain.repository;

import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Repository
@Slf4j
public class CustomProductRepositoryImpl implements CustomProductRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findProducts(String ean, String brand, String initExpirationDate, String endExpirationDate, String expirationDate, String manufacturedIn) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cbQuery = cb.createQuery(Product.class);

        Root<Product> root = cbQuery.from(Product.class);

        // Crear la lista de predicados (condiciones dinámicas)
        List<Predicate> predicates = new ArrayList<>();

        // Filtros dinámicos según los parámetros

        if (StringUtils.isNotEmpty(ean)) {
            predicates.add(cb.like(cb.upper(root.get("ean")), "%" + ean.toUpperCase() + "%"));
        }

        if (StringUtils.isNotEmpty(brand)) {
            predicates.add(cb.like(cb.upper(root.get("brand")), "%" + brand.toUpperCase() + "%"));
        }

        if (StringUtils.isNotEmpty(manufacturedIn)) {
            predicates.add(cb.like(cb.upper(root.get("manufacturedIn")), "%" + manufacturedIn.toUpperCase() + "%"));
        }

        if (StringUtils.isNotEmpty(initExpirationDate) && StringUtils.isNotEmpty(endExpirationDate)) {
            LocalDate initDate = LocalDate.parse(initExpirationDate, FORMATTER);
            LocalDate endDate = LocalDate.parse(endExpirationDate, FORMATTER);
            predicates.add(cb.between(root.get("expirationDay"), initDate, endDate));
        }

        if (StringUtils.isNotEmpty(expirationDate)) {
            LocalDate date = LocalDate.parse(expirationDate, FORMATTER);
            predicates.add(cb.equal(root.get("expirationDay"), date));
        }

        cbQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        TypedQuery<Product> query = entityManager.createQuery(cbQuery);
        return query.getResultList();
    }
}
