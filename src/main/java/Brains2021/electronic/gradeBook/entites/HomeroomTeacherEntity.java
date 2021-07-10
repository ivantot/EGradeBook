package Brains2021.electronic.gradeBook.entites;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
@Table(name = "Homeroom_Teachers")
@PrimaryKeyJoinColumn(name = "HomeroomTeacherID")
public class HomeroomTeacherEntity extends AbstractUserEntity {

	@JsonBackReference(value = "3")
	@OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "inChargeOff")
	private StudentGroupEntity inChargeOff;

	public HomeroomTeacherEntity() {
		super();
	}

	public StudentGroupEntity getInChargeOff() {
		return inChargeOff;
	}

	public void setInChargeOff(StudentGroupEntity inChargeOff) {
		this.inChargeOff = inChargeOff;
	}

}
