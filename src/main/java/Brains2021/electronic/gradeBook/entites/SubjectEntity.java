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
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Subjects")
public class SubjectEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private String name;

	private String description;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinTable(name = "Subjects_and_StudentGroups", joinColumns = {
			@JoinColumn(name = "SubjectID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "StudentGroupID", nullable = false, updatable = false) })
	private Set<StudentGroupEntity> studentGroupsTaking = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinTable(name = "Subjects_and_Teachers", joinColumns = {
			@JoinColumn(name = "SubjectID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "TeacherID", nullable = false, updatable = false) })
	private Set<TeacherEntity> teachersTeaching = new HashSet<>();

	private Boolean deleted;

	@Version
	private Integer version;

	public SubjectEntity() {
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

	public Set<StudentGroupEntity> getStudentGroupsTaking() {
		return studentGroupsTaking;
	}

	public void setStudentGroupsTaking(Set<StudentGroupEntity> studentGroupsTaking) {
		this.studentGroupsTaking = studentGroupsTaking;
	}

	public Set<TeacherEntity> getTeachersTeaching() {
		return teachersTeaching;
	}

	public void setTeachersTeaching(Set<TeacherEntity> teachersTeaching) {
		this.teachersTeaching = teachersTeaching;
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
