package com.example.int221_ssi_03.Entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Data
@Entity
@Table(name = "brand")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "websiteUrl", length = 40)
    private String websiteUrl;

    @Column(name = "isActive")
    private Boolean isActive;

    @Column(name = "countryOfOrigin", length = 80)
    private String countryOfOrigin;

    @Column(name = "createdOn", updatable = false, insertable = false)
    private LocalDateTime createdOn;

    @Column(name = "updatedOn", updatable = false, insertable = false)
    private LocalDateTime updatedOn;

    @OneToMany(mappedBy = "brand")
    private List<SaleItem> saleItems = new LinkedList<>();

    public void setName(String name) {
        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) name = null;
        }
        this.name = name;
    }

    public void setWebsiteUrl(String websiteUrl) {
        if (websiteUrl != null) {
            websiteUrl = websiteUrl.trim();
            if (websiteUrl.isEmpty()) websiteUrl = null;
        }
        this.websiteUrl = websiteUrl;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        if (countryOfOrigin != null) {
            countryOfOrigin = countryOfOrigin.trim();
            if (countryOfOrigin.isEmpty()) countryOfOrigin = null;
        }
        this.countryOfOrigin = countryOfOrigin;
    }

    public void setIsActive(Boolean isActive) {
        if (isActive == null) {
            isActive = true;
        }
        this.isActive = isActive;
    }
}