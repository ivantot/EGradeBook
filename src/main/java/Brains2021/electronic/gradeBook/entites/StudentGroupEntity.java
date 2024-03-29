package Brains2021.electronic.gradeBook.entites;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
	private String year;

	@Column(nullable = false)
	private Integer yearIndex;

	@JsonManagedReference(value = "ref2")
	@OneToMany(mappedBy = "belongsToStudentGroup", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	private Set<StudentEntity> students = new HashSet<>();

	@OneToOne(mappedBy = "inChargeOf", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	private TeacherEntity homeroomTeacher;

	@OneToMany(mappedBy = "studentGroup", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Set<StudentGroupTakingASubjectEntity> subjectsTaken = new HashSet<>();

	private Integer deleted;

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

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
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

	public Set<StudentGroupTakingASubjectEntity> getSubjectsTaken() {
		return subjectsTaken;
	}

	public void setSubjectsTaken(Set<StudentGroupTakingASubjectEntity> subjectsTaken) {
		this.subjectsTaken = subjectsTaken;
	}

	public Integer getYearIndex() {
		return yearIndex;
	}

	public void setYearIndex(Integer yearIndex) {
		this.yearIndex = yearIndex;
	}

	public Integer getDeleted() {
		return deleted;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
