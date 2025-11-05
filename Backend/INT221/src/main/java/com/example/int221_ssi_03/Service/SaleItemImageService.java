package com.example.int221_ssi_03.Service;

import com.example.int221_ssi_03.DTO.SaleItemImageRequestDTO;
import com.example.int221_ssi_03.Entities.SaleItem;
import com.example.int221_ssi_03.Entities.Saleitemimage;
import com.example.int221_ssi_03.Exception.DataConstraintViolationException;
import com.example.int221_ssi_03.Repositories.SaleItemImageRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaleItemImageService {
    @Autowired
    private SaleItemImageRepository saleItemImageRepo;

    private final FileService fileService;
    private final Path saleItemImageFolder;

    @Autowired
    private SaleItemService saleItemService;

    @Autowired
    private EntityManager entityManager;


    @Autowired
    public SaleItemImageService(FileService fileService) {
        this.fileService = fileService;
        this.saleItemImageFolder = fileService.initEntityFolder("saleItem-images");
    }

    public List<Saleitemimage> getAllSaleItemImageBySaleItemId(Integer saleItemId) {
        return saleItemImageRepo.findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);
    }

    @Transactional
    public List<Saleitemimage> createAllSaleItemImage(List<MultipartFile> fileList, SaleItem saleItem) {
        if (fileList == null || fileList.size() == 0) {
            return null;
        }

        List<MultipartFile> limitedFiles = fileList.stream()
                .filter(f -> f != null && !f.isEmpty())
                .limit(4)
                .toList();

        List<Saleitemimage> listFileNames = new ArrayList<>(limitedFiles.size());

        for (int i = 0; i < limitedFiles.size(); i++) {
            MultipartFile file = limitedFiles.get(i);
            int order = i + 1;
            String fileName = fileService.generateFileName(saleItem.getId(), file.getOriginalFilename(), saleItemImageFolder);

            Saleitemimage saleItemImage = new Saleitemimage();
            saleItemImage.setSaleItem(saleItem);
            saleItemImage.setFileName(fileName);
            saleItemImage.setImageViewOrder(order);
            try {
                Saleitemimage added = saleItemImageRepo.saveAndFlush(saleItemImage);
                entityManager.refresh(added);
                fileService.store(file, fileName, saleItemImageFolder);
                listFileNames.add(added);
            } catch (Exception e) {
                throw new DataConstraintViolationException("Sale Item image save Failed");
            }
        }

        return listFileNames;
    }

    @Transactional
    public List<Saleitemimage> updateImageBySaleItemId(List<SaleItemImageRequestDTO> updateList, Integer saleItemId) {
        try {
            for (SaleItemImageRequestDTO itemImageRequest : updateList) {
                switch (itemImageRequest.getStatus().toUpperCase()) {
                    case "ONLINE":
                        break;

                    case "MOVE":
                        Saleitemimage imgToMove = saleItemImageRepo.findBySaleItemIdAndFileName(saleItemId, itemImageRequest.getFileName());
                        imgToMove.setImageViewOrder(itemImageRequest.getOrder());
                        Saleitemimage updated = saleItemImageRepo.saveAndFlush(imgToMove);
                        entityManager.refresh(updated);
                        break;

                    case "NEW":
                        String newFileName = fileService.generateFileName(saleItemId, itemImageRequest.getFileName(), saleItemImageFolder);
                        fileService.store(itemImageRequest.getImageFile(), newFileName, saleItemImageFolder);

                        Saleitemimage newImg = new Saleitemimage();
                        newImg.setSaleItem(saleItemService.getSaleItemEntityById(saleItemId));
                        newImg.setFileName(newFileName);
                        newImg.setImageViewOrder(itemImageRequest.getOrder());
                        Saleitemimage created = saleItemImageRepo.saveAndFlush(newImg);
                        entityManager.refresh(created);
                        break;

                    case "DELETE":
                        fileService.removeFile(itemImageRequest.getFileName(), saleItemImageFolder);

                        Saleitemimage imgToDelete = saleItemImageRepo.findBySaleItemIdAndFileName(saleItemId, itemImageRequest.getFileName());
                        saleItemImageRepo.delete(imgToDelete);
                        break;

                    default:
                        break;
                }
            }

            return getAllSaleItemImageBySaleItemId(saleItemId);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to update SaleItem images", ex);
        }
    }

    public void deleteAllSaleItemImageBySaleItemId(Integer saleItemId) {
        saleItemImageRepo.deleteAllBySaleItemId(saleItemId);
        fileService.removeAllFilesByEntityId(saleItemId, saleItemImageFolder);
    }
}
