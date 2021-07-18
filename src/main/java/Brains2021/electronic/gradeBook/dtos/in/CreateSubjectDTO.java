package Brains2021.electronic.gradeBook.dtos.in;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateSubjectDTO {

	@NotBlank(message = "First name can't be blank.")
	@NotNull(message = "First name must be provided.")
	private String name;

	@Size(max = 60, message = "Description must be up to {max} characters long.")
	private String description;

	public CreateSubjectDTO() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
