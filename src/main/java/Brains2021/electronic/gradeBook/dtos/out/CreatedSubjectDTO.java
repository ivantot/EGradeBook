package Brains2021.electronic.gradeBook.dtos.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.security.Views;

//@JsonRootName(value = "Created Subject")
@JsonPropertyOrder({ "name", "description" })
public class CreatedSubjectDTO {

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Subject name")
	private String name;

	@JsonView(Views.Admin.class)
	@JsonProperty(value = "Description")
	private String description;

	public CreatedSubjectDTO() {
		super();
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

}
