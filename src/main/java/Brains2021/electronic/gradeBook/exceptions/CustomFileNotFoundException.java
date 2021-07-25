package Brains2021.electronic.gradeBook.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//spring will respond with this status is file is not found
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomFileNotFoundException extends RuntimeException {

	public CustomFileNotFoundException(String message) {
		super(message);
	}

	public CustomFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
