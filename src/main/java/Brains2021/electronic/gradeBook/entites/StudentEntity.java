package Brains2021.electronic.gradeBook.entites;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Students")
@PrimaryKeyJoinColumn(name = "StudentID")
public class StudentEntity extends AbstractUserEntity {

	@JsonBackReference(value = "1")
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "belongsToStudentGroup")
	private StudentGroupEntity belongsToStudentGroup;

	//@JsonManagedReference(value = "2")
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinTable(name = "Parents_and_Children", joinColumns = {
			@JoinColumn(name = "StudentID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "ParentID", nullable = false, updatable = false) })
	private Set<ParentEntity> parents = new HashSet<>();

	public StudentEntity() {
		super();
	}

	public StudentGroupEntity getBelongsToClass() {
		return belongsToStudentGroup;
	}

	public void setBelongsToClass(StudentGroupEntity belongsToClass) {
		this.belongsToStudentGroup = belongsToClass;
	}

	public Set<ParentEntity> getParents() {
		return parents;
	}

	public void setParents(Set<ParentEntity> parents) {
		this.parents = parents;
	}

}
