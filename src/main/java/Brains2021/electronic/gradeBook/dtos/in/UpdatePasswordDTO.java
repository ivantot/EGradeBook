package Brains2021.electronic.gradeBook.dtos.in;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class UpdatePasswordDTO {

	@NotBlank(message = "Cannot be blank.")
	@NotNull(message = "Cannot be null.")
	private String oldPassword;

	@NotBlank(message = "Cannot be blank.")
	@NotNull(message = "Cannot be null.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{5,}$", message = "Password invalid, it must contain at least 5 characters, both numbers and letters must be used.")
	private String newPassword;

	@NotBlank(message = "Cannot be blank.")
	@NotNull(message = "Cannot be null.")
	private String repeatedPassword;

	public UpdatePasswordDTO() {
		super();
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getRepeatedPassword() {
		return repeatedPassword;
	}

	public void setRepeatedPassword(String repeatedPassword) {
		this.repeatedPassword = repeatedPassword;
	}

}
