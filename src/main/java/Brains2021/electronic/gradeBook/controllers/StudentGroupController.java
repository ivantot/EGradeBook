package Brains2021.electronic.gradeBook.controllers;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.dtos.in.CreateStudentGroupDTO;
import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.StudentGroupRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ERole;

@RestController
@RequestMapping(path = "/api/v1/studentGroup")
public class StudentGroupController {

	@Autowired
	private StudentGroupRepository studentGroupRepo;

	@Autowired
	private UserRepository userRepo;

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
			return new ResponseEntity<RESTError>(new RESTError(3001, "Student Group already in the database."),
					HttpStatus.BAD_REQUEST);
		}

		// populate fields and save to db
		StudentGroupEntity newStudentGroup = new StudentGroupEntity();
		newStudentGroup.setDeleted(0);
		newStudentGroup.setYear(studentGroup.getYear());
		newStudentGroup.setYearIndex(Integer.parseInt(studentGroup.getYearIndex()));

		studentGroupRepo.save(newStudentGroup);

		return new ResponseEntity<String>(
				"Student Group " + newStudentGroup.getYear() + "-" + newStudentGroup.getYearIndex() + " created.",
				HttpStatus.OK);
	}

	/***********************************************************************************************
	 * PUT endpoint for administrator or headmaster looking to assign a student to a student group
	 * -- postman code adm017 --
	 * 
	 * @param student group
	 * @param student
	 * @return if ok, student linked to a student group
	 **********************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/assignStudentToStudentGroup")
	public ResponseEntity<?> assignStudentToStudentGroup(@RequestParam String username, @RequestParam String year,
			@RequestParam Integer yearIndex) {

		// validate yaer
		if (!year.matches("^I|II|III|IV|V|VI|VII|VIII$")) {
			return new ResponseEntity<RESTError>(
					new RESTError(3005, "Provide a valid year value by using roman numerals between I and VIII."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<StudentGroupEntity> studentGroup = studentGroupRepo.findByYearAndYearIndex(year, yearIndex);

		// check db for student group
		if (studentGroup.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(3002, "Student Group not in database."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<UserEntity> user = userRepo.findByUsername(username);

		if (user.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(3003, "Student not in database."),
					HttpStatus.BAD_REQUEST);
		}

		if (user.get().getDeleted() == 1) {
			return new ResponseEntity<RESTError>(new RESTError(3003, "Not an active student."), HttpStatus.BAD_REQUEST);
		}

		if (!user.get().getRole().getName().equals(ERole.ROLE_STUDENT)) {
			return new ResponseEntity<RESTError>(new RESTError(3004, "User is not a student."), HttpStatus.BAD_REQUEST);
		}

		StudentEntity student = (StudentEntity) user.get();
		student.setBelongsToStudentGroup(studentGroup.get());
		userRepo.save(student);

		return new ResponseEntity<String>(
				"Student " + username + " asigned to student group " + year + "-" + yearIndex + ".", HttpStatus.OK);
	}

	/*******************************************************************************************************
	 * PUT endpoint for administrator or headmaster looking to assign a homeroom teacher to a student group
	 * -- postman code adm018 --
	 * 
	 * @param student group
	 * @param student
	 * @return if ok, student linked to a student group
	 *******************************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/assignHomeroomToStudentGroup")
	public ResponseEntity<?> assignHomeroomToStudentGroup(@RequestParam String username, @RequestParam String year,
			@RequestParam Integer yearIndex) {

		// validate year
		if (!year.matches("^I|II|III|IV|V|VI|VII|VIII$")) {
			return new ResponseEntity<RESTError>(
					new RESTError(3005, "Provide a valid year value, using roman numerals between I and VIII."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<StudentGroupEntity> studentGroup = studentGroupRepo.findByYearAndYearIndex(year, yearIndex);

		// check db for student group
		if (studentGroup.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(3002, "Student Group not in database."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<UserEntity> user = userRepo.findByUsername(username);

		if (user.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(3003, "Teacher not in database."),
					HttpStatus.BAD_REQUEST);
		}

		if (user.get().getDeleted() == 1) {
			return new ResponseEntity<RESTError>(new RESTError(3003, "Not an active teacher."), HttpStatus.BAD_REQUEST);
		}

		if (!user.get().getRole().getName().equals(ERole.ROLE_HOMEROOM)) {
			return new ResponseEntity<RESTError>(new RESTError(3004, "User is not a homeroom teacher."),
					HttpStatus.BAD_REQUEST);
		}

		TeacherEntity homeroomTeacher = (TeacherEntity) user.get();
		homeroomTeacher.setInChargeOf(studentGroup.get());
		userRepo.save(homeroomTeacher);

		return new ResponseEntity<String>("Teacher " + username + " asigned to student group " + year + "-" + yearIndex
				+ " as a homeroom teacher.", HttpStatus.OK);
	}

}
