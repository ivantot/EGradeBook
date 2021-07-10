package Brains2021.electronic.gradeBook.entites.users;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Parents")
@PrimaryKeyJoinColumn(name = "ParentID")
public class ParentEntity extends UserEntity {

	@JsonView(Views.Parent.class)
	@Column(nullable = false)
	private String phoneNumber;

	@JsonView(Views.Parent.class)
	@JsonBackReference(value = "2")
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinTable(name = "Parents_and_Children", joinColumns = {
			@JoinColumn(name = "ParentID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "StudentID", nullable = false, updatable = false) })
	private Set<StudentEntity> children = new HashSet<>();

	public ParentEntity() {
		super();
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Set<StudentEntity> getChildren() {
		return children;
	}

	public void setChildren(Set<StudentEntity> children) {
		this.children = children;
	}

}
