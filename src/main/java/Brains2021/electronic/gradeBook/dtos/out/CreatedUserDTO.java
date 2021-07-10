package Brains2021.electronic.gradeBook.dtos.out;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;

public class CreatedUserDTO {

	@JsonView(Views.Principal.class)
	private Long id;

	@JsonView(Views.Student.class)
	private String name;

	@JsonView(Views.Student.class)
	private String surname;

	@JsonView(Views.Student.class)
	private String email;

	@JsonView(Views.Student.class)
	private String username;

	@JsonView(Views.Admin.class)
	private String password;

	@JsonView(Views.Principal.class)
	private String jmbg;

	@JsonView(Views.Student.class)
	private LocalDate dateOfBirth;

	@JsonView(Views.Principal.class)
	private Boolean deleted;

	@JsonView(Views.Admin.class)
	private Integer version;

	@JsonView(Views.Principal.class)
	private String role;

	public CreatedUserDTO() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
