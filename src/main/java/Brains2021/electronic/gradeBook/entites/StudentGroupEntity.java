package Brains2021.electronic.gradeBook.entites;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "StudentGroup")
public class StudentGroupEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private Integer year;

	@Column(nullable = false)
	private Integer yearIndex;

	@JsonManagedReference(value = "ref2")
	@OneToMany(mappedBy = "belongsToStudentGroup", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	private Set<StudentEntity> students = new HashSet<>();

	@OneToOne(mappedBy = "inChargeOf", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	private TeacherEntity homeroomTeacher;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinTable(name = "Subjects_and_StudentGroups", joinColumns = {
			@JoinColumn(name = "StudentGroupID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "SubjectID", nullable = false, updatable = false) })
	private Set<SubjectEntity> subjectsTaken = new HashSet<>();

	private Boolean archived;

	@Version
	private Integer version;

	public StudentGroupEntity() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getYear_index() {
		return yearIndex;
	}

	public void setYear_index(Integer year_index) {
		this.yearIndex = year_index;
	}

	public Set<StudentEntity> getStudents() {
		return students;
	}

	public void setStudents(Set<StudentEntity> students) {
		this.students = students;
	}

	public TeacherEntity getHomeroomTeacher() {
		return homeroomTeacher;
	}

	public void setHomeroomTeacher(TeacherEntity homeroomTeacher) {
		this.homeroomTeacher = homeroomTeacher;
	}

	public Set<SubjectEntity> getSubjectsTaken() {
		return subjectsTaken;
	}

	public void setSubjectsTaken(Set<SubjectEntity> subjectsTaken) {
		this.subjectsTaken = subjectsTaken;
	}

	public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
