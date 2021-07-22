package Brains2021.electronic.gradeBook.dtos.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;

//@JsonRootName(value = "New TeacherStudent")
@JsonPropertyOrder({ "name", "surname", "subject", "yearOfSchooling", "weeklyHoursAlloted" })
public class CreatedTeacherSubjectDTO {

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Weekly hours alloted to teacher")
	private Integer weeklyHoursAlloted;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Name")
	private String name;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Surname")
	private String surname;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Subject")
	private String subject;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Grade - year of schooling")
	private String yearOfSchooling;

	public CreatedTeacherSubjectDTO() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public Integer getWeeklyHoursAlloted() {
		return weeklyHoursAlloted;
	}

	public void setWeeklyHoursAlloted(Integer weeklyHoursAlloted) {
		this.weeklyHoursAlloted = weeklyHoursAlloted;
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
