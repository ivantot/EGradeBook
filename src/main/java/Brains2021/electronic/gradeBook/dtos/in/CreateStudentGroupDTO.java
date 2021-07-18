package Brains2021.electronic.gradeBook.dtos.in;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class CreateStudentGroupDTO {

	@NotBlank(message = "Year can't be blank.")
	@NotNull(message = "Year must be provided.")
	@Pattern(regexp = "^I|II|III|IV|V|VI|VII|VIII$", message = "Provide a valid year value, using roman numerals between I and VIII.")
	private String year;

	@NotBlank(message = "Index can't be blank.")
	@NotNull(message = "Index must be provided.")
	@Pattern(regexp = "^1|2|3|4|5$", message = "Provide a valid index between 1 and 5.")
	private String yearIndex;

	public CreateStudentGroupDTO() {
		super();
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getYearIndex() {
		return yearIndex;
	}

	public void setYearIndex(String yearIndex) {
		this.yearIndex = yearIndex;
	}

}
