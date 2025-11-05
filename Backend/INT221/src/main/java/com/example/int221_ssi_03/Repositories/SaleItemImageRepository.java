package com.example.int221_ssi_03.Repositories;

import com.example.int221_ssi_03.Entities.Saleitemimage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SaleItemImageRepository  extends JpaRepository<Saleitemimage, Integer> {
    List<Saleitemimage> findAllBySaleItemIdOrderByImageViewOrderAsc(Integer saleItemId);

    Saleitemimage findBySaleItemIdAndFileName(Integer saleItemId, String fileName);

    @Modifying
    @Transactional
    @Query("DELETE FROM Saleitemimage si WHERE si.saleItem.id = :saleItemId")
    void deleteAllBySaleItemId(@Param("saleItemId") Integer saleItemId);
}
