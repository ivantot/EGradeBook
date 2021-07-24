package Brains2021.electronic.gradeBook.entites.users;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.security.Views;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Teachers")
@PrimaryKeyJoinColumn(name = "TeacherID")
public class TeacherEntity extends UserEntity {

	@JsonView(Views.Headmaster.class)
	@Column(nullable = false)
	private LocalDate startOfEmployment;

	@JsonView(Views.Headmaster.class)
	@Column(nullable = false)
	private Double salary;

	@JsonView(Views.Teacher.class)
	@Column(nullable = false)
	private Integer isHomeroomTeacher;

	@JsonView(Views.Teacher.class)
	@Column(nullable = false)
	private Integer isHeadmaster;

	@JsonView(Views.Teacher.class)
	@Column(nullable = false)
	private Integer isAdministrator;

	@JsonView(Views.Headmaster.class)
	private Double salaryHomeroomBonus;

	@JsonView(Views.Headmaster.class)
	private Double salaryHeadmasterBonus;

	@JsonView(Views.Headmaster.class)
	private Double salaryAdminBonus;

	@JsonView(Views.Headmaster.class)
	@Column(nullable = false)
	private Integer weeklyHourCapacity;

	@OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "inChargeOf")
	private StudentGroupEntity inChargeOf;

	@OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Set<TeacherSubjectEntity> subjectsTeaching = new HashSet<>();

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

	public Integer getIsHomeroomTeacher() {
		return isHomeroomTeacher;
	}

	public void setIsHomeroomTeacher(Integer isHomeroomTeacher) {
		this.isHomeroomTeacher = isHomeroomTeacher;
	}

	public Integer getIsHeadmaster() {
		return isHeadmaster;
	}

	public void setIsHeadmaster(Integer isHeadmaster) {
		this.isHeadmaster = isHeadmaster;
	}

	public Integer getIsAdministrator() {
		return isAdministrator;
	}

	public void setIsAdministrator(Integer isAdministrator) {
		this.isAdministrator = isAdministrator;
	}

	public Double getSalaryHomeroomBonus() {
		return salaryHomeroomBonus;
	}

	public void setSalaryHomeroomBonus(Double salaryHomeroomBonus) {
		this.salaryHomeroomBonus = salaryHomeroomBonus;
	}

	public Double getSalaryHeadmasterBonus() {
		return salaryHeadmasterBonus;
	}

	public void setSalaryHeadmasterBonus(Double salaryHeadmasterBonus) {
		this.salaryHeadmasterBonus = salaryHeadmasterBonus;
	}

	public Double getSalaryAdminBonus() {
		return salaryAdminBonus;
	}

	public void setSalaryAdminBonus(Double salaryAdminBonus) {
		this.salaryAdminBonus = salaryAdminBonus;
	}

	public Integer getWeeklyHourCapacity() {
		return weeklyHourCapacity;
	}

	public void setWeeklyHourCapacity(Integer weeklyHourCapacity) {
		this.weeklyHourCapacity = weeklyHourCapacity;
	}

	public StudentGroupEntity getInChargeOf() {
		return inChargeOf;
	}

	public void setInChargeOf(StudentGroupEntity inChargeOf) {
		this.inChargeOf = inChargeOf;
	}

	public Set<TeacherSubjectEntity> getSubjectsTeaching() {
		return subjectsTeaching;
	}

	public void setSubjectsTeaching(Set<TeacherSubjectEntity> subjectsTeaching) {
		this.subjectsTeaching = subjectsTeaching;
	}

}
