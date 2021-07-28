package Brains2021.electronic.gradeBook.dtos;

import java.time.LocalDate;

public class EmailObjectDTO {

	private String to;
	private String cc;
	private String subject;
	private String studentName;
	private String studentLastName;
	private String teacherName;
	private String teacherLastName;
	private String gradedSubject;
	private String grade;
	private LocalDate date;
	private String assignment;
	private String description;
	private String overridenGrade;

	public String getAssignment() {
		return assignment;
	}

	public void setAssignment(String assignment) {
		this.assignment = assignment;
	}

	public EmailObjectDTO() {
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public String getStudentLastName() {
		return studentLastName;
	}

	public void setStudentLastName(String studentLastName) {
		this.studentLastName = studentLastName;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	public String getTeacherLastName() {
		return teacherLastName;
	}

	public void setTeacherLastName(String teacherLastName) {
		this.teacherLastName = teacherLastName;
	}

	public String getGradedSubject() {
		return gradedSubject;
	}

	public void setGradedSubject(String gradedSubject) {
		this.gradedSubject = gradedSubject;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOverridenGrade() {
		return overridenGrade;
	}

	public void setOverridenGrade(String overridenGrade) {
		this.overridenGrade = overridenGrade;
	}

	@Override
	public String toString() {
		return "EmailObjectDTO [to=" + to + ", cc=" + cc + ", subject=" + subject + ", studentName=" + studentName
				+ ", studentLastName=" + studentLastName + ", teacherName=" + teacherName + ", teacherLastName="
				+ teacherLastName + ", gradedSubject=" + gradedSubject + ", grade=" + grade + ", date=" + date
				+ ", assignment=" + assignment + ", description=" + description + ", overridenGrade=" + overridenGrade
				+ ", getAssignment()=" + getAssignment() + ", getTo()=" + getTo() + ", getCc()=" + getCc()
				+ ", getSubject()=" + getSubject() + ", getStudentName()=" + getStudentName()
				+ ", getStudentLastName()=" + getStudentLastName() + ", getTeacherName()=" + getTeacherName()
				+ ", getTeacherLastName()=" + getTeacherLastName() + ", getGradedSubject()=" + getGradedSubject()
				+ ", getGrade()=" + getGrade() + ", getDate()=" + getDate() + ", getDescription()=" + getDescription()
				+ ", getOverridenGrade()=" + getOverridenGrade() + ", getClass()=" + getClass() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString() + "]";
	}

}
