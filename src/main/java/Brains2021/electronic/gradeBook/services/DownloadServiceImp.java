package Brains2021.electronic.gradeBook.services;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.exceptions.CustomFileNotFoundException;

@Service
public class DownloadServiceImp implements DownloadService {

	private final Path fileStorageLocation = Paths.get("logs\\");

	@Override
	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new CustomFileNotFoundException("File not found " + fileName);
			}
		} catch (MalformedURLException ex) {
			throw new CustomFileNotFoundException("File not found " + fileName, ex);
		}
	}
}
