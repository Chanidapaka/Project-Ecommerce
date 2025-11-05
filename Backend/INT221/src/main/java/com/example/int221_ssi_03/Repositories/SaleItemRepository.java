package com.example.int221_ssi_03.Repositories;

import com.example.int221_ssi_03.Entities.SaleItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer>, JpaSpecificationExecutor<SaleItem> {
    List<SaleItem> findAllByOrderByCreatedOnAscIdAsc();
    Page<SaleItem> findAll(Pageable pageable);

    @Query("SELECT DISTINCT s.storageGb FROM SaleItem s ORDER BY s.storageGb ASC")
    List<Integer> findAllDistinctStorages();

    static Specification<SaleItem> brandIn(List<String> brandNames) {
        return (root, query, cb) -> {
            if (brandNames == null || brandNames.isEmpty()) return null;
            return cb.lower(root.get("brand").get("name")).in(
                    brandNames.stream().map(String::toLowerCase).toList()
            );
        };
    }

    static Specification<SaleItem> storageIn(List<Integer> storages, boolean includeNull) {
        return (root, query, cb) -> {
            if ((storages == null || storages.isEmpty()) && !includeNull) return null;
            if (storages != null && !storages.isEmpty() && includeNull) {
                return cb.or(root.get("storageGb").in(storages), cb.isNull(root.get("storageGb")));
            }
            if (storages != null && !storages.isEmpty()) {
                return root.get("storageGb").in(storages);
            }
            return cb.isNull(root.get("storageGb"));
        };
    }

    static Specification<SaleItem> priceBetween(Integer lower, Integer upper) {
        return (root, query, cb) -> {
            if (lower != null && upper != null) {
                return cb.between(root.get("price"), lower, upper);
            } else if (lower != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), lower);
            } else if (upper != null) {
                return cb.lessThanOrEqualTo(root.get("price"), upper);
            }
            return null;
        };
    }

    static Specification<SaleItem> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("model")), like),
                    cb.like(cb.lower(root.get("color")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }

    static Specification<SaleItem> userIdEquals(Integer userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.equal(root.get("seller").get("id"), userId);
        };
    }
}
