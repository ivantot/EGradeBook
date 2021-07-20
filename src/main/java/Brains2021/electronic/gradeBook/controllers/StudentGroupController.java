package Brains2021.electronic.gradeBook.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.dtos.in.CreateStudentGroupDTO;
import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.repositories.StudentGroupRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.utils.RESTError;

@RestController
@RequestMapping(path = "/api/v1/studentGroup")
public class StudentGroupController {

	@Autowired
	private StudentGroupRepository studentGroupRepo;

	/***************************************************************************************
	 * POST endpoint for administrator looking to create new subject group
	 * -- postman code adm006 --
	 * 
	 * @param student group
	 * @return if ok, new student group
	 ***************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newStudentGroup")
	public ResponseEntity<?> postNewStudentGroup(@Valid @RequestBody CreateStudentGroupDTO studentGroup) {

		// check db for student group
		if (studentGroupRepo
				.findByYearAndYearIndex(studentGroup.getYear(), Integer.parseInt(studentGroup.getYearIndex()))
				.isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(3001, "Subject already in the database."),
					HttpStatus.BAD_REQUEST);
		}

		// populate fields and save to db
		StudentGroupEntity newStudentGroup = new StudentGroupEntity();
		newStudentGroup.setDeleted(false);
		newStudentGroup.setYear(studentGroup.getYear());
		newStudentGroup.setYearIndex(Integer.parseInt(studentGroup.getYearIndex()));

		studentGroupRepo.save(newStudentGroup);

		return new ResponseEntity<String>(
				"Student Group " + newStudentGroup.getYear() + "-" + newStudentGroup.getYearIndex() + " created.",
				HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT endpoint for administrator or headmaster looking to assign a student to a student grooup
	 * -- postman code adm017 --
	 * 
	 * @param student group
	 * @param student
	 * @return if ok, student linked to a student group
	 ***************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/assignStudentToStudentGroup")
	public ResponseEntity<?> assignToStudentGroup(String username, Long studentGroupID) {

		// TODO

		return new ResponseEntity<String>("Student " + "TODO" + " asigned to student group " + "TODO" + "-" + "TODO.",
				HttpStatus.OK);
	}

}
