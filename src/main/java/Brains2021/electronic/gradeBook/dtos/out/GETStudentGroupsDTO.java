package Brains2021.electronic.gradeBook.dtos.out;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;

//@JsonRootName(value = "Student group")
@JsonPropertyOrder({ "id", "designation", "homeroomTeacher", "students", "subjectsTaken", "deleted" })
public class GETStudentGroupsDTO {

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "id")
	private Long id;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Designation")
	private String designation;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Students")
	private Set<String> students = new HashSet<>();

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Homeroom teacher")
	private String homeroomTeacher;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Subjects taken")
	private Set<String> subjectsTaken = new HashSet<>();

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "deleted")
	private Integer deleted;

	public GETStudentGroupsDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public Set<String> getStudents() {
		return students;
	}

	public void setStudents(Set<String> students) {
		this.students = students;
	}

	public String getHomeroomTeacher() {
		return homeroomTeacher;
	}

	public void setHomeroomTeacher(String homeroomTeacher) {
		this.homeroomTeacher = homeroomTeacher;
	}

	public Set<String> getSubjectsTaken() {
		return subjectsTaken;
	}

	public void setSubjectsTaken(Set<String> subjectsTaken) {
		this.subjectsTaken = subjectsTaken;
	}

	public Integer getDeleted() {
		return deleted;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

	@Override
	public String toString() {
		return "GETStudentGroupsDTO [id=" + id + ", designation=" + designation + ", students=" + students
				+ ", homeroomTeacher=" + homeroomTeacher + ", subjectsTaken=" + subjectsTaken + ", deleted=" + deleted
				+ "]";
	}

}
