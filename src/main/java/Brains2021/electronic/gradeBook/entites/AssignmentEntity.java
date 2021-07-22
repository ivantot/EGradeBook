package Brains2021.electronic.gradeBook.entites;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.utils.enums.EAssignmentType;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Asignments")
public class AssignmentEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private EAssignmentType type;

	private String description;

	@Column(nullable = false)
	private Integer semester;

	@Column(nullable = false)
	private String studyYear;

	@Column(nullable = false)
	private LocalDate dateCreated;

	private LocalDate dateAssigned;

	private LocalDate dateCompleted;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate dueDate;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "teacherIssuing")
	private TeacherSubjectEntity teacherIssuing;

	private Integer gradeRecieved;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "assignedTo")
	private StudentEntity assignedTo;

	private Integer overridenGrade;

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

	public EAssignmentType getType() {
		return type;
	}

	public void setType(EAssignmentType type) {
		this.type = type;
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

	public String getStudyYear() {
		return studyYear;
	}

	public void setStudyYear(String studyYear) {
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

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalDate getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(LocalDate dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public TeacherSubjectEntity getTeacherIssuing() {
		return teacherIssuing;
	}

	public void setTeacherIssuing(TeacherSubjectEntity teacherIssuing) {
		this.teacherIssuing = teacherIssuing;
	}

	public Integer getGradeRecieved() {
		return gradeRecieved;
	}

	public void setGradeRecieved(Integer gradeRecieved) {
		this.gradeRecieved = gradeRecieved;
	}

	public Integer getOverridenGrade() {
		return overridenGrade;
	}

	public void setOverridenGrade(Integer overridenGrade) {
		this.overridenGrade = overridenGrade;
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
