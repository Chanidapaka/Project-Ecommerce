package com.example.int221_ssi_03.Entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Data
@Entity
@Table(name = "saleItem")
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private Seller seller;

    @Column(name = "model", nullable = false, length = 60)
    private String model;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "ramGb")
    private Integer ramGb;

    @Column(name = "screenSizeInch", precision = 4, scale = 2)
    private BigDecimal screenSizeInch;

    @Column(name = "storageGb")
    private Integer storageGb;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "createdOn", updatable = false, insertable = false)
    private Instant createdOn;

    @Column(name = "updatedOn", updatable = false, insertable = false)
    private Instant updatedOn;

    @OneToMany(mappedBy = "saleItem")
    @OrderBy("imageViewOrder ASC")
    private List<Saleitemimage> saleItems = new LinkedList<>();

    public void setQuantity(Integer quantity){
        if (quantity == null || quantity < 0) {
            this.quantity = 1;
        }else{
            this.quantity = quantity;
        }
    }

    public void setModel(String model){
        if (model != null) {
            model = model.trim();
            if (model.isEmpty()) model = null;
        }
        this.model = model;
    }

    public void setDescription(String description) {
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) description = null;
        }
        this.description = description;
    }

    public void setColor(String color) {
        if (color != null) {
            color = color.trim();
            if (color.isEmpty()) color = null;
        }
        this.color = color;
    }

}
