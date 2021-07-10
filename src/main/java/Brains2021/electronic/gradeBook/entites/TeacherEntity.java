package Brains2021.electronic.gradeBook.entites;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Teachers")
@PrimaryKeyJoinColumn(name = "TeacherID")
public class TeacherEntity extends AbstractUserEntity {

	@Column(nullable = false)
	private LocalDate startOfEmployment;

	@Column(nullable = false)
	private Double salary;

	public TeacherEntity() {
		super();
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
