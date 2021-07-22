package Brains2021.electronic.gradeBook.dtos.in;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class CreateTeacherSubjectDTO {

	@NotBlank(message = "Weekly alloted hours can't be blank.")
	@NotNull(message = "Weekly alloted hours must be provided.")
	private Integer weeklyHoursAlloted;

	@NotBlank(message = "Teacher can't be blank.")
	@NotNull(message = "Teacher must be provided.")
	private String username;

	@NotBlank(message = "Subject can't be blank.")
	@NotNull(message = "Subject must be provided.")
	private String subject;

	@NotBlank(message = "Year of schooling can't be blank.")
	@NotNull(message = "Year of schooling  must be provided.")
	@Pattern(regexp = "^I|II|III|IV|V|VI|VII|VIII$", message = "Provide a valid year value, using roman numerals between I and VIII.")
	private String yearOfSchooling;

	public CreateTeacherSubjectDTO() {
		super();
	}

	public Integer getWeeklyHoursAlloted() {
		return weeklyHoursAlloted;
	}

	public void setWeeklyHoursAlloted(Integer weeklyHoursAlloted) {
		this.weeklyHoursAlloted = weeklyHoursAlloted;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getYearOfSchooling() {
		return yearOfSchooling;
	}

	public void setYearOfSchooling(String yearOfSchooling) {
		this.yearOfSchooling = yearOfSchooling;
	}

}
