package org.seng302.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class StorageServiceImpl implements StorageService {
    private static final Logger logger = LogManager.getLogger(StorageServiceImpl.class.getName());
    private final Path root = Paths.get("uploads");

    @Override
    public void init() {
        logger.warn("Initialising StorageServiceImpl");
        try {
            Files.createDirectory(root);
        } catch (FileAlreadyExistsException ignored) {
            // It is alright if the /uploads folder already exists.
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public void store(MultipartFile file, String filename) {
        logger.info(() -> String.format("Storing image with filename=%s", filename));
        try {
            Files.copy(file.getInputStream(), this.root.resolve(filename));
            
        } catch (Exception e) {
            logger.error(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    // Took out deleteAll as product images should not be deleted all at once
    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    /**
     * Deletes a single file from the disk
     * @param filename Filename to delete
     */
    @Override
    public void deleteOne(String filename) {
        if (filename.isEmpty() || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename not given for deletion");
        }
        Path file = root.resolve(filename);

        if (!file.toFile().delete()) {
            logger.warn(() -> "Failed to delete: \"" + filename + "\"");
        }
    }



}
