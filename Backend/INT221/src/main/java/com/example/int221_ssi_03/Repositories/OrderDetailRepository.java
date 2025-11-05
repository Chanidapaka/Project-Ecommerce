package com.example.int221_ssi_03.Repositories;

import com.example.int221_ssi_03.Entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
}
