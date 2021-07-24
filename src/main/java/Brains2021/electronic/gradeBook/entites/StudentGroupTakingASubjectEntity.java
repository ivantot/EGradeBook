package Brains2021.electronic.gradeBook.entites;

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

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Student_groups_and_teacher_subjects")
public class StudentGroupTakingASubjectEntity {

	@Id
	@GeneratedValue
	@Column(name = "Student_groups_and_SubjectsID")
	private Long id;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "Teacher_subjectID")
	private TeacherSubjectEntity teacherSubject;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "Student_groupID")
	private StudentGroupEntity studentGroup;

	private Integer weeklyHours;

	private Integer deleted;

	@Version
	private Integer version;

	public StudentGroupTakingASubjectEntity() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TeacherSubjectEntity getTeacherSubject() {
		return teacherSubject;
	}

	public void setTeacherSubject(TeacherSubjectEntity teacherSubject) {
		this.teacherSubject = teacherSubject;
	}

	public StudentGroupEntity getStudentGroup() {
		return studentGroup;
	}

	public void setStudentGroup(StudentGroupEntity studentGroup) {
		this.studentGroup = studentGroup;
	}

	public Integer getWeeklyHours() {
		return weeklyHours;
	}

	public void setWeeklyHours(Integer weeklyHours) {
		this.weeklyHours = weeklyHours;
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
