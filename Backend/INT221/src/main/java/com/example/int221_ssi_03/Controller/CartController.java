package com.example.int221_ssi_03.Controller;

import com.example.int221_ssi_03.DTO.CartDTO;
import com.example.int221_ssi_03.DTO.RequestAddToCartDTO;
import com.example.int221_ssi_03.DTO.ResponseAddToCartDTO;
import com.example.int221_ssi_03.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/{uid}")
    public ResponseEntity<List<CartDTO>> getCart(@PathVariable Integer uid) {
        return ResponseEntity.ok(cartService.findAllCartItemByUserId(uid));
    }

    @PostMapping("/{uid}")
    public ResponseEntity<ResponseAddToCartDTO> addToCart(
            @PathVariable Integer uid,
            @RequestBody RequestAddToCartDTO request) {
        ResponseAddToCartDTO response = cartService.addToCart(uid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{cid}")
    public ResponseEntity<ResponseAddToCartDTO> updateCart(@PathVariable Integer cid,
                                              @RequestBody RequestAddToCartDTO request){
        ResponseAddToCartDTO response = cartService.updateCart(cid, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cid}")
    public ResponseEntity<Void> deleteCart(@PathVariable Integer cid) {
        cartService.deleteCart(cid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uid}/select/{sid}")
    public ResponseEntity<List<CartDTO>> updateSelectCartByUserAndSeller(@PathVariable Integer uid,
                                                                         @PathVariable Integer sid,
                                                                         @RequestParam boolean selected){
        List<CartDTO> updatedCarts = cartService.setSelectedByUserAndSeller(uid, sid, selected);
        return ResponseEntity.ok(updatedCarts);
    }

    @PutMapping("/{uid}/select")
    public ResponseEntity<List<CartDTO>> updateSelectCartByUser(
            @PathVariable Integer uid,
            @RequestParam boolean selected) {

        List<CartDTO> updatedCarts = cartService.setSelectedByUser(uid, selected);
        return ResponseEntity.ok(updatedCarts);
    }
}
