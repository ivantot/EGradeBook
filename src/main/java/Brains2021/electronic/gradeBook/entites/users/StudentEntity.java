package Brains2021.electronic.gradeBook.entites.users;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.entites.AssignmentEntity;
import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.security.Views;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Students")
@PrimaryKeyJoinColumn(name = "StudentID")
public class StudentEntity extends UserEntity {

	@JsonView(Views.Student.class)
	@JsonBackReference(value = "ref2")
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "belongsToStudentGroup")
	private StudentGroupEntity belongsToStudentGroup;

	@JsonView(Views.Student.class)
	@JsonManagedReference(value = "ref3")
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinTable(name = "Parents_and_Children", joinColumns = {
			@JoinColumn(name = "StudentID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "ParentID", nullable = false, updatable = false) })
	private Set<ParentEntity> parents = new HashSet<>();

	@OneToMany(mappedBy = "assignedTo", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Set<AssignmentEntity> givenAssigmnents = new HashSet<>();

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

	public StudentGroupEntity getBelongsToStudentGroup() {
		return belongsToStudentGroup;
	}

	public void setBelongsToStudentGroup(StudentGroupEntity belongsToStudentGroup) {
		this.belongsToStudentGroup = belongsToStudentGroup;
	}

	public Set<AssignmentEntity> getGivenAssigmnents() {
		return givenAssigmnents;
	}

	public void setGivenAssigmnents(Set<AssignmentEntity> givenAssigmnents) {
		this.givenAssigmnents = givenAssigmnents;
	}

}
