package com.example.int221_ssi_03.Service;

import com.example.int221_ssi_03.DTO.CartDTO;
import com.example.int221_ssi_03.DTO.CartItemDTO;
import com.example.int221_ssi_03.DTO.RequestAddToCartDTO;
import com.example.int221_ssi_03.DTO.ResponseAddToCartDTO;
import com.example.int221_ssi_03.Entities.*;
import com.example.int221_ssi_03.Exception.DataConstraintViolationException;
import com.example.int221_ssi_03.Exception.InsufficientStockException;
import com.example.int221_ssi_03.Exception.ItemNotFoundException;
import com.example.int221_ssi_03.Repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private SaleItemService saleItemService;

    public Cart getCartById(Integer cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new ItemNotFoundException("Cart not found"));
    }

    public ResponseAddToCartDTO addToCart(Integer userId, RequestAddToCartDTO request) {

        User user = userService.findUserById(userId);

        SaleItem saleItem = saleItemService.getSaleItemEntityById(request.getSaleItemId());

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if(user.getId().equals(saleItem.getSeller().getId())){
            throw new IllegalArgumentException("You cannot add your sale item to your cart.");
        }

        if (request.getQuantity() > saleItem.getQuantity()) {
            throw new InsufficientStockException("Not enough stock available");
        }

        Cart cart = cartRepository.findByUser(user).stream()
                .filter(c -> c.getSaleItem().getId().equals(saleItem.getId()))
                .findFirst()
                .map(existing -> {
                    int newQty = existing.getQuantity() + request.getQuantity();
                    if (newQty > saleItem.getQuantity()) {
                        throw new InsufficientStockException("Not enough stock available");
                    }
                    existing.setQuantity(newQty);
                    return existing;
                })
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setSaleItem(saleItem);
                    newCart.setQuantity(request.getQuantity());
                    newCart.setSelected(true);
                    return newCart;
                });
        Cart saved = cartRepository.save(cart);

        ResponseAddToCartDTO response = toResponseAddToCartDTO(userId, saved, "Item added to cart successfully");
        return response;
    }

    public ResponseAddToCartDTO updateCart(Integer cartId, RequestAddToCartDTO request) {
        SaleItem saleItem = saleItemService.getSaleItemEntityById(request.getSaleItemId());

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findById(cartId).orElse(null);

        if (cart == null) {
            return addToCart(request.getUserId(), request);
        }

        if (request.getQuantity() > cart.getQuantity() && request.getQuantity() > saleItem.getQuantity()) {
            throw new InsufficientStockException("Not enough stock available");
        }

        try {
            cart.setQuantity(request.getQuantity());
            cart.setSelected(request.isSelected());
            Cart saved = cartRepository.save(cart);
            return toResponseAddToCartDTO(cart.getUser().getId(), saved, "Item updated to cart successfully");
        } catch (Exception e) {
            throw new DataConstraintViolationException("Cart Update Failed");
        }
    }

    public List<CartDTO> findAllCartItemByUserId(Integer userId) {
        User user = userService.findUserById(userId);
        List<Cart> cartList = cartRepository.findByUser(user);
        Map<Seller, List<Cart>> groupedCart = cartList.stream()
                .collect(Collectors.groupingBy(c -> c.getSaleItem().getSeller()));

        List<CartDTO> listCartDTO = groupedCart.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getId()))
                .map(entry -> toCartDTO(user, entry.getKey(), entry.getValue()))
                .toList();

        return listCartDTO;
    }

    public void deleteCart(Integer cartId) {
        Cart cart = getCartById(cartId);
        cartRepository.delete(cart);
    }

    public void deleteCartByUserIdAndSaleItemId(Integer userId, Integer saleItemId) {
        cartRepository.deleteByUser_IdAndSaleItem_Id(userId, saleItemId);
    }

    @Transactional
    public List<CartDTO> setSelectedByUser(Integer userId, boolean selected) {
        int updatedCount = cartRepository.updateSelectedByUser(userId, selected);
        if (updatedCount == 0) {
            throw new ItemNotFoundException("No cart items found for userId " + userId);
        }
        return findAllCartItemByUserId(userId);
    }

    @Transactional
    public List<CartDTO> setSelectedByUserAndSeller(Integer userId, Integer sellerId, boolean selected) {
        int updatedCount = cartRepository.updateSelectedByUserAndSeller(userId, sellerId, selected);
        if (updatedCount == 0) {
            throw new ItemNotFoundException("No cart items found for userId " + userId + " and sellerId " + sellerId);
        }
        return findAllCartItemByUserId(userId);
    }

    public CartDTO toCartDTO(User buyer, Seller seller, List<Cart> cartItems) {
        CartDTO dto = new CartDTO();
        dto.setBuyerId(buyer.getId());
        dto.setSeller(sellerService.toSellerDetailDTO(seller));
        dto.setCartItems(cartItems.stream()
                .map(this::toCartItemDTO)
                .toList());
        return dto;
    }

    public CartItemDTO toCartItemDTO(Cart cart) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartId(cart.getId());
        dto.setSaleItemId(cart.getSaleItem().getId());
        dto.setOrderQuantity(cart.getQuantity());
        dto.setPrice(cart.getSaleItem().getPrice());
        dto.setDescription(cart.getSaleItem().getBrand().getName() + " " + cart.getSaleItem().getModel()
                + " (" + cart.getSaleItem().getRamGb() + "GB, " + cart.getSaleItem().getColor() + ")");
        dto.setQuantityInStock(cart.getSaleItem().getQuantity());
        dto.setSelected(cart.getSelected());
        List<Saleitemimage> images = cart.getSaleItem().getSaleItems();
        if (!images.isEmpty()) {
            dto.setImage(images.getFirst().getFileName());
        }
        return dto;
    }

    public ResponseAddToCartDTO toResponseAddToCartDTO(Integer userId, Cart cart, String message) {
        ResponseAddToCartDTO response = new ResponseAddToCartDTO();
        response.setCartId(cart.getId());
        response.setUserId(userId);
        response.setSaleItemId(cart.getSaleItem().getId());
        response.setQuantity(cart.getQuantity());
        response.setSelected(cart.getSelected());
        response.setMessage(message);
        return response;
    }
}
