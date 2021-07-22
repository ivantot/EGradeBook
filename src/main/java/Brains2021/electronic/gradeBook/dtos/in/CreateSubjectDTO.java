package Brains2021.electronic.gradeBook.dtos.in;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CreateSubjectDTO {

	@NotBlank(message = "First name can't be blank.")
	@NotNull(message = "First name must be provided.")
	private String name;

	@Size(max = 60, message = "Description must be up to {max} characters long.")
	private String description;

	@NotBlank(message = "Year of schooling can't be blank.")
	@NotNull(message = "Year of schooling must be provided.")
	@Pattern(regexp = "^I|II|III|IV|V|VI|VII|VIII$", message = "Provide a valid year value, using roman numerals between I and VIII.")
	private String yerofSchooling;

	@NotBlank(message = "Weekly teaching hours required for subject can't be blank.")
	@NotNull(message = "Weekly teaching hours required for subject must be provided.")
	private Integer weeklyHoursRequired;

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

	public String getYerofSchooling() {
		return yerofSchooling;
	}

	public void setYerofSchooling(String yerofSchooling) {
		this.yerofSchooling = yerofSchooling;
	}

	public Integer getWeeklyHoursRequired() {
		return weeklyHoursRequired;
	}

	public void setWeeklyHoursRequired(Integer weeklyHoursRequired) {
		this.weeklyHoursRequired = weeklyHoursRequired;
	}

}
