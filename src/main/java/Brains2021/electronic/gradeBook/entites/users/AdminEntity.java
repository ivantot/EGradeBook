package Brains2021.electronic.gradeBook.entites.users;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;
/*
@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Admins")
@PrimaryKeyJoinColumn(name = "AdminID")
public class AdminEntity extends TeacherEntity {

	//administrator is appointed from present school staff

	@JsonView(Views.Principal.class)
	@Column(nullable = false)
	private Double adminBonus;

	public AdminEntity() {
		super();
	}

	public Double getAdminBonus() {
		return adminBonus;
	}

	public void setAdminBonus(Double adminBonus) {
		this.adminBonus = adminBonus;
	}

}
*/