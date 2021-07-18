package Brains2021.electronic.gradeBook.dtos.in;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class UpdatePhoneNumberDTO {

	@NotBlank(message = "Cannot be blank.")
	@NotNull(message = "Cannot be null.")
	@Pattern(regexp = "^\\+381-6[0-9]-\\d{3}-\\d{3,4}$", message = "Provide a valid phone number using the following pattern +381-6X-XXX-XXXx.")
	private String phoneNumber;

	public UpdatePhoneNumberDTO() {
		super();
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
