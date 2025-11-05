package com.example.int221_ssi_03.Controller;

import com.example.int221_ssi_03.DTO.*;
import com.example.int221_ssi_03.Entities.*;
import com.example.int221_ssi_03.Mapper.ListMapper;
import com.example.int221_ssi_03.Repositories.BrandRepository;
import com.example.int221_ssi_03.Repositories.SellerRepository;
import com.example.int221_ssi_03.Repositories.UserRepository;
import com.example.int221_ssi_03.Service.BrandService;
import com.example.int221_ssi_03.Service.SaleItemImageService;
import com.example.int221_ssi_03.Service.SaleItemService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/sale-items")
@RequiredArgsConstructor
public class SaleItemControllerV2 {

    private final SaleItemService saleItemService;
    private final SaleItemImageService saleItemImageService;
    private final BrandService brandService;
    private final ModelMapper modelMapper;
    private final ListMapper listMapper;


    @GetMapping
    public ResponseEntity<PageDTO<SaleItemGalleryDTO>> getSortedGalleryItems(
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam(required = false) List<Integer> filterStorages,
            @RequestParam(required = false) Integer filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam(required = false) String searchKeyWord,
            @RequestParam Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String sortField
    ) {
        Page<SaleItem> saleItemPage = saleItemService.getSaleItemPage(page, size, sortField, sortDirection,
                filterBrands, filterStorages, filterPriceLower, filterPriceUpper, searchKeyWord);
        PageDTO<SaleItemGalleryDTO> result = listMapper.toPageDTO(saleItemPage, SaleItemGalleryDTO.class, modelMapper);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailWithImageDTO> getSaleItemById(@PathVariable Integer id) {
        SaleItem entity = saleItemService.getSaleItemEntityById(id);
        List<Saleitemimage> images = saleItemImageService.getAllSaleItemImageBySaleItemId(id);
        List<SaleItemImageDTO> imagesDTO = listMapper.mapList(images, SaleItemImageDTO.class, modelMapper);

        SaleItemDetailWithImageDTO result = modelMapper.map(entity, SaleItemDetailWithImageDTO.class);
        result.setSaleItemImages(imagesDTO);

        return ResponseEntity.ok(result);
    }


    @PutMapping("/{id}")
    public ResponseEntity<SaleItemDetailWithImageDTO> updateSaleItem(
            @ModelAttribute SaleItemWithImageInfo updateSaleItem, @PathVariable Integer id) {

        SaleItemDetailWithImageDTO result;

        if (updateSaleItem.getSaleItem() != null) {
            Brand brand = brandService.getBrandById(updateSaleItem.getSaleItem().getBrand().getId());

            SaleItem saleItem = modelMapper.map(updateSaleItem.getSaleItem(), SaleItem.class);
            saleItem.setBrand(brand);
            saleItem.setId(id);

            SaleItem updated = saleItemService.updateSaleItemById(id, saleItem);
            result = modelMapper.map(updated, SaleItemDetailWithImageDTO.class);
        } else {
            result = modelMapper.map(saleItemService.getSaleItemEntityById(id), SaleItemDetailWithImageDTO.class);
        }

        List<Saleitemimage> images;
        if (updateSaleItem.getImageInfos() != null && !updateSaleItem.getImageInfos().isEmpty()) {
            images = saleItemImageService.updateImageBySaleItemId(updateSaleItem.getImageInfos(), result.getId());
        } else {
            images = saleItemImageService.getAllSaleItemImageBySaleItemId(id);
        }

        List<SaleItemImageDTO> imagesDTO = listMapper.mapList(images, SaleItemImageDTO.class, modelMapper);
        result.setSaleItemImages(imagesDTO);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaleItem(@PathVariable Integer id) {
        saleItemService.deleteSaleItemById(id);
        saleItemImageService.deleteAllSaleItemImageBySaleItemId(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/storages")
    public ResponseEntity<List<String>> getAllStorages() {
        return ResponseEntity.ok(saleItemService.getAllDistinctStorages());
    }
}
