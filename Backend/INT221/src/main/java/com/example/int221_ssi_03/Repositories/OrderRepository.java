package com.example.int221_ssi_03.Repositories;

import com.example.int221_ssi_03.Entities.Order;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> , JpaSpecificationExecutor<Order> {
    Page<Order> findBySellerId(Integer SellerId, Pageable pageable);

    Page<Order> findBySellerIdAndIsReadBySellerFalse(Integer sellerId, Pageable pageable);

    Page<Order> findBySellerIdAndOrderStatus(Integer sellerId, String orderStatus, Pageable pageable);

    Integer countBySellerIdAndIsReadBySellerFalse(Integer sellerId);

    static Specification<Order> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String like = "%" + keyword.toLowerCase() + "%";

            Join<Object, Object> sellerJoin = root.join("seller");
            Join<Object, Object> userJoin = sellerJoin.join("user");
            Join<Object, Object> orderDetailJoin = root.join("orderDetails", JoinType.LEFT);
            Join<Object, Object> saleItemJoin = orderDetailJoin.join("saleItem", JoinType.LEFT);
            Join<Object, Object> brandJoin = saleItemJoin.join("brand", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(userJoin.get("nickname")), like),
                    cb.like(cb.lower(brandJoin.get("name")), like),
                    cb.like(cb.lower(saleItemJoin.get("model")), like)
            );
        };
    }

    static Specification<Order> buyerIdEquals(Integer buyerId) {
        return (root, query, cb) -> {
            if (buyerId == null) return null;
            return cb.equal(root.get("buyer").get("id"), buyerId);
        };
    }


    static Specification<Order> buildFilter(Integer buyerId, String keyword) {
        return Specification.where(buyerIdEquals(buyerId))
                .and(keywordLike(keyword));
    }
}
