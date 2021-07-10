package Brains2021.electronic.gradeBook.exceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ResponseBody
@ControllerAdvice
public class GlobalExHandler {

	//@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class) //honeypot
	public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, List<String>> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			List<String> errorMessage = new ArrayList<>();
			for (ObjectError anyError : ex.getBindingResult().getAllErrors()) {
				if (fieldName.equals(((FieldError) anyError).getField())) {
					errorMessage.add(anyError.getDefaultMessage());
				}
			}
			errors.put(fieldName, errorMessage);
		});
		return new ResponseEntity<Map<String, List<String>>>(errors, HttpStatus.BAD_REQUEST);
	}
	
}


