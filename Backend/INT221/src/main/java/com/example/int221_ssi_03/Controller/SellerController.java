package com.example.int221_ssi_03.Controller;

import com.example.int221_ssi_03.DTO.*;
import com.example.int221_ssi_03.Entities.*;
import com.example.int221_ssi_03.Exception.AccountNotActiveException;
import com.example.int221_ssi_03.Exception.SellerNotFoundException;
import com.example.int221_ssi_03.Exception.UserMismatchException;
import com.example.int221_ssi_03.Mapper.ListMapper;
import com.example.int221_ssi_03.Service.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v2/sellers")
public class SellerController {

    @Autowired
    private SaleItemService saleItemService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private SaleItemImageService saleItemImageService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/{id}/sale-items")
    public ResponseEntity<PageDTO<SaleItemSellerDTO>> sellerSaleItems(@PathVariable Integer id, Authentication authentication,
                                                                       @RequestParam Integer page,
                                                                       @RequestParam(required = false) Integer size,
                                                                       @RequestParam(required = false) String sortDirection,
                                                                       @RequestParam(required = false) String sortField) {
        Map<String, Object> claims = (Map<String, Object>) authentication.getDetails();
        if (!claims.get("id").toString().equals(id.toString())) {
            throw new UserMismatchException("Request user id not matched with id in access token");
        }

        if (!((AuthUserDetail) authentication.getPrincipal()).isActive()){
            throw new AccountNotActiveException("Account is not active");
        }

        Page<SaleItem> saleItemPage = saleItemService.getSaleItemPage(page, size, sortField, sortDirection, id);
        PageDTO<SaleItemSellerDTO> result = listMapper.toPageDTO(saleItemPage, SaleItemSellerDTO.class, modelMapper);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/sale-items")
    public ResponseEntity<?> createSaleItem( @PathVariable Integer id, Authentication authentication,
            @ModelAttribute AddSaleItemDTO newSaleItem,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        Map<String, Object> claims = (Map<String, Object>) authentication.getDetails();
        if (!claims.get("id").toString().equals(id.toString())) {
            throw new UserMismatchException("Request user id not matched with id in access token");
        }

        if (!((AuthUserDetail) authentication.getPrincipal()).isActive()) {
            throw new AccountNotActiveException("Account is not active");
        }

        Brand brand = brandService.getBrandById(newSaleItem.getBrand().getId());

        Seller seller;

        try {
            seller = sellerService.findSellerById(id);
        } catch (Exception e) {
            throw new SellerNotFoundException("Seller not found");
        }

        SaleItem saleItem = modelMapper.map(newSaleItem, SaleItem.class);
        saleItem.setBrand(brand);
        saleItem.setId(null);
        saleItem.setSeller(seller);

        SaleItem savedItem = saleItemService.addSaleItem(saleItem);

        List<Saleitemimage> imageList = saleItemImageService.createAllSaleItemImage(images, savedItem);

        SaleItemDetailWithImageDTO result = modelMapper.map(savedItem, SaleItemDetailWithImageDTO.class);

        if (imageList == null) {
            result.setSaleItemImages(null);
        } else {
            List<SaleItemImageDTO> imagesDTO = listMapper.mapList(imageList, SaleItemImageDTO.class, modelMapper);
            result.setSaleItemImages(imagesDTO);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{sid}/orders")
    public ResponseEntity<PageDTO<OrderResponseBySellerDTO>> getOrdersBySeller(
            @PathVariable Integer sid,
            @AuthenticationPrincipal AuthUserDetail authUser,
            @RequestParam Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String type){

        if (!authUser.getId().equals(sid)) {
            throw new UserMismatchException("Cannot access other seller's orders.");
        }

        return ResponseEntity.ok(orderService.getOrdersBySeller(sid, page, size, sortField, sortDirection, type));
    }

    @GetMapping("/{sid}/orders/{orderId}")
    public ResponseEntity<OrderResponseDetailSellerDTO> getOrderBySellerAndId(
            @PathVariable Integer sid,
            @PathVariable Integer orderId,
            @AuthenticationPrincipal AuthUserDetail authUser
    ) {
        if (!authUser.getId().equals(sid)) {
            throw new UserMismatchException("Cannot access other seller's order.");
        }

        OrderResponseDetailSellerDTO order = orderService.getOrderBySellerAndOrderId(sid, orderId);

        return ResponseEntity.ok(order);
    }

}
