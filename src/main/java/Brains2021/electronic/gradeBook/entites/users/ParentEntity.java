package Brains2021.electronic.gradeBook.entites.users;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Parents")
@PrimaryKeyJoinColumn(name = "ParentID")
public class ParentEntity extends UserEntity {

	@JsonView(Views.Parent.class)
	private String phoneNumber;

	@JsonView(Views.Parent.class)
	//@JsonBackReference(value = "ref3")
	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Set<StudentParentEntity> children = new HashSet<>();

	public ParentEntity() {
		super();
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Set<StudentParentEntity> getChildren() {
		return children;
	}

	public void setChildren(Set<StudentParentEntity> children) {
		this.children = children;
	}

}
