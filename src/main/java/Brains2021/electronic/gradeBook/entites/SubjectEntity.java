package Brains2021.electronic.gradeBook.entites;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Subjects")
public class SubjectEntity {

	@Id
	@GeneratedValue
	@JsonView(Views.Admin.class)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@JsonView(Views.Admin.class)
	private ESubjectName name;

	@Column(nullable = false)
	@JsonView(Views.Admin.class)
	private String yearOfSchooling;

	@JsonView(Views.Admin.class)
	private String description;

	@Column(nullable = false)
	private Integer weeklyHoursRequired;

	@OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Set<TeacherSubjectEntity> teachersTeaching = new HashSet<>();

	private Integer deleted;

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

	public ESubjectName getName() {
		return name;
	}

	public void setName(ESubjectName name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getYearOfSchooling() {
		return yearOfSchooling;
	}

	public void setYearOfSchooling(String yearofSchooling) {
		this.yearOfSchooling = yearofSchooling;
	}

	public Integer getWeeklyHoursRequired() {
		return weeklyHoursRequired;
	}

	public void setWeeklyHoursRequired(Integer weeklyHoursRequired) {
		this.weeklyHoursRequired = weeklyHoursRequired;
	}

	public Set<TeacherSubjectEntity> getTeachersTeaching() {
		return teachersTeaching;
	}

	public void setTeachersTeaching(Set<TeacherSubjectEntity> teachersTeaching) {
		this.teachersTeaching = teachersTeaching;
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
