package com.example.int221_ssi_03.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 20)
    @NotNull
    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 100)
    @NotNull
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Size(max = 40)
    @NotNull
    @Column(name = "fullName", nullable = false, length = 40)
    private String fullName;

    @Size(max = 255)
    @Column(name = "shippingAddress")
    private String shippingAddress;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "isActive", nullable = false)
    private Boolean isActive = false;

    @NotNull
    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "createdOn", updatable = false, insertable = false)
    private Instant createdOn;

    @Column(name = "updatedOn", updatable = false, insertable = false)
    private Instant updatedOn;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Seller seller;

    @OneToMany(mappedBy = "user")
    private List<Cart> cart = new LinkedList<>();

    @OneToMany(mappedBy = "buyer")
    private List<Order> order = new LinkedList<>();

}