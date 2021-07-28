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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Teachers_and_subjects")
public class TeacherSubjectEntity {

	@Id
	@GeneratedValue
	@Column(name = "SubjectTeacherID")
	private Long id;

	private Integer weeklyHoursAlloted;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "teacherID")
	private TeacherEntity teacher;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "subjectID")
	private SubjectEntity subject;

	@OneToMany(mappedBy = "teacherSubject", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Set<StudentGroupTakingASubjectEntity> studentGroupsTakingASubject = new HashSet<>();

	@OneToMany(mappedBy = "teacherIssuing", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Set<AssignmentEntity> assignmentsGiven = new HashSet<>();

	private Integer deleted;

	@Version
	private Integer version;

	public TeacherSubjectEntity() {
		super();
	}

	public TeacherSubjectEntity(TeacherEntity teacher, SubjectEntity subject) {
		super();
		this.teacher = teacher;
		this.subject = subject;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TeacherEntity getTeacher() {
		return teacher;
	}

	public void setTeacher(TeacherEntity teacher) {
		this.teacher = teacher;
	}

	public SubjectEntity getSubject() {
		return subject;
	}

	public void setSubject(SubjectEntity subject) {
		this.subject = subject;
	}

	public Set<StudentGroupTakingASubjectEntity> getStudentGroupsTakingASubject() {
		return studentGroupsTakingASubject;
	}

	public void setStudentGroupsTakingASubject(Set<StudentGroupTakingASubjectEntity> studentGroupsTakingASubject) {
		this.studentGroupsTakingASubject = studentGroupsTakingASubject;
	}

	public Set<AssignmentEntity> getAssignmentsGiven() {
		return assignmentsGiven;
	}

	public void setAssignmentsGiven(Set<AssignmentEntity> assignmentsGiven) {
		this.assignmentsGiven = assignmentsGiven;
	}

	public Integer getWeeklyHoursAlloted() {
		return weeklyHoursAlloted;
	}

	public void setWeeklyHoursAlloted(Integer weeklyHoursAlloted) {
		this.weeklyHoursAlloted = weeklyHoursAlloted;
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
