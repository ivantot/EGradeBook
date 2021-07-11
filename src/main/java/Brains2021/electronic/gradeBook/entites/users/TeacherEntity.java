package Brains2021.electronic.gradeBook.entites.users;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.SubjectEntity;
import Brains2021.electronic.gradeBook.security.Views;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Teachers")
@PrimaryKeyJoinColumn(name = "TeacherID")
public class TeacherEntity extends UserEntity {

	@JsonView(Views.Principal.class)
	@Column(nullable = false)
	private LocalDate startOfEmployment;

	@JsonView(Views.Principal.class)
	@Column(nullable = false)
	private Double salary;

	@JsonView(Views.Teacher.class)
	@Column(nullable = false)
	private Boolean isHomeroomTeacher;

	@JsonView(Views.Teacher.class)
	@Column(nullable = false)
	private Boolean isPrincipal;

	@JsonView(Views.Teacher.class)
	@Column(nullable = false)
	private Boolean isAdministrator;

	@JsonView(Views.Principal.class)
	private Double salaryHomeroomBonus;

	@JsonView(Views.Principal.class)
	private Double salaryPrincipalBonus;

	@JsonView(Views.Principal.class)
	private Double salaryAdminBonus;

	@OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "inChargeOf")
	private StudentGroupEntity inChargeOf;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinTable(name = "Subjects_and_Teachers", joinColumns = {
			@JoinColumn(name = "TeacherID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "SubjectID", nullable = false, updatable = false) })
	private Set<SubjectEntity> subjectsTeaching = new HashSet<>();

	@OneToMany(mappedBy = "teacherIssuing", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Set<AssignmentEntity> assignmentsGiven = new HashSet<>();

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

	public Boolean getIsHomeroomTeacher() {
		return isHomeroomTeacher;
	}

	public void setIsHomeroomTeacher(Boolean isHomeroomTeacher) {
		this.isHomeroomTeacher = isHomeroomTeacher;
	}

	public StudentGroupEntity getInChargeOf() {
		return inChargeOf;
	}

	public void setInChargeOf(StudentGroupEntity inChargeOf) {
		this.inChargeOf = inChargeOf;
	}

	public Boolean getIsPrincipal() {
		return isPrincipal;
	}

	public void setIsPrincipal(Boolean isPrincipal) {
		this.isPrincipal = isPrincipal;
	}

	public Boolean getIsAdministrator() {
		return isAdministrator;
	}

	public void setIsAdministrator(Boolean isAdministrator) {
		this.isAdministrator = isAdministrator;
	}

	public Double getSalaryHomeroomBonus() {
		return salaryHomeroomBonus;
	}

	public void setSalaryHomeroomBonus(Double salaryHomeroomBonus) {
		this.salaryHomeroomBonus = salaryHomeroomBonus;
	}

	public Double getSalaryPrincipalBonus() {
		return salaryPrincipalBonus;
	}

	public void setSalaryPrincipalBonus(Double salaryPrincipalBonus) {
		this.salaryPrincipalBonus = salaryPrincipalBonus;
	}

	public Double getSalaryAdminBonus() {
		return salaryAdminBonus;
	}

	public void setSalaryAdminBonus(Double salaryAdminBonus) {
		this.salaryAdminBonus = salaryAdminBonus;
	}

	public Set<SubjectEntity> getSubjectsTeaching() {
		return subjectsTeaching;
	}

	public void setSubjectsTeaching(Set<SubjectEntity> subjectsTeaching) {
		this.subjectsTeaching = subjectsTeaching;
	}

}
