package Brains2021.electronic.gradeBook.entites.users;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import Brains2021.electronic.gradeBook.entites.GradeEntity;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Asignments")
public class AssignmentEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private String name;

	private String description;

	@Column(nullable = false)
	private Integer semester;

	@Column(nullable = false)
	private Integer studyYear;

	@Column(nullable = false)
	private LocalDate dateCreated;

	private LocalDate dateAssigned;

	private LocalDate dateCompleted;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "teacherIssuing")
	private TeacherEntity teacherIssuing;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "gradeRecieved")
	private GradeEntity gradeRecieved;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "assignedTo")
	private StudentEntity assignedTo;

	private Boolean deleted;

	@Version
	private Integer version;

	public AssignmentEntity() {
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getSemester() {
		return semester;
	}

	public void setSemester(Integer semester) {
		this.semester = semester;
	}

	public Integer getStudyYear() {
		return studyYear;
	}

	public void setStudyYear(Integer studyYear) {
		this.studyYear = studyYear;
	}

	public LocalDate getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(LocalDate dateCreated) {
		this.dateCreated = dateCreated;
	}

	public LocalDate getDateAssigned() {
		return dateAssigned;
	}

	public void setDateAssigned(LocalDate dateAssigned) {
		this.dateAssigned = dateAssigned;
	}

	public LocalDate getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(LocalDate dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public TeacherEntity getTeacherIssuing() {
		return teacherIssuing;
	}

	public void setTeacherIssuing(TeacherEntity teacherIssuing) {
		this.teacherIssuing = teacherIssuing;
	}

	public GradeEntity getGradeRecieved() {
		return gradeRecieved;
	}

	public void setGradeRecieved(GradeEntity gradeRecieved) {
		this.gradeRecieved = gradeRecieved;
	}

	public StudentEntity getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(StudentEntity assignedTo) {
		this.assignedTo = assignedTo;
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

}
