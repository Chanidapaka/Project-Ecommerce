package com.example.int221_ssi_03.Controller;

import com.example.int221_ssi_03.DTO.AddBrandDTO;
import com.example.int221_ssi_03.DTO.BrandDTO;
import com.example.int221_ssi_03.DTO.BrandListDto;
import com.example.int221_ssi_03.Entities.Brand;
import com.example.int221_ssi_03.Mapper.ListMapper;
import com.example.int221_ssi_03.Service.BrandService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/brands")
@PreAuthorize("hasAuthority('seller')")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    @GetMapping("")
    public ResponseEntity<List<BrandListDto>> getAllBrands() {
        List<Brand> brands = brandService.getAllBrands();
        List<BrandListDto> dtoList = listMapper.mapList(brands, BrandListDto.class, modelMapper);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getBrandById(@PathVariable int id) {
        Brand brand = brandService.getBrandById(id);
        BrandDTO dto = modelMapper.map(brand, BrandDTO.class);
        dto.setNoOfSaleItems(brand.getSaleItems().size());
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<BrandDTO> addBrand(@RequestBody AddBrandDTO req) {
        Brand brand = modelMapper.map(req, Brand.class);
        Brand saved = brandService.addBrand(brand);
        BrandDTO dto = modelMapper.map(saved, BrandDTO.class);
        dto.setNoOfSaleItems(saved.getSaleItems().size());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDTO> editBrand(
            @PathVariable Integer id,
            @RequestBody AddBrandDTO dto) {

        Brand brand = modelMapper.map(dto, Brand.class);
        brand.setId(id);
        Brand updated = brandService.updateBrandById(id, brand);
        BrandDTO response = modelMapper.map(updated, BrandDTO.class);
        response.setNoOfSaleItems(updated.getSaleItems().size());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrandById(@PathVariable int id) {
        brandService.deleteBrandById(id);
        return ResponseEntity.noContent().build();
    }
}
