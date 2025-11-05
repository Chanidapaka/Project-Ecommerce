package com.example.int221_ssi_03.Service;

import com.example.int221_ssi_03.utils.FileStorageProperties;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
@Getter
public class FileService {
    private final Path rootLocation;

    @Autowired
    public FileService(FileStorageProperties fileStorageProperties) {
        this.rootLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            if (!Files.exists(this.rootLocation)) {
                Files.createDirectories(this.rootLocation);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Cannot create root upload directory.", ex);
        }
    }

    public Path initEntityFolder(String entityType) {
        Path entityFolder = rootLocation.resolve(entityType).normalize();
        try {
            if (!Files.exists(entityFolder)) {
                Files.createDirectories(entityFolder);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot create folder for entity: " + entityType, e);
        }
        return entityFolder;
    }

    public String store(MultipartFile file, String fileName, Path entityFolder) {
        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid filename: " + fileName);
            }
            Path targetLocation = entityFolder.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName, ex);
        }
    }

    public void removeFile(String fileName, Path entityFolder) {
        try {
            Path filePath = entityFolder.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            } else {
                throw new ResourceNotFoundException("File not found " + fileName);
            }
        } catch (IOException ex) {
            throw new RuntimeException("File delete error: " + fileName, ex);
        }
    }

    public void removeAllFilesByEntityId(Integer entityId, Path entityFolder) {
        String prefix = entityId + ".";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(entityFolder, prefix + "*")) {
            for (Path filePath : stream) {
                Files.delete(filePath);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error deleting files for id=" + entityId, ex);
        }
    }

    public String generateFileName(Integer entityId, String originalName, Path entityFolder) {
        if (originalName == null) originalName = "";

        int dotIndex = originalName.lastIndexOf(".");
        String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : originalName.substring(dotIndex);

        String newName = entityId + "." + baseName + extension;
        Path targetLocation = entityFolder.resolve(newName);

        int counter = 1;
        while (Files.exists(targetLocation)) {
            newName = entityId + "." + baseName + "." + counter + extension;
            targetLocation = entityFolder.resolve(newName);
            counter++;
        }

        return newName;
    }
    
}
