package Brains2021.electronic.gradeBook.controllers;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.dtos.in.CreateStudentGroupDTO;
import Brains2021.electronic.gradeBook.dtos.out.GETStudentGroupsDTO;
import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.StudentGroupRepository;
import Brains2021.electronic.gradeBook.repositories.StudentRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.studentGroup.StudentGroupService;
import Brains2021.electronic.gradeBook.services.user.UserService;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ERole;

@RestController
@RequestMapping(path = "/api/v1/studentGroup")
public class StudentGroupController {

	@Autowired
	private StudentGroupRepository studentGroupRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private StudentRepository studentRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private StudentGroupService studentGroupService;

	@Autowired
	private UserService userService;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/********************************************************************************************
	 * POST endpoint for administrator looking to create new subject group -- postman code 006 --
	 * 
	 * @param student group
	 * @return if ok, new student group
	 ********************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newStudentGroup")
	public ResponseEntity<?> postNewStudentGroup(@Valid @RequestBody CreateStudentGroupDTO studentGroup) {

		logger.info("**POST STUDENT GROUP** Access to the endpoint successful.");

		// check db for student group
		logger.info("**POST STUDENT GROUP** Attempt to find if user group is in the database.");
		if (studentGroupRepo
				.findByYearAndYearIndex(studentGroup.getYear(), Integer.parseInt(studentGroup.getYearIndex()))
				.isPresent()) {
			logger.warn("**POST STUDENT GROUP** Student Group already in the database.");
			return new ResponseEntity<RESTError>(new RESTError(3001, "Student Group already in the database."),
					HttpStatus.BAD_REQUEST);
		}

		// populate fields and save to db
		logger.info("**POST STUDENT GROUP** Populate fields and save new student group to database");
		StudentGroupEntity newStudentGroup = new StudentGroupEntity();
		newStudentGroup.setDeleted(0);
		newStudentGroup.setYear(studentGroup.getYear());
		newStudentGroup.setYearIndex(Integer.parseInt(studentGroup.getYearIndex()));

		studentGroupRepo.save(newStudentGroup);

		return new ResponseEntity<String>(
				"Student Group " + newStudentGroup.getYear() + "-" + newStudentGroup.getYearIndex() + " created.",
				HttpStatus.OK);
	}

	/********************************************************************************************************************
	 * PUT endpoint for administrator or headmaster looking to assign a student to a student group -- postman code 017 --
	 * 
	 * @param student group
	 * @param student
	 * @return if ok, student linked to a student group
	 ********************************************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/assignStudentToStudentGroup")
	public ResponseEntity<?> assignStudentToStudentGroup(@RequestParam String username, @RequestParam String year,
			@RequestParam Integer yearIndex) {

		logger.info("**ADD STUDENT TO STUDENT GROUP** Access to the endpoint successful.");

		// validate yaer
		logger.info("**ADD STUDENT TO STUDENT GROUP** Attempt to validate year input.");
		if (!year.matches("^I|II|III|IV|V|VI|VII|VIII$")) {
			logger.warn("**ADD STUDENT TO STUDENT GROUP** Year input not valid.");
			return new ResponseEntity<RESTError>(
					new RESTError(3005, "Provide a valid year value by using roman numerals between I and VIII."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<StudentGroupEntity> studentGroup = studentGroupRepo.findByYearAndYearIndex(year, yearIndex);
		logger.info("**ADD STUDENT TO STUDENT GROUP** Attempt to find student group in the database.");
		// check db for student group
		if (studentGroup.isEmpty()) {
			logger.warn("**ADD STUDENT TO STUDENT GROUP** Student group not in the database.");
			return new ResponseEntity<RESTError>(new RESTError(3002, "Student Group not in database."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<UserEntity> user = userRepo.findByUsername(username);
		logger.info("**ADD STUDENT TO STUDENT GROUP** Attempt to find student in the database.");
		if (user.isEmpty()) {
			logger.warn("**ADD STUDENT TO STUDENT GROUP** Student not in the database.");
			return new ResponseEntity<RESTError>(new RESTError(3003, "Student not in database."),
					HttpStatus.BAD_REQUEST);
		}

		logger.info("**ADD STUDENT TO STUDENT GROUP** Attempt to find if student is deleted.");
		if (user.get().getDeleted() == 1) {
			logger.warn("**ADD STUDENT TO STUDENT GROUP** Student deleted.");
			return new ResponseEntity<RESTError>(new RESTError(3003, "Not an active student."), HttpStatus.BAD_REQUEST);
		}

		logger.info("**ADD STUDENT TO STUDENT GROUP** Attempt to find if user is a student.");
		if (!user.get().getRole().getName().equals(ERole.ROLE_STUDENT)) {
			logger.warn("**ADD STUDENT TO STUDENT GROUP** User is not a student.");
			return new ResponseEntity<RESTError>(new RESTError(3004, "User is not a student."), HttpStatus.BAD_REQUEST);
		}

		logger.info("**ADD STUDENT TO STUDENT GROUP** Put student to student group.");
		StudentEntity student = (StudentEntity) user.get();
		student.setBelongsToStudentGroup(studentGroup.get());
		userRepo.save(student);

		return new ResponseEntity<String>(
				"Student " + username + " asigned to student group " + year + "-" + yearIndex + ".", HttpStatus.OK);
	}

	/*****************************************************************************************************************************
	 * PUT endpoint for administrator or headmaster looking to assign a homeroom teacher to a student group -- postman code 018 --
	 * 
	 * @param student group
	 * @param student
	 * @return if ok, student linked to a student group
	 *****************************************************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/assignHomeroomToStudentGroup")
	public ResponseEntity<?> assignHomeroomToStudentGroup(@RequestParam String username, @RequestParam String year,
			@RequestParam Integer yearIndex) {

		logger.info("**ASSIGN HOMEROOM TO STUDENT GROUP** Access to the endpoint successful.");

		// validate year
		logger.info("**ASSIGN HOMEROOM TO STUDENT GROUP** Attempt to validate year input.");
		if (!year.matches("^I|II|III|IV|V|VI|VII|VIII$")) {
			logger.warn("**ASSIGN HOMEROOM TO STUDENT GROUP** Year input not valid.");
			return new ResponseEntity<RESTError>(
					new RESTError(3005, "Provide a valid year value, using roman numerals between I and VIII."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<StudentGroupEntity> studentGroup = studentGroupRepo.findByYearAndYearIndex(year, yearIndex);

		// check db for student group
		logger.info("**ASSIGN HOMEROOM TO STUDENT GROUP** Attempt to find student group in the database.");
		if (studentGroup.isEmpty()) {
			logger.warn("**ASSIGN HOMEROOM TO STUDENT GROUP** Student group not in the database.");
			return new ResponseEntity<RESTError>(new RESTError(3002, "Student Group not in database."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<UserEntity> user = userRepo.findByUsername(username);
		logger.info("**ASSIGN HOMEROOM TO STUDENT GROUP** Attempt to find user in the database.");
		if (user.isEmpty()) {
			logger.warn("**ASSIGN HOMEROOM TO STUDENT GROUP** User not in the database.");
			return new ResponseEntity<RESTError>(new RESTError(3003, "User not in database."), HttpStatus.BAD_REQUEST);
		}

		logger.info("**ASSIGN HOMEROOM TO STUDENT GROUP** Attempt to find if techer is deleted.");
		if (user.get().getDeleted() == 1) {
			logger.warn("**ASSIGN HOMEROOM TO STUDENT GROUP** Teacher deleted.");
			return new ResponseEntity<RESTError>(new RESTError(3003, "Not an active teacher."), HttpStatus.BAD_REQUEST);
		}

		logger.info("**ASSIGN HOMEROOM TO STUDENT GROUP** Attempt to find if role corresponds to homeroom teacher.");
		if (!user.get().getRole().getName().equals(ERole.ROLE_HOMEROOM)) {
			logger.warn("**ASSIGN HOMEROOM TO STUDENT GROUP** Wrong role.");
			return new ResponseEntity<RESTError>(new RESTError(3004, "User is not a homeroom teacher."),
					HttpStatus.BAD_REQUEST);
		}

		logger.info("**ASSIGN HOMEROOM TO STUDENT GROUP** Put homeroom teacher to student group.");
		TeacherEntity homeroomTeacher = (TeacherEntity) user.get();
		homeroomTeacher.setInChargeOf(studentGroup.get());
		userRepo.save(homeroomTeacher);

		return new ResponseEntity<String>("Teacher " + username + " asigned to student group " + year + "-" + yearIndex
				+ " as a homeroom teacher.", HttpStatus.OK);
	}

	/******************************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to soft delete a student group. -- postman code 038 --
	 * 
	 * @param studentGroup id
	 * @return if ok set deleted to 1
	 ******************************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/deleteStudentGroup/{studentGroupID}")
	public ResponseEntity<?> deleteStudentGroup(@PathVariable Long studentGroupID) {

		logger.info("**DELETE STUDENT GROUP** Access to the endpoint successful.");

		logger.info("**DELETE STUDENT GROUP** Attempt to find an active student group in database.");
		// initial check for existance in db
		Optional<StudentGroupEntity> ogStudentGroup = studentGroupRepo.findById(studentGroupID);
		if (ogStudentGroup.isEmpty() || ogStudentGroup.get().getDeleted() == 1) {
			logger.warn("**DELETE STUDENT GROUP** Student group not in database or deleted.");
			return new ResponseEntity<RESTError>(
					new RESTError(7530,
							"Student group not found in database or is deleted, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**DELETE STUDENT GROUP** Attempt successful.");

		logger.info("**DELETE STUDENT GROUP** Attempt to unlink students from student group, if any.");
		// unlink students and save
		List<StudentEntity> students = studentRepo.findByBelongsToStudentGroup(ogStudentGroup.get());

		if (!students.isEmpty()) {
			for (StudentEntity studentEntity : students) {
				studentEntity.setBelongsToStudentGroup(null);
			}
			studentRepo.saveAll(students);
			logger.info("**DELETE STUDENT GROUP** Attempt successful, students unlinked and saved to db.");
		}

		logger.info("**DELETE STUDENT GROUP** Attempt to unlink homeroom teacher from student group, if any.");
		// unlink homeroomTeacher
		Optional<TeacherEntity> homeroomTeacher = teacherRepo.findByInChargeOf(ogStudentGroup.get());

		if (homeroomTeacher.isPresent()) {
			homeroomTeacher.get().setInChargeOf(null);
			homeroomTeacher.get().setIsHomeroomTeacher(0);
			homeroomTeacher.get().setSalaryHomeroomBonus(0.00);
			teacherRepo.save(homeroomTeacher.get());
			logger.info("**DELETE STUDENT GROUP** Attempt successful, homeroom teacher unlinked and saved to db.");
		}

		// set to deleted and save
		logger.info("**DELETE STUDENT GROUP** Attempt on editing deleted field and saving to db.");
		ogStudentGroup.get().setDeleted(1);
		studentGroupRepo.save(ogStudentGroup.get());
		logger.info("**DELETE STUDENT GROUP** Attempt successful.");

		return new ResponseEntity<String>(
				"Student group with id " + studentGroupID + " deleted, students and homeroom teacher unlinked.",
				HttpStatus.OK);
	}

	/***************************************************************************************************
	 * PUT endpoint for administrator looking to restore a deleted student group. -- postman code 039 --
	 * 
	 * @param studentGroup id
	 * @return if ok set deleted to 0
	 ***************************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/restoreStudentGroup/{studentGroupID}")
	public ResponseEntity<?> restoreStudentGroup(@PathVariable Long studentGroupID) {

		logger.info("**RESTORE STUDENT GROUP** Access to the endpoint successful.");

		logger.info("**RESTORE STUDENT GROUP** Attempt to find a deleted student group in database.");
		// initial check for existance in db
		Optional<StudentGroupEntity> ogStudentGroup = studentGroupRepo.findById(studentGroupID);
		if (ogStudentGroup.isEmpty() || ogStudentGroup.get().getDeleted() == 0) {
			logger.warn("**RESTORE STUDENT GROUP** Student group not in database or active.");
			return new ResponseEntity<RESTError>(
					new RESTError(7531, "Student group not found in database or is active, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**RESTORE STUDENT GROUP** Attempt successful.");

		// set to active and save
		logger.info("**RESTORE STUDENT GROUP** Attempt on editing deleted field and saving to db.");
		ogStudentGroup.get().setDeleted(0);
		studentGroupRepo.save(ogStudentGroup.get());
		logger.info("**RESTORE STUDENT GROUP** Attempt successful.");

		return new ResponseEntity<String>("Assignment with id " + studentGroupID + " restored.", HttpStatus.OK);
	}

	/*********************************************************************************************
	 * GET endpoint for administrator looking to fetch all student groups. -- postman code 065 --
	 * 
	 * @param 
	 * @return if ok list of all student groups in database
	 *********************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/")
	public ResponseEntity<?> getAllStudentGroups() {

		logger.info("**GET ALL STUDENT GROUPS** Access to the endpoint successful.");

		logger.info("**GET ALL STUDENT GROUPS** Attempt to find student groups.");
		if (studentGroupRepo.findAll() == null) {
			logger.warn("**GET ALL STUDENT GROUPS** No student groups in database.");
			return new ResponseEntity<RESTError>(new RESTError(6532, "No student groups found in database."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET ALL STUDENT GROUPS** Attempt successful, student groups are present.");

		// fetch student groups and present to admin/headmaster
		logger.info("**GET ALL STUDENT GROUPSS** Attempt to invoke service to translate student groups to DTOSs.");
		List<GETStudentGroupsDTO> ogstudentGroupsDTO = studentGroupService
				.GETStudentGroupsDTOtranslation((List<StudentGroupEntity>) studentGroupRepo.findAll());
		logger.info("**GET ALL STUDENT GROUPS** Attempt successful, list retrieved. Exiting controller");

		return new ResponseEntity<List<GETStudentGroupsDTO>>(ogstudentGroupsDTO, HttpStatus.OK);
	}

	/************************************************************************************************
	 * GET endpoint for administrator looking to fetch a student group by ID. -- postman code 066 --
	 * 
	 * @param studentGroup id
	 * @return if ok student grooup with given id
	 ************************************************************************************************/
	@SuppressWarnings("unlikely-arg-type")
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER", "ROLE_HOMEROOM" })
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/{studentGroupID}")
	public ResponseEntity<?> getStudentGroupByID(@PathVariable Long studentGroupID) {

		logger.info("**GET STUDENT GROUP BY ID** Access to the endpoint successful.");

		logger.info(
				"**GET STUDENT GROUP BY ID** Attempt to see if logged user is student groups homeroom teacher, admin or headmaster.");
		Optional<StudentGroupEntity> ogStudentGroup = studentGroupRepo.findById(studentGroupID);
		if (!userService.amIHeadmaster() && !userService.amIAdmin() && !userRepo.findByUsername(userService.whoAmI())
				.equals(ogStudentGroup.get().getHomeroomTeacher().getUsername())) {
			logger.warn(
					"**GET STUDENT GROUP BY ID** Looged user not student groups homeroom teacher, admin or headmaster.");
			return new ResponseEntity<RESTError>(
					new RESTError(6595, "ooged user not student groups homeroom teacher, admin or headmaster."),
					HttpStatus.BAD_REQUEST);
		}

		logger.info("**GET STUDENT GROUP BY ID** Attempt to find a student group in database.");
		if (ogStudentGroup.isEmpty()) {
			logger.warn("**GET STUDENT GROUP BY ID** No student group with given id in database.");
			return new ResponseEntity<RESTError>(new RESTError(6535, "No student group with given id in database."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET STUDENT GROUP BY ID** Student group found.");

		logger.info("**GET STUDENT GROUP BY ID** All done, output to DTO.");
		return new ResponseEntity<GETStudentGroupsDTO>(
				studentGroupService.GETStudentGroupDTOtranslation(ogStudentGroup.get()), HttpStatus.OK);
	}

}
