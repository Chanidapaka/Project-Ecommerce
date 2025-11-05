package com.example.int221_ssi_03.Service;

import com.example.int221_ssi_03.Entities.SaleItem;
import com.example.int221_ssi_03.Exception.DataConstraintViolationException;
import com.example.int221_ssi_03.Exception.ItemNotFoundException;
import com.example.int221_ssi_03.Repositories.SaleItemRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SaleItemService {
    @Autowired
    private SaleItemRepository saleItemRepo;

    @Autowired
    private EntityManager entityManager;

    public List<SaleItem> getAllSaleItemEntitiesSorted() {
        return saleItemRepo.findAllByOrderByCreatedOnAscIdAsc();
    }

    public SaleItem getSaleItemEntityById(Integer id) {
        return saleItemRepo.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Sale item not found for this id :: " + id));
    }

    @Transactional
    public SaleItem addSaleItem(SaleItem saleItem) {
        if (!isValidSaleItem(saleItem)) {
            throw new DataConstraintViolationException("Sale Item create Failed");
        }
        try {
            SaleItem added = saleItemRepo.saveAndFlush(saleItem);
            entityManager.refresh(added);
            return getSaleItemEntityById(added.getId());
        } catch (Exception e) {
            throw new DataConstraintViolationException("Sale Item create Failed");
        }
    }

    @Transactional
    public SaleItem updateSaleItemById(Integer id, SaleItem saleItem) {
        SaleItem oldSeleItem = getSaleItemEntityById(id);
        if (!isValidSaleItem(saleItem)) {
            throw new DataConstraintViolationException("Sale Item Update Failed");
        }
        try {
            saleItem.setSeller(oldSeleItem.getSeller());
            SaleItem updated = saleItemRepo.saveAndFlush(saleItem);
            entityManager.refresh(updated);
            return getSaleItemEntityById(updated.getId());
        } catch (Exception e) {
            throw new DataConstraintViolationException("Sale Item Update Failed");
        }
    }

    public void deleteSaleItemById(Integer id) {
        SaleItem item = getSaleItemEntityById(id);
        saleItemRepo.delete(item);
    }

    public Page<SaleItem> getSaleItemPage(Integer page, Integer size, String sortField, String sortDirection,
                                          List<String> filterBrands, List<Integer> filterStorages,
                                          Integer filterPriceLower, Integer filterPriceUpper,
                                          String searchKeyWord){
        return getSaleItemPage(page, size, sortField, sortDirection, filterBrands,filterStorages, filterPriceLower, filterPriceUpper, searchKeyWord, null);
    }

    public Page<SaleItem> getSaleItemPage(Integer page, Integer size, String sortField, String sortDirection, Integer userId){
        return getSaleItemPage(page, size, sortField, sortDirection, null,null, null, null, null, userId);
    }

    public Page<SaleItem> getSaleItemPage(Integer page, Integer size, String sortField, String sortDirection,
                                          List<String> filterBrands, List<Integer> filterStorages,
                                          Integer filterPriceLower, Integer filterPriceUpper,
                                          String searchKeyWord, Integer userId) {
        sortField = (sortField == null || sortField.trim().isEmpty()) ? "createdOn" : sortField.trim();
        sortDirection = (sortDirection == null || sortDirection.trim().isEmpty()) ? "asc" : sortDirection.trim();
        size = (size == null || size <= 0) ? 10 : size;

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Order.desc(sortField), Sort.Order.asc("id"))
                : Sort.by(Sort.Order.asc(sortField), Sort.Order.asc("id"));

        Pageable pageable = PageRequest.of(page, size, sort);

        boolean includeNullStorage = false;
        if (filterStorages != null) {
            if (filterStorages.isEmpty()) {
                includeNullStorage = true;
            } else if (filterStorages.contains(null)) {
                filterStorages = filterStorages.stream().filter(Objects::nonNull).toList();
                includeNullStorage = true;
            }
        }

        if (filterPriceLower == null || filterPriceUpper == null) {
            filterPriceLower = null;
            filterPriceUpper = null;
        } else if (filterPriceLower > filterPriceUpper) {
            Integer temp = filterPriceUpper;
            filterPriceUpper = filterPriceLower;
            filterPriceLower = temp;
        }

        Specification<SaleItem> spec = Specification
                .where(SaleItemRepository.brandIn(filterBrands))
                .and(SaleItemRepository.storageIn(filterStorages, includeNullStorage))
                .and(SaleItemRepository.priceBetween(filterPriceLower, filterPriceUpper))
                .and(SaleItemRepository.keywordLike(searchKeyWord))
                .and(SaleItemRepository.userIdEquals(userId));

        return saleItemRepo.findAll(spec, pageable);
    }

    public boolean isValidSaleItem(SaleItem saleItem) {
        return saleItem.getBrand() != null && saleItem.getModel() != null && saleItem.getDescription() != null && saleItem.getPrice() != null;
    }

    public List<String> getAllDistinctStorages() {
        List<Integer> storages = saleItemRepo.findAllDistinctStorages();

        List<String> result = storages.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));

        if (storages.contains(null)) {
            result.add(null);
        }

        return result;
    }

}
