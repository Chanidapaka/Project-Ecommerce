package com.example.int221_ssi_03.Service;

import com.example.int221_ssi_03.DTO.SellerDetailDTO;
import com.example.int221_ssi_03.Entities.Seller;
import com.example.int221_ssi_03.Exception.ItemNotFoundException;
import com.example.int221_ssi_03.Repositories.OrderRepository;
import com.example.int221_ssi_03.Repositories.SellerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SellerService {
    @Autowired
    private SellerRepository sellerRepository;


    public Seller findSellerById(int id) {
        return sellerRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Seller not found"));
    }

    public SellerDetailDTO toSellerDetailDTO(Seller seller) {
        SellerDetailDTO dto = new SellerDetailDTO();
        dto.setId(seller.getUser().getId());
        dto.setEmail(seller.getUser().getEmail());
        dto.setFullName(seller.getUser().getFullName());
        dto.setUserType(seller.getUser().getRole());
        dto.setNickName(seller.getUser().getNickname());
        return dto;
    }
}
