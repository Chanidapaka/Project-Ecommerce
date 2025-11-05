package com.example.int221_ssi_03.Controller;

import com.example.int221_ssi_03.DTO.AddSaleItemDTO;
import com.example.int221_ssi_03.DTO.SaleItemDetailDTO;
import com.example.int221_ssi_03.DTO.SaleItemGalleryDTO;
import com.example.int221_ssi_03.Entities.Brand;
import com.example.int221_ssi_03.Entities.SaleItem;
import com.example.int221_ssi_03.Service.BrandService;
import com.example.int221_ssi_03.Service.SaleItemService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/sale-items")
public class SaleItemController {
    @Autowired
    private SaleItemService saleItemService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BrandService brandService;

    @GetMapping
    public List<SaleItemGalleryDTO> getAllSaleItems() {
        return saleItemService.getAllSaleItemEntitiesSorted().stream()
                .map(item -> modelMapper.map(item, SaleItemGalleryDTO.class))
                .toList();
    }

    @GetMapping("/{id}")
    public SaleItemDetailDTO getSaleItemById(@PathVariable Integer id) {
        SaleItem entity = saleItemService.getSaleItemEntityById(id);
        return modelMapper.map(entity, SaleItemDetailDTO.class);
    }

    @PostMapping
    public ResponseEntity<SaleItemDetailDTO> addSaleItem(@RequestBody AddSaleItemDTO addSaleItemDTO) {
        Brand brand = brandService.getBrandById(addSaleItemDTO.getBrand().getId());

        SaleItem saleItem = modelMapper.map(addSaleItemDTO, SaleItem.class);
        saleItem.setBrand(brand);
        saleItem.setId(null);

        SaleItem savedItem = saleItemService.addSaleItem(saleItem);
        SaleItemDetailDTO result = modelMapper.map(savedItem, SaleItemDetailDTO.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleItemDetailDTO> editSaleItemDetailDTO(@PathVariable Integer id, @RequestBody AddSaleItemDTO saleItemDTO) {
        Brand brand = brandService.getBrandById(saleItemDTO.getBrand().getId());

        SaleItem saleItem = modelMapper.map(saleItemDTO, SaleItem.class);
        saleItem.setBrand(brand);
        saleItem.setId(id);

        SaleItem updated = saleItemService.updateSaleItemById(id, saleItem);
        SaleItemDetailDTO result = modelMapper.map(updated, SaleItemDetailDTO.class);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaleItem(@PathVariable Integer id) {
        saleItemService.deleteSaleItemById(id);
        return ResponseEntity.noContent().build();
    }
}
