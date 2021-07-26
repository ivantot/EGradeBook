package Brains2021.electronic.gradeBook.services.download;

import org.springframework.core.io.Resource;

public interface DownloadService {

	public Resource loadFileAsResource(String fileName);
}
