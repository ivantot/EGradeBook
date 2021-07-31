package Brains2021.electronic.gradeBook.dtos.in;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CreateAssignmentDTO {

	@NotEmpty(message = "Assignment name must be provided.")
	@Pattern(regexp = "^ORAL|HOMEWORK|ESSAY|PRACTICAL|TEST$", message = "Provide a valid assignment type (ORAL, HOMEWORK, ESSAY, PRACTICAL, TEST).")
	private String type;

	@NotEmpty(message = "Description must be provided.")
	@Size(max = 60, message = "Description must be less than {max} characters long.")
	private String description;
	
	@NotBlank(message = "Year can't be blank.")
	@NotNull(message = "Year must be provided.")
	@Pattern(regexp = "^I|II|III|IV|V|VI|VII|VIII$", message = "Provide a valid year value, using roman numerals between I and VIII.")
	private String year;

	@Min(value = 1, message = "Select a valid semester (1 or 2).")
	@Max(value = 2, message = "Select a valid semester (1 or 2).")
	private Integer semester;

	@NotEmpty(message = "Subject must be provided.")
	private String subject;

	public CreateAssignmentDTO() {
		super();
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getSemester() {
		return semester;
	}

	public void setSemester(Integer semester) {
		this.semester = semester;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
