package Brains2021.electronic.gradeBook.dtos.out;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;

//@JsonRootName(value = "New Teacher")
@JsonPropertyOrder({ "name", "surname", "username", "email", "jmbg", "dateOfBirth", "startOfEmployment", "salary",
		"weeklyHourCapacity" })
public class CreatedTeacherDTO {

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Name")
	private String name;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Surname")
	private String surname;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "E-mail")
	private String email;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Username")
	private String username;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Unique ID number")
	private String jmbg;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Date of birth")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate dateOfBirth;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Start of Employment")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate startOfEmployment;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Salary")
	private Double salary;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Weekly hour capacity")
	private Integer weeklyHourCapacity;

	public CreatedTeacherDTO() {
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getJmbg() {
		return jmbg;
	}

	public void setJmbg(String jmbg) {
		this.jmbg = jmbg;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public LocalDate getStartOfEmployment() {
		return startOfEmployment;
	}

	public void setStartOfEmployment(LocalDate startOfEmployment) {
		this.startOfEmployment = startOfEmployment;
	}

	public Double getSalary() {
		return salary;
	}

	public void setSalary(Double salary) {
		this.salary = salary;
	}

	public Integer getWeeklyHourCapacity() {
		return weeklyHourCapacity;
	}

	public void setWeeklyHourCapacity(Integer weeklyHourCapacity) {
		this.weeklyHourCapacity = weeklyHourCapacity;
	}

}
