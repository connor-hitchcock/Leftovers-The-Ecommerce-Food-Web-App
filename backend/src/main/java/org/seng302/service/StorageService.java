package org.seng302.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

	void init();

	void store(MultipartFile file, String filename);

	Stream<Path> loadAll();

	Resource load(String filename);

	void deleteAll();

	void deleteOne(String filename);

}