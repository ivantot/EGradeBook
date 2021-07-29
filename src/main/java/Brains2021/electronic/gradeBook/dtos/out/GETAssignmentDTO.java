package Brains2021.electronic.gradeBook.dtos.out;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;

//@JsonRootName(value = "Assignment")
@JsonPropertyOrder({ "id", "subject", "type", "description", "teacher", "assignedTo", "studentGroup", "semester",
		"dateCreated", "dateAssigned", "dueDate", "dateCompleted", "gradeRecieved", "overridenGrade", "deleted" })
public class GETAssignmentDTO {

	@JsonView(Views.Admin.class)
	private Long id;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Assignment type")
	private String type;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Description")
	private String description;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Semester")
	private Integer semester;

	@JsonView(Views.Teacher.class)
	@JsonProperty(value = "Date of creation")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate dateCreated;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Due date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate dueDate;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Date assigned")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate dateAssigned;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Date of completion")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate dateCompleted;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Assigned to")
	private String assignedTo;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Student group")
	private String studentGroup;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Teacher issuing")
	private String teacher;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Subject")
	private String subject;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Grade recieved")
	private Integer gradeRecieved;

	@JsonView(Views.Headmaster.class)
	@JsonProperty(value = "Overriden grade")
	private Integer overridenGrade;

	@JsonView(Views.Admin.class)
	private Integer deleted;

	public GETAssignmentDTO() {
		super();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
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

	public LocalDate getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(LocalDate dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getTeacher() {
		return teacher;
	}

	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public LocalDate getDateAssigned() {
		return dateAssigned;
	}

	public LocalDate getDateCompleted() {
		return dateCompleted;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public String getStudentGroup() {
		return studentGroup;
	}

	public Integer getGradeRecieved() {
		return gradeRecieved;
	}

	public Integer getOverridenGrade() {
		return overridenGrade;
	}

	public Integer getDeleted() {
		return deleted;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public void setDateAssigned(LocalDate dateAssigned) {
		this.dateAssigned = dateAssigned;
	}

	public void setDateCompleted(LocalDate dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	public void setStudentGroup(String studentGroup) {
		this.studentGroup = studentGroup;
	}

	public void setGradeRecieved(Integer gradeRecieved) {
		this.gradeRecieved = gradeRecieved;
	}

	public void setOverridenGrade(Integer overridenGrade) {
		this.overridenGrade = overridenGrade;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

	@Override
	public String toString() {
		return "GETAssignmentDTO [id=" + id + ", type=" + type + ", description=" + description + ", semester="
				+ semester + ", dateCreated=" + dateCreated + ", dueDate=" + dueDate + ", dateAssigned=" + dateAssigned
				+ ", dateCompleted=" + dateCompleted + ", assignedTo=" + assignedTo + ", studentGroup=" + studentGroup
				+ ", teacher=" + teacher + ", subject=" + subject + ", gradeRecieved=" + gradeRecieved
				+ ", overridenGrade=" + overridenGrade + ", deleted=" + deleted + ", getType()=" + getType()
				+ ", getDescription()=" + getDescription() + ", getSemester()=" + getSemester() + ", getDateCreated()="
				+ getDateCreated() + ", getTeacher()=" + getTeacher() + ", getSubject()=" + getSubject() + ", getId()="
				+ getId() + ", getDueDate()=" + getDueDate() + ", getDateAssigned()=" + getDateAssigned()
				+ ", getDateCompleted()=" + getDateCompleted() + ", getAssignedTo()=" + getAssignedTo()
				+ ", getStudentGroup()=" + getStudentGroup() + ", getGradeRecieved()=" + getGradeRecieved()
				+ ", getOverridenGrade()=" + getOverridenGrade() + ", getDeleted()=" + getDeleted() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}

}
