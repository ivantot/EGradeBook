package Brains2021.electronic.gradeBook.utils;

import Brains2021.electronic.gradeBook.security.Views;

import com.fasterxml.jackson.annotation.JsonView;

public class RESTError {

	@JsonView(Views.Student.class)
	private Integer code;

	@JsonView(Views.Student.class)
	private String message;

	public RESTError(Integer code, String message) {

		this.code = code;
		this.message = message;

	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

}
