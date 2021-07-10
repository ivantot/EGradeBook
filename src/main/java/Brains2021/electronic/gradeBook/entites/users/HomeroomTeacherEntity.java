package Brains2021.electronic.gradeBook.entites.users;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.security.Views;
/*
@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Homeroom_Teachers")
@PrimaryKeyJoinColumn(name = "HomeroomTeacherID")
public class HomeroomTeacherEntity extends TeacherEntity {

	@JsonView(Views.Principal.class)
	@Column(nullable = false)
	private Double homeroomBonus;

	@JsonView(Views.Teacher.class)
	@JsonBackReference(value = "3")
	@OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "inChargeOff")
	private StudentGroupEntity inChargeOff;

	public HomeroomTeacherEntity() {
		super();
	}

	public Double getHomeroomBonus() {
		return homeroomBonus;
	}

	public void setHomeroomBonus(Double homeroomBonus) {
		this.homeroomBonus = homeroomBonus;
	}

	public StudentGroupEntity getInChargeOff() {
		return inChargeOff;
	}

	public void setInChargeOff(StudentGroupEntity inChargeOff) {
		this.inChargeOff = inChargeOff;
	}

}
*/