package com.example.int221_ssi_03.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "seller")
public class Seller {
    @Id
    @Column(name = "userId", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Size(max = 20)
    @Column(name = "mobileNumber", length = 20)
    private String mobileNumber;

    @Size(max = 20)
    @Column(name = "bankAccountNumber", length = 20)
    private String bankAccountNumber;

    @Size(max = 50)
    @Column(name = "bankName", length = 50)
    private String bankName;

    @Size(max = 20)
    @Column(name = "nationalId", length = 20)
    private String nationalId;

    @Size(max = 255)
    @Column(name = "nationalFrontPhoto")
    private String nationalFrontPhoto;

    @Size(max = 255)
    @Column(name = "nationalBackPhoto")
    private String nationalBackPhoto;

    @OneToMany(mappedBy = "seller")
    private List<SaleItem> saleItems = new LinkedList<>();

    @OneToMany(mappedBy = "seller")
    private List<Order> order = new LinkedList<>();

}