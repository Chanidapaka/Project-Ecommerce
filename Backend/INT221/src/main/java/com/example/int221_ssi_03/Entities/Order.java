package com.example.int221_ssi_03.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyerId", nullable = false)
    private User buyer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sellerId", nullable = false)
    private Seller seller;

    @NotNull
    @Column(name = "orderDate", nullable = false)
    private Instant orderDate;

    @Column(name = "paymentDate", updatable = false, insertable = false)
    private Instant paymentDate;

    @Size(max = 255)
    @NotNull
    @Column(name = "shippingAddress", nullable = false)
    private String shippingAddress;

    @Size(max = 255)
    @NotNull
    @Column(name = "orderNote", nullable = false)
    private String orderNote;

    @Column(name = "isReadBySeller")
    private boolean isReadBySeller;

    @Size(max = 50)
    @NotNull
    @Column(name = "orderStatus", nullable = false, length = 50)
    private String orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails = new LinkedList<>();

}