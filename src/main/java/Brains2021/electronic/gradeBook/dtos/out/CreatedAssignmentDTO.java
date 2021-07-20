package Brains2021.electronic.gradeBook.dtos.out;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;

//@JsonRootName(value = "New Teacher")
@JsonPropertyOrder({ "type", "description", "teacher", "subject", "studyYear", "semester", "dateCreated" })
public class CreatedAssignmentDTO {

	@JsonView(Views.Teacher.class)
	@JsonProperty(value = "Assignment type")
	private String type;

	@JsonView(Views.Teacher.class)
	@JsonProperty(value = "Description")
	private String description;

	@JsonView(Views.Teacher.class)
	@JsonProperty(value = "Semester")
	private Integer semester;

	@JsonView(Views.Teacher.class)
	@JsonProperty(value = "Study year")
	private String studyYear;

	@JsonView(Views.Teacher.class)
	@JsonProperty(value = "Date of creation")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate dateCreated;

	@JsonView(Views.Teacher.class)
	@JsonProperty(value = "Teacher issuing")
	private String teacher;

	@JsonView(Views.Teacher.class)
	@JsonProperty(value = "Subject")
	private String subject;

	public CreatedAssignmentDTO() {
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

}
