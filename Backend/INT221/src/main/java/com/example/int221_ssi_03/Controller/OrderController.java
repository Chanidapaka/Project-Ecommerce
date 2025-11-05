package com.example.int221_ssi_03.Controller;

import com.example.int221_ssi_03.DTO.NewOrderCountDTO;
import com.example.int221_ssi_03.DTO.OrderRequestDTO;
import com.example.int221_ssi_03.DTO.OrderResponseByOrderDTO;
import com.example.int221_ssi_03.DTO.OrderResponseByUserDTO;
import com.example.int221_ssi_03.Entities.AuthUserDetail;
import com.example.int221_ssi_03.Exception.AccountNotActiveException;
import com.example.int221_ssi_03.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseByOrderDTO> getOrderById(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthUserDetail authUser) {
        return ResponseEntity.ok(orderService.getOrderById(id, authUser.getId()));
    }

    @PostMapping
    public ResponseEntity<List<OrderResponseByUserDTO>> createOrder(
            @RequestBody List<OrderRequestDTO> request,
            @AuthenticationPrincipal AuthUserDetail authUser
    ) {
        if (!authUser.isActive()) {
            throw new AccountNotActiveException("Account is not active");
        }

        request.forEach(req -> req.setBuyerId(authUser.getId()));

        List<OrderResponseByUserDTO> responseDTO = orderService.createOrders(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PutMapping("/{orderId}/read")
    public ResponseEntity<Void> markOrderAsRead(@PathVariable Integer orderId, @AuthenticationPrincipal AuthUserDetail authUser){
        orderService.markOrderAsRead(orderId, authUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/new/count")
    public ResponseEntity<NewOrderCountDTO> getNewOrderCount(@AuthenticationPrincipal AuthUserDetail authUser) {
        Integer count = orderService.countNewOrdersForSeller(authUser.getId());
        return ResponseEntity.ok(new NewOrderCountDTO(count));
    }

}
