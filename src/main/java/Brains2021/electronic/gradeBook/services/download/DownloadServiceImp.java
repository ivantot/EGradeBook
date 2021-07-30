package Brains2021.electronic.gradeBook.services.download;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.exceptions.CustomFileNotFoundException;

@Service
public class DownloadServiceImp implements DownloadService {

	private final Path fileStorageLocation = Paths.get("logs\\");

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	@Override
	public Resource loadFileAsResource(String fileName) {

		logger.info("**SERVICE FOR MAKING A RESOURCE** Access to service successful.");
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				logger.info("**SERVICE FOR MAKING A RESOURCE** Resource found.");
				return resource;
			} else {
				logger.warn("**SERVICE FOR MAKING A RESOURCE** File not found.");
				throw new CustomFileNotFoundException("File not found " + fileName);
			}
		} catch (MalformedURLException ex) {
			throw new CustomFileNotFoundException("File not found " + fileName, ex);
		}
	}
}
