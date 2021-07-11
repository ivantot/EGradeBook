package Brains2021.electronic.gradeBook.dtos.in;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CreateUserDTO {

	@NotNull(message = "First name must be provided.")
	@NotBlank(message = "First name can't be blank.")
	private String name;

	@NotNull(message = "Surname must be provided.")
	private String surname;

	@NotNull(message = "Email must be provided.")
	@Email(message = "Please provide a valid email.")
	private String email;

	@NotNull(message = "Username must be provided.")
	@NotBlank(message = "Username mustn't be blank.")
	@Size(min = 5, max = 20, message = "Username must be between {min} and {max} characters long.")
	private String username;

	@NotNull(message = "Cannot be null.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{5,}$", message = "Password invalid, it must contain at least 5 characters, both numbers and letters must be used.")
	private String password;

	@NotNull(message = "Cannot be null.")
	private String repeatedPassword;

	@NotNull(message = "Cannot be null.")
	@Pattern(regexp = "^(0[1-9]|1[0-9]|2[0-9]|3[0-1])(0[1-9]|1[0-2])[0-9]\\d{8}$", message = "Provide a valid JMBG.")
	private String jmbg;

	@NotNull(message = "Cannot be null.")
	@Past(message = "Date of birth must be a date in the past.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate dateOfBirth;

	@Min(value = 25000, message = "Administrator bonus must be greater than 25000.")
	private Double adminBonus;

	@Min(value = 20000, message = "Homeroom bonus must be greater than 20000.")
	private Double homeroomBonus;

	@Min(value = 30000, message = "Principal bonus must be greater than 30000.")
	private Double principalBonus;

	@Pattern(regexp = "^\\+381-6[0-5]-\\d{6,7}$", message = "Provide a valid phone number using the following pattern +381-6X-XXXXXXX.")
	private String phoneNumber;

	@Past(message = "Start of employment must be a date in the past.")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate startOfEmployment;

	@Min(value = 25000, message = "Salary must be greater than 60000.")
	private Double salary;

	@NotNull(message = "Cannot be null.")
	@Pattern(regexp = "^ROLE_ADMIN|ROLE_SUPERADMIN|ROLE_HOMEROOM|ROLE_TEACHER|ROLE_PARENT|ROLE_STUDENT|ROLE_PRINCIPAL$", message = "Provide a valid role.")
	private String role;

	public CreateUserDTO() {
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRepeatedPassword() {
		return repeatedPassword;
	}

	public void setRepeatedPassword(String repeatedPassword) {
		this.repeatedPassword = repeatedPassword;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Double getAdminBonus() {
		return adminBonus;
	}

	public void setAdminBonus(Double adminBonus) {
		this.adminBonus = adminBonus;
	}

	public Double getPrincipalBonus() {
		return principalBonus;
	}

	public void setPrincipalBonus(Double principalBonus) {
		this.principalBonus = principalBonus;
	}

	public Double getHomeroomBonus() {
		return homeroomBonus;
	}

	public void setHomeroomBonus(Double homeroomBonus) {
		this.homeroomBonus = homeroomBonus;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

}
