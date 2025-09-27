package com.abcm0018.inventarioautomatizado.palets.domain.repository;

import com.abcm0018.inventarioautomatizado.palets.domain.entity.Palet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class CustomPaletRepositoryImpl implements CustomPaletRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Palet> findPalets(String ean, String batchNumber, String packagingDate, String productUseByDate, String productionTime, String shift) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Palet> cbQuery = cb.createQuery(Palet.class);

        Root<Palet> root = cbQuery.from(Palet.class);

        // Crear la lista de predicados (condiciones dinámicas)
        List<Predicate> predicates = new ArrayList<>();

        // Filtros dinámicos según los parámetros

        if (StringUtils.isNotEmpty(ean)) {
            predicates.add(cb.like(cb.upper(root.get("ean")), "%" + ean.toUpperCase() + "%"));
        }

        if (StringUtils.isNotEmpty(batchNumber)) {
            predicates.add(cb.like(cb.upper(root.get("batchNumber")), "%" + batchNumber.toUpperCase() + "%"));
        }

        if (StringUtils.isNotEmpty(shift)) {
            predicates.add(cb.like(cb.upper(root.get("shift")), "%" + shift.toUpperCase() + "%"));
        }

        if (StringUtils.isNotEmpty(productionTime)) {
            predicates.add(cb.like(cb.upper(root.get("time")), "%" + productionTime.toUpperCase() + "%"));
        }

        if (StringUtils.isNotEmpty(packagingDate) && StringUtils.isEmpty(productUseByDate)) {
            LocalDate date = LocalDate.parse(packagingDate, FORMATTER);
            predicates.add(cb.equal(root.get("packagingDate"), date));

        }

        if (StringUtils.isEmpty(packagingDate) && StringUtils.isNotEmpty(productUseByDate)) {
            LocalDate date = LocalDate.parse(productUseByDate, FORMATTER);
            predicates.add(cb.equal(root.get("productUseByDate"), date));
        }

        if (StringUtils.isNotEmpty(packagingDate) && StringUtils.isNotEmpty(productUseByDate)) {
            LocalDate initDate = LocalDate.parse(packagingDate, FORMATTER);
            LocalDate endDate = LocalDate.parse(productUseByDate, FORMATTER);
            predicates.add(cb.between(root.get("productUseByDate"), initDate, endDate));
        }

        cbQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        TypedQuery<Palet> query = entityManager.createQuery(cbQuery);
        return query.getResultList();
    }
}
