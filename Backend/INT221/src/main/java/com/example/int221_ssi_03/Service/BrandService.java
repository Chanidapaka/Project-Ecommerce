package com.example.int221_ssi_03.Service;

import com.example.int221_ssi_03.Entities.Brand;
import com.example.int221_ssi_03.Exception.BrandHasSaleItemException;
import com.example.int221_ssi_03.Exception.DataConstraintViolationException;
import com.example.int221_ssi_03.Exception.DuplicateItemException;
import com.example.int221_ssi_03.Exception.ItemNotFoundException;
import com.example.int221_ssi_03.Repositories.BrandRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BrandService {
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private EntityManager entityManager;

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Brand getBrandById(int id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Brand not found for this id :: " + id));
    }

    @Transactional
    public Brand addBrand(Brand req) {
        if (brandRepository.existsByName(req.getName())) {
            throw new DuplicateItemException("Duplicate Brand name");
        }

        if (req.getName().isEmpty()){
            throw new DataConstraintViolationException("Brand create Failed");
        }

        try {
            Brand saved = brandRepository.saveAndFlush(req);
            entityManager.refresh(saved);
            return saved;
        } catch (Exception e){
            throw new DataConstraintViolationException("Brand create Failed");
        }
    }

    @Transactional
    public Brand updateBrandById(Integer id, Brand brandInput) {
        Brand existing = brandRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Brand not found"));

        if (brandInput.getName().isEmpty()) {
            throw new DataConstraintViolationException("Brand name cannot be null");
        }

        Optional<Brand> duplicate = brandRepository.findByName(brandInput.getName());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateItemException("Brand name already exists");
        }

        try {
            existing.setName(brandInput.getName());
            existing.setWebsiteUrl(brandInput.getWebsiteUrl());
            existing.setCountryOfOrigin(brandInput.getCountryOfOrigin());
            existing.setIsActive(brandInput.getIsActive());

            Brand updated = brandRepository.saveAndFlush(existing);
            entityManager.refresh(updated);
            return updated;
        } catch (Exception e) {
            throw new DataConstraintViolationException("Brand update failed");
        }
    }

    public void deleteBrandById(int id) {
        Brand brand = getBrandById(id);
        if (brand.getSaleItems().size() > 0){
            throw new BrandHasSaleItemException("Brand has sale items");
        }
        brandRepository.deleteById(id);
    }
}
