package com.example.int221_ssi_03.Repositories;

import com.example.int221_ssi_03.Entities.Seller;
import com.example.int221_ssi_03.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Integer> {
    Optional<Seller> findByUser(User currentUser);
}
