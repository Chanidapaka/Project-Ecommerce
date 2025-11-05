package com.example.int221_ssi_03.Repositories;

import com.example.int221_ssi_03.Entities.Cart;

import jakarta.transaction.Transactional;

import com.example.int221_ssi_03.Entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    @Transactional
    void deleteByUser_IdAndSaleItem_Id(Integer userId, Integer saleItemId);

    List<Cart> findByUser(User user);

    @Modifying
    @Query("UPDATE Cart c SET c.selected = :selected WHERE c.user.id = :userId")
    int updateSelectedByUser(@Param("userId") Integer userId, @Param("selected") boolean selected);

    @Modifying
    @Query("UPDATE Cart c SET c.selected = :selected WHERE c.user.id = :userId AND c.saleItem.seller.id = :sellerId")
    int updateSelectedByUserAndSeller(@Param("userId") Integer userId,
                                      @Param("sellerId") Integer sellerId,
                                      @Param("selected") boolean selected);

}
