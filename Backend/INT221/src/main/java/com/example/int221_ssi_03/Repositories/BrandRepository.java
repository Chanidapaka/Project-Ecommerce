package com.example.int221_ssi_03.Repositories;

import com.example.int221_ssi_03.Entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository <Brand, Integer> {
    boolean existsByName(String name);

    Optional<Brand> findByName(String name);

}
