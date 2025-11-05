package com.example.int221_ssi_03.Service;

import com.example.int221_ssi_03.DTO.*;
import com.example.int221_ssi_03.Entities.*;
import com.example.int221_ssi_03.Exception.ItemNotFoundException;
import com.example.int221_ssi_03.Exception.UserMismatchException;
import com.example.int221_ssi_03.Repositories.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private SaleItemService saleItemService;

    @Autowired
    private CartService cartService;

    @Autowired
    private EntityManager entityManager;

    public OrderResponseByOrderDTO getOrderById(Integer orderId, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ItemNotFoundException("Order not found"));

        if (!(order.getBuyer().getId().equals(userId) ||
                order.getSeller().getUser().getId().equals(userId))) {
            throw new UserMismatchException("User id not an owner (seller/buyer) of the order");
        }

        return mapOrderToDTO(order);
    }

    public PageDTO<OrderResponseByUserDTO> getOrdersByUser(Integer buyerId, Integer page, Integer size, String sortField, String sortDirection, String searchKeyword) {
        User user = userService.findUserById(buyerId);

        sortField = (sortField == null || sortField.trim().isEmpty()) ? "OrderDate" : sortField.trim();
        sortDirection = (sortDirection == null || sortDirection.trim().isEmpty()) ? "desc" : sortDirection.trim();
        size = (size == null || size <= 0) ? 10 : size;

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Order.desc(sortField), Sort.Order.desc("id"))
                : Sort.by(Sort.Order.asc(sortField), Sort.Order.desc("id"));

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orderPage = orderRepository.findAll(OrderRepository.buildFilter(buyerId, searchKeyword), pageable);

        List<OrderResponseByUserDTO> dtoList = orderPage
                .stream()
                .map(this::mapOrderToUserDTO)
                .toList();

        return toPageDTO(orderPage, dtoList);
    }

    public PageDTO<OrderResponseBySellerDTO> getOrdersBySeller(Integer sellerId, Integer page, Integer size, String sortField, String sortDirection, String type) {
        Seller seller = sellerService.findSellerById(sellerId);

        sortField = (sortField == null || sortField.trim().isEmpty()) ? "OrderDate" : sortField.trim();
        sortDirection = (sortDirection == null || sortDirection.trim().isEmpty()) ? "desc" : sortDirection.trim();
        size = (size == null || size <= 0) ? 10 : size;

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orderPage;

        if ("newOrder".equalsIgnoreCase(type)) {
            orderPage = orderRepository.findBySellerIdAndIsReadBySellerFalse(seller.getId(), pageable);
        } else if ("canceled".equalsIgnoreCase(type)) {
            orderPage = orderRepository.findBySellerIdAndOrderStatus(seller.getId(), "CANCELED", pageable);
        } else if ("completed".equalsIgnoreCase(type)){
            orderPage = orderRepository.findBySellerIdAndOrderStatus(seller.getId(), "COMPLETED", pageable);
        } else{
            orderPage = orderRepository.findBySellerId(seller.getId(), pageable);
        }

        List<OrderResponseBySellerDTO> dtoList = orderPage
                .stream()
                .map(this::mapOrderToSellerDTO)
                .toList();

        return toPageDTO(orderPage, dtoList);
    }

    @Transactional
    public void markOrderAsRead(Integer orderId, Integer sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ItemNotFoundException("Order not found"));

        if (!order.getSeller().getUser().getId().equals(sellerId)) {
            throw new UserMismatchException("User id not an owner (seller/buyer) of the order");
        }

        if (!order.isReadBySeller()) {
            order.setReadBySeller(true);
            orderRepository.save(order);
        }
    }

    @Transactional
    public List<OrderResponseByUserDTO> createOrders(List<OrderRequestDTO> orderRequestList) {
        return orderRequestList.stream()
                .map(this::createSingleOrder)
                .toList();
    }

    @Transactional
    public OrderResponseByUserDTO createSingleOrder(OrderRequestDTO dto) {
        User buyer = userService.findUserById(dto.getBuyerId());

        Seller seller = sellerService.findSellerById(dto.getSellerId());

        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setOrderDate(dto.getOrderDate());
        order.setShippingAddress(dto.getShippingAddress());
        order.setOrderNote(dto.getOrderNote());

        boolean isRejectOrder = false;

        for (OrderItem itemDto : dto.getOrderItems()) {
            SaleItem saleItem = saleItemService.getSaleItemEntityById(Math.toIntExact(itemDto.getSaleItemId()));

            if (saleItem.getQuantity() < itemDto.getQuantity()) {
                isRejectOrder = true;
                break;
            }
        }

        if(buyer.getId().equals(seller.getId())) {
            isRejectOrder = true;
        }

        order.setOrderStatus(isRejectOrder ? "CANCELED" : "COMPLETED");

        for (OrderItem itemDto : dto.getOrderItems()) {
            SaleItem saleItem = saleItemService.getSaleItemEntityById(Math.toIntExact(itemDto.getSaleItemId()));

            if (!isRejectOrder) {
                saleItem.setQuantity(saleItem.getQuantity() - itemDto.getQuantity());
                saleItemService.updateSaleItemById(Math.toIntExact(itemDto.getSaleItemId()), saleItem);
            }

            cartService.deleteCartByUserIdAndSaleItemId(buyer.getId(), saleItem.getId());

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setSaleItem(saleItem);
            detail.setPrice(itemDto.getPrice());
            detail.setQuantity(itemDto.getQuantity());
            detail.setDescription(itemDto.getDescription());
            order.getOrderDetails().add(detail);
        }
        Order savedOrder = orderRepository.saveAndFlush(order);
        entityManager.refresh(savedOrder);
        return mapOrderToUserDTO(savedOrder);
    }

    public OrderResponseByOrderDTO mapOrderToDTO(Order order) {
        OrderResponseByOrderDTO dto = new OrderResponseByOrderDTO();
        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyer().getId());

        if (order.getSeller() != null) {
            Seller seller = order.getSeller();
            SellerDetailDTO sellerDTO = new SellerDetailDTO();
            sellerDTO.setId(seller.getId());
            if (seller.getUser() != null) {
                sellerDTO.setNickName(seller.getUser().getNickname());
                sellerDTO.setEmail(seller.getUser().getEmail());
                sellerDTO.setFullName(seller.getUser().getFullName());
                sellerDTO.setUserType(seller.getUser().getRole());
            }
            dto.setSeller(sellerDTO);
        }

        dto.setOrderDate(order.getOrderDate());
        dto.setPaymentDate(order.getPaymentDate());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setOrderNote(order.getOrderNote());
        dto.setOrderStatus(order.getOrderStatus());

        dto.setOrderItems(mapOrderDetailsToItems(order.getOrderDetails()));

        return dto;
    }

    public OrderResponseByUserDTO mapOrderToUserDTO(Order order) {
        OrderResponseByUserDTO dto = new OrderResponseByUserDTO();
        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyer().getId());

        if (order.getSeller() != null && order.getSeller().getUser() != null) {
            dto.setSeller(toUserDTO(order.getSeller().getUser()));
        }

        dto.setOrderDate(order.getOrderDate());
        dto.setPaymentDate(order.getPaymentDate());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setOrderNote(order.getOrderNote());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setOrderItems(mapOrderDetailsToItems(order.getOrderDetails()));

        return dto;
    }

    public OrderResponseBySellerDTO mapOrderToSellerDTO(Order order) {
        OrderResponseBySellerDTO dto = new OrderResponseBySellerDTO();
        dto.setId(order.getId());

        if (order.getSeller() != null) {
            dto.setSellerId(order.getSeller().getId());
        }

        if (order.getBuyer() != null) {
            dto.setBuyer(toUserDTO(order.getBuyer()));
        }

        dto.setOrderDate(order.getOrderDate());
        dto.setPaymentDate(order.getPaymentDate());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setOrderNote(order.getOrderNote());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setOrderItems(mapOrderDetailsToItems(order.getOrderDetails()));

        return dto;
    }

    private UserDTO toUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getNickname());
        return dto;
    }

    private List<OrderItem> mapOrderDetailsToItems(List<OrderDetail> orderDetails) {
        return orderDetails.stream().map(detail -> {
            OrderItem item = new OrderItem();
            item.setNo(detail.getId());
            item.setSaleItemId(detail.getSaleItem().getId().longValue());
            item.setPrice(detail.getPrice());
            item.setQuantity(detail.getQuantity());
            item.setDescription(detail.getDescription());
            return item;
        }).toList();
    }

    private <T> PageDTO<T> toPageDTO(Page<?> page, List<T> content) {
        PageDTO<T> dto = new PageDTO<>();
        dto.setContent(content);
        dto.setFirst(page.isFirst());
        dto.setLast(page.isLast());
        dto.setTotalPages(page.getTotalPages());
        dto.setTotalElements((int) page.getTotalElements());
        dto.setSize(page.getSize());
        dto.setSort(page.getSort().toString());
        dto.setNumber(page.getNumber());
        return dto;
    }

    public OrderResponseDetailSellerDTO getOrderBySellerAndOrderId(Integer sellerId, Integer orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ItemNotFoundException("Order not found"));

        if (order.getSeller() == null || !order.getSeller().getId().equals(sellerId)) {
            throw new UserMismatchException("Order does not belong to this seller.");
        }

        OrderResponseDetailSellerDTO dto = new OrderResponseDetailSellerDTO();
        dto.setId(order.getId());

        if (order.getSeller() != null && order.getSeller().getUser() != null) {
            dto.setSellerId(order.getSeller().getId());
        }

        if (order.getBuyer() != null) {
            UserDTO buyerDTO = new UserDTO();
            buyerDTO.setId(order.getBuyer().getId());
            buyerDTO.setUserName(order.getBuyer().getNickname());
            dto.setBuyer(buyerDTO);
        }

        dto.setOrderDate(order.getOrderDate());
        dto.setPaymentDate(order.getPaymentDate());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setOrderNote(order.getOrderNote());
        dto.setOrderStatus(order.getOrderStatus());

        List<OrderItem> orderItems = mapOrderDetailsToItems(order.getOrderDetails());
        dto.setOrderItems(orderItems);

        return dto;
    }

    public Integer countNewOrdersForSeller(Integer sellerId) {
        return orderRepository.countBySellerIdAndIsReadBySellerFalse(sellerId);
    }

}

