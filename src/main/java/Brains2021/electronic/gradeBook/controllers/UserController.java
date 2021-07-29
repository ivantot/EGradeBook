package Brains2021.electronic.gradeBook.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.dtos.in.CreateParentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateStudentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherDTO;
import Brains2021.electronic.gradeBook.dtos.in.UpdatePasswordDTO;
import Brains2021.electronic.gradeBook.dtos.in.UpdatePhoneNumberDTO;
import Brains2021.electronic.gradeBook.dtos.in.UpdateUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetChildrenDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetParentsDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.UpdatedRoleDTO;
import Brains2021.electronic.gradeBook.dtos.out.UserTokenDTO;
import Brains2021.electronic.gradeBook.entites.RoleEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.ParentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentParentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.RoleRepository;
import Brains2021.electronic.gradeBook.repositories.StudentParentRepository;
import Brains2021.electronic.gradeBook.repositories.StudentRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.download.DownloadServiceImp;
import Brains2021.electronic.gradeBook.services.user.UserService;
import Brains2021.electronic.gradeBook.utils.Encryption;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ERole;

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private UserService userService;

	@Autowired
	private StudentRepository studentRepo;

	@Autowired
	private DownloadServiceImp downloadService;

	@Autowired
	private StudentParentRepository studentParentRepo;

	@Autowired
	TeacherSubjectRepository teacherSubjectRepo;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/***************************************************************************************
	 * POST login endpoint accessible to all
	 * -- postman code adm000 --
	 * 	
	 * @param username
	 * @param password
	 * @return if ok, token, else unauthorized status
	 **************************************************************************************/
	@RequestMapping(method = RequestMethod.POST, path = "/login")
	public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
		// find user by username
		logger.info("**LOGIN** User " + username + " attempting to log into the system.");
		Optional<UserEntity> user = userRepo.findByUsername(username);
		if (user.isPresent() && Encryption.validatePassword(password, user.get().getPassword())) {
			logger.info("**LOGIN** Both attempt to find user " + username
					+ " in the database and password validation turned out to be successful.");
			// if found, try password
			logger.info("**LOGIN** Attempting to create a token.");
			String token = userService.createJWTToken(user.get());
			// if ok make token
			logger.info("**LOGIN** Assigning the token to a DTO.");
			UserTokenDTO retVal = new UserTokenDTO(username, "Bearer " + token);
			logger.info("**LOGIN** User " + username + " has successfuly logged in the system.");
			return new ResponseEntity<UserTokenDTO>(retVal, HttpStatus.OK);
		}
		logger.warn("**LOGIN** User " + username + " not found or password not correct.");

		// otherwise return 401 unauthorized
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	/*************************************************************************************************
	 * utility POST endpoint for posting user roles to database, meant to be accessed by IT specialist
	 * -- postman code adm001 --
	 * 
	 * @param roleName
	 * @return if ok, new role
	 *************************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newRole")
	public ResponseEntity<?> postNewRole(@RequestParam String role) {

		if (!userService.isRoleInEnum(role)) {
			return new ResponseEntity<RESTError>(new RESTError(1000, "Role name not allowed, check ERole for details."),
					HttpStatus.BAD_REQUEST);
		}

		if (roleRepo.findByName(ERole.valueOf(role)).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(1001, "Role already in the database."),
					HttpStatus.BAD_REQUEST);
		}

		RoleEntity newRole = new RoleEntity();
		newRole.setName(ERole.valueOf(role));
		return new ResponseEntity<RoleEntity>(roleRepo.save(newRole), HttpStatus.OK);
	}

	/***************************************************************************************
	 * POST endpoint for administrator looking to create new student
	 * -- postman code adm002 --
	 * 
	 * @param student
	 * @return if ok, new student
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newStudent")
	public ResponseEntity<?> postNewStudent(@Valid @RequestBody CreateStudentDTO student) {

		// invoke a service for DTO translation to Entity
		StudentEntity newStudent = userService.createStudentDTOtranslation(student);

		// standard checks for new users, password match, username availability, no students with same IDs allowed
		if (!newStudent.getPassword().equals(newStudent.getRepeatedPassword())) {
			return new ResponseEntity<RESTError>(
					new RESTError(1020, "Passwords not matching, please check your entry."), HttpStatus.BAD_REQUEST);
		}
		if (userRepo.findByUsername(newStudent.getUsername()).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(1021, "Username already in database."),
					HttpStatus.BAD_REQUEST);
		}
		if (userRepo.findByJmbg(newStudent.getJmbg()).isPresent()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1022, "Student with an existing unique ID number in database."),
					HttpStatus.BAD_REQUEST);
		}

		if (studentRepo.findByStudentUniqueNumber(newStudent.getStudentUniqueNumber()).isPresent()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1023, "Student with an existing school ID number in database."),
					HttpStatus.BAD_REQUEST);
		}

		// encript password and save user
		newStudent.setPassword(Encryption.getPasswordEncoded(newStudent.getPassword()));
		userRepo.save(newStudent);

		// invoke service for Entity translation to DTO
		return userService.createdStudentDTOtranslation(newStudent);
	}

	/***************************************************************************************
	 * POST endpoint for administrator looking to create new parent
	 * -- postman code adm003 --
	 * 
	 * @param parent
	 * @return if ok, new parent
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newParent")
	public ResponseEntity<?> postNewParent(@Valid @RequestBody CreateParentDTO parent) {

		// invoke a service for DTO translation to Entity
		ParentEntity newParent = userService.createParentDTOtranslation(parent);

		// standard checks for new users, password match and username availability
		if (!newParent.getPassword().equals(newParent.getRepeatedPassword())) {
			return new ResponseEntity<RESTError>(
					new RESTError(1020, "Passwords not matching, please check your entry."), HttpStatus.BAD_REQUEST);
		}
		if (userRepo.findByUsername(newParent.getUsername()).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(1021, "Username already in database."),
					HttpStatus.BAD_REQUEST);
		}

		//allow for teacher being a parent in the database, no unique ID check

		// encript password and save user
		newParent.setPassword(Encryption.getPasswordEncoded(newParent.getPassword()));
		userRepo.save(newParent);

		// invoke service for Entity translation to DTO
		return userService.createdParentDTOtranslation(newParent);
	}

	/***************************************************************************************
	 * POST endpoint for administrator looking to create new teacher
	 * -- postman code adm004 --
	 * 
	 * @param teacher
	 * @return if ok, new teacher
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newTeacher")
	public ResponseEntity<?> postNewTeacher(@Valid @RequestBody CreateTeacherDTO teacher) {

		// invoke a service for DTO translation to Entity
		logger.info("**POST NEW TEACHER** Endpoint for posting a new teacher entered successfuly.");
		logger.info("**POST NEW TEACHER** Attempting to translate input DTO to Entity.");
		TeacherEntity newTeacher = userService.createTeacherDTOtranslation(teacher);
		logger.info("**POST NEW TEACHER** Translation successful.");

		// standard checks for new users, password match and username availability
		logger.info("**POST NEW TEACHER** Attempt to match password and repeated password.");
		if (!newTeacher.getPassword().equals(newTeacher.getRepeatedPassword())) {
			logger.warn("**POST NEW TEACHER** Passwords not matching.");
			return new ResponseEntity<RESTError>(
					new RESTError(1020, "Passwords not matching, please check your entry."), HttpStatus.BAD_REQUEST);
		}
		logger.info("**POST NEW TEACHER** Passwords matching.");

		logger.info("**POST NEW TEACHER** Attempt to see if username exists in db.");
		if (userRepo.findByUsername(newTeacher.getUsername()).isPresent()) {
			logger.warn("**POST NEW TEACHER** Existing username.");
			return new ResponseEntity<RESTError>(new RESTError(1021, "Username already in database."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**POST NEW TEACHER** Username available.");

		logger.info("**POST NEW TEACHER** Attempt to see if unique ID number exists in db.");
		if (userRepo.findByJmbg(newTeacher.getJmbg()).isPresent()) {
			logger.warn("**POST NEW TEACHER** Existing unique ID number.");
			return new ResponseEntity<RESTError>(
					new RESTError(1022, "Teacher with an existing unique ID number in database."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**POST NEW TEACHER** Existing unique ID number not in database.");

		logger.info("**POST NEW TEACHER** Attempt to see if username exists as deleted in db.");
		if (userRepo.findByDeletedAndUsername(1, newTeacher.getUsername()).isPresent()) {
			logger.warn("**POST NEW TEACHER** Deleted username confirmed.");
			return new ResponseEntity<RESTError>(new RESTError(1091,
					"Teacher with this username previously deleted, please reinstate old teacher or use a different username."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**POST NEW TEACHER** Username not previously used and deleted.");

		//allow for teacher being a parent in the database, no unique ID check

		// encript password and save user
		logger.info("**POST NEW TEACHER** Attempt to encode user password.");
		newTeacher.setPassword(Encryption.getPasswordEncoded(newTeacher.getPassword()));
		userRepo.save(newTeacher);
		logger.info("**POST NEW TEACHER** Encoding complete and teacher saved to db.");

		// invoke service for Entity translation to DTO
		logger.info("**POST NEW TEACHER** Attempting to translate Entity to output DTO.");
		return userService.createdTeacherDTOtranslation(newTeacher);
	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to update general user info
	 * -- postman code adm010 --
	 * 
	 * @param user
	 * @param username
	 * @return if ok update user
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/changeUsers/{username}")
	public ResponseEntity<?> changeUserGeneral(@Valid @RequestBody UpdateUserDTO updatedUser,
			@PathVariable String username) {

		// initial check for existance in db
		Optional<UserEntity> ogUser = userRepo.findByUsername(username);
		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1030, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(new RESTError(1031, "User previously deleted and not accesible."),
					HttpStatus.BAD_REQUEST);
		}

		// chaeck if username taken, see if same as old to avoid RESTError for taken username
		if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(ogUser.get().getUsername())
				&& userRepo.findByUsername(updatedUser.getUsername()).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(1032, "Username taken, please choose another."),
					HttpStatus.BAD_REQUEST);
		}

		// invoke a service for DTO translation to Entity
		userRepo.save(userService.updateUserDTOtranslation(updatedUser, ogUser.get()));

		// invoke service for Entity translation to DTO
		return userService.updatedUserDTOtranslation(ogUser.get());
	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to change user's role.
	 * -- postman code adm011 --
	 * 
	 * @param role
	 * @param username
	 * @return if ok update user's role
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/changeRole")
	public ResponseEntity<?> changeUserRole(@RequestParam String role, @RequestParam String username) {

		// initial check for existance in db
		Optional<UserEntity> ogUser = userRepo.findByUsername(username);
		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1030, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(new RESTError(1031, "User previously deleted and not accesible."),
					HttpStatus.BAD_REQUEST);
		}

		// check for role in db
		if (!userService.isRoleInEnum(role)) {
			return new ResponseEntity<RESTError>(
					new RESTError(1040, "Role not in the system, contact the Ministry of Education."),
					HttpStatus.BAD_REQUEST);
		}

		// set new role and save user
		ogUser.get().setRole(roleRepo.findByName(ERole.valueOf(role)).get());
		userRepo.save(ogUser.get());

		// fill DTO
		UpdatedRoleDTO updatedUserRoleDTO = new UpdatedRoleDTO();
		updatedUserRoleDTO.setRole(role);
		updatedUserRoleDTO.setUsername(username);

		return new ResponseEntity<UpdatedRoleDTO>((updatedUserRoleDTO), HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to change user's password.
	 * -- postman code adm012 --
	 * 
	 * @param passwordsUpdate
	 * @param username
	 * @return if ok update user's pasword
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/changePassword/{username}")
	public ResponseEntity<?> changePassword(@Valid @RequestBody UpdatePasswordDTO passwordsUpdate,
			@PathVariable String username) {

		// initial check for existance in db
		Optional<UserEntity> ogUser = userRepo.findByUsername(username);
		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1030, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(new RESTError(1031, "User previously deleted and not accesible."),
					HttpStatus.BAD_REQUEST);
		}

		// check if old password matches the passwrod stored in db
		if (!Encryption.validatePassword(passwordsUpdate.getOldPassword(), ogUser.get().getPassword())) {
			return new ResponseEntity<RESTError>(new RESTError(1050, "Old password not correct, please try again."),
					HttpStatus.BAD_REQUEST);
		}

		// check if new and repeated passwords match
		if (!passwordsUpdate.getNewPassword().equals(passwordsUpdate.getRepeatedPassword())) {
			return new ResponseEntity<RESTError>(
					new RESTError(1051, "New password and repeated password don't match. Check your inputs."),
					HttpStatus.BAD_REQUEST);
		}

		// set new encode password
		ogUser.get().setPassword(userService.encodePassword(passwordsUpdate.getNewPassword()));
		userRepo.save(ogUser.get());

		return new ResponseEntity<String>("Password changed successfully for user " + username + ".", HttpStatus.OK);

	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to change teacher's specific role
	 * -- postman code adm013 --
	 * 
	 * @param role
	 * @param username
	 * @return if ok update teacher's role
	 **************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/changeTeacherRole")
	public ResponseEntity<?> changeTeacherRole(@RequestParam String username, @RequestParam String role,
			@RequestParam Double bonus) {

		// initial check for existance in db
		Optional<UserEntity> ogUser = userRepo.findByUsername(username);
		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1030, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is a teacher
		if (!ogUser.get().getRole().getName().equals(ERole.ROLE_ADMIN)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HEADMASTER)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HOMEROOM)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_TEACHER)) {
			return new ResponseEntity<RESTError>(new RESTError(1060, "User is not a teacher."), HttpStatus.BAD_REQUEST);
		}

		// check if new role is valid
		if (!role.equals(ERole.ROLE_ADMIN.toString()) && !role.equals(ERole.ROLE_HEADMASTER.toString())
				&& !role.equals(ERole.ROLE_HOMEROOM.toString()) && !role.equals(ERole.ROLE_TEACHER.toString())) {
			return new ResponseEntity<RESTError>(
					new RESTError(1061, "You must choose one of teacher roles available in ERole."),
					HttpStatus.BAD_REQUEST);
		}

		// check if user is deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(new RESTError(31, "User previously deleted and not accesible."),
					HttpStatus.BAD_REQUEST);
		}

		// invoke service for salary bonus logic
		String oldRole = ogUser.get().getRole().getName().toString();
		userService.updateTeacherRole((TeacherEntity) ogUser.get(), role, bonus);
		return new ResponseEntity<String>("Teacher " + username + " has undergone a role change, used to be " + oldRole
				+ ", and now is " + ogUser.get().getRole().getName().toString() + ".", HttpStatus.OK);

	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to change teacher's salary
	 * -- postman code adm014 --
	 * 
	 * @param salary
	 * @param username
	 * @return if ok update teacher's salary
	 **************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/changeTeacherSalary/{username}/{salary}")
	public ResponseEntity<?> changeTeacherSalary(@PathVariable String username, @PathVariable Double salary) {

		// initial check for existance in db
		Optional<UserEntity> ogUser = userRepo.findByUsername(username);
		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1030, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(new RESTError(1031, "User previously deleted and not accesible."),
					HttpStatus.BAD_REQUEST);
		}

		// check if user is a teacher
		if (!ogUser.get().getRole().getName().equals(ERole.ROLE_ADMIN)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HEADMASTER)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HOMEROOM)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_TEACHER)) {
			return new ResponseEntity<RESTError>(new RESTError(1060, "User is not a teacher."), HttpStatus.BAD_REQUEST);
		}

		TeacherEntity ogTeacher = (TeacherEntity) ogUser.get();
		Double oldSalary = ogTeacher.getSalary();
		ogTeacher.setSalary(salary);
		userRepo.save(ogTeacher);

		return new ResponseEntity<String>("Teacher " + username + " has undergone a salary change, used to be "
				+ oldSalary + ", and now is " + ogTeacher.getSalary() + ".", HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to change parent's phone number
	 * -- postman code adm015 --
	 * 
	 * @param phoneNumber DTO
	 * @param username
	 * @return if ok update parent's phone number
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/changeParentsPhoneNumber/{username}")
	public ResponseEntity<?> changeParentsPhonenUmber(@PathVariable String username,
			@Valid @RequestBody UpdatePhoneNumberDTO phoneNumber) {

		// initial check for active parent existance in db
		Optional<UserEntity> ogUser = userRepo.findByDeletedAndRoleAndUsername(0,
				roleRepo.findByName(ERole.ROLE_PARENT).get(), username);
		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1090, "Parent not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		ParentEntity ogParent = (ParentEntity) ogUser.get();
		String oldPhoneNumber = ogParent.getPhoneNumber();
		ogParent.setPhoneNumber(phoneNumber.getPhoneNumber());
		userRepo.save(ogParent);

		return new ResponseEntity<String>("Parent " + username + " has undergone a phone number change, used to be "
				+ oldPhoneNumber + ", and now is " + ogParent.getPhoneNumber() + ".", HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to assign children to parents
	 * -- postman code adm016 --
	 * 
	 * @param username child
	 * @param username parent
	 * @return if ok update parent's children list
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/assignChildToParent")
	public ResponseEntity<?> assignChildToParent(@RequestParam String usernameParent,
			@RequestParam String usernameChild) {
		logger.info("**ASSIGN STUDENT TO PARENT** Access to the endpoint for assigning student to parent successful.");
		// initial check for active parent existance in db
		logger.info("**ASSIGN STUDENT TO PARENT** Atempt to find the parent in database.");
		Optional<UserEntity> ogParent = userRepo.findByDeletedAndRoleAndUsername(0,
				roleRepo.findByName(ERole.ROLE_PARENT).get(), usernameParent);
		if (ogParent.isEmpty()) {
			logger.warn("**ASSIGN STUDENT TO PARENT** Atempt failed.");
			return new ResponseEntity<RESTError>(
					new RESTError(1090, "Parent not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**ASSIGN STUDENT TO PARENT** Parent found.");
		// initial check for active student existance in db
		logger.info("**ASSIGN STUDENT TO PARENT** Atempt to find the student in database.");
		Optional<UserEntity> ogStudent = userRepo.findByDeletedAndRoleAndUsername(0,
				roleRepo.findByName(ERole.ROLE_STUDENT).get(), usernameChild);
		if (ogStudent.isEmpty()) {
			logger.warn("**ASSIGN STUDENT TO PARENT** Atempt failed.");
			return new ResponseEntity<RESTError>(
					new RESTError(1100, "Student not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**ASSIGN STUDENT TO PARENT** Student found.");

		logger.info("**ASSIGN STUDENT TO PARENT** Casting user entities to parent and student.");
		StudentEntity updatedStudent = (StudentEntity) ogStudent.get();
		ParentEntity updatedParent = (ParentEntity) ogParent.get();

		// check for number of parents
		logger.info("**ASSIGN STUDENT TO PARENT** Atempting to find number of parents related to student.");
		if (updatedStudent.getParents().size() >= 2) {
			logger.warn("**ASSIGN STUDENT TO PARENT** Parents number more than 3.");
			return new ResponseEntity<RESTError>(new RESTError(1101, "No more than 2 parent allowed per student."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**ASSIGN STUDENT TO PARENT** Number of parents is " + updatedStudent.getParents().size() + ".");

		logger.info("**ASSIGN STUDENT TO PARENT** Creating a new student-parent database entry.");
		StudentParentEntity studentParent = new StudentParentEntity();
		studentParent.setParent(updatedParent);
		studentParent.setStudent(updatedStudent);
		studentParent.setDeleted(0);
		logger.info("**ASSIGN STUDENT TO PARENT** Database entry created.");

		// check if child already assigned
		logger.info("**ASSIGN STUDENT TO PARENT** Atempting to see if student is already associated with the parent.");
		if (studentParentRepo.findByStudentAndParentAndDeleted(studentParent.getStudent(), studentParent.getParent(), 0)
				.isPresent()) {
			logger.warn("**ASSIGN STUDENT TO PARENT** Relationship already exsisting.");
			return new ResponseEntity<RESTError>(new RESTError(1112, "Student already assigned to a parent."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**ASSIGN STUDENT TO PARENT** Student not associated with the parent.");

		// add Student to list of children, update parent and save to db

		logger.info("**ASSIGN STUDENT TO PARENT** Atempting to save to studentParent Repository.");
		studentParentRepo.save(studentParent);
		logger.info("**ASSIGN STUDENT TO PARENT** Entry saved.Exiting endpoint.");

		return new ResponseEntity<String>("Student " + usernameChild + " assigned to parent " + usernameParent + ".",
				HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to soft delete a user.
	 * -- postman code adm030 --
	 * 
	 * @param username
	 * @return if ok set user to deleted
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/deleteUser/{username}")
	public ResponseEntity<?> deleteUser(@PathVariable String username) {

		logger.info("**DELETE USER** Access to the endpoint successful.");

		logger.info("**DELETE USER** Attempt to find an active user in database.");
		// initial check for existance in db
		Optional<UserEntity> ogUser = userRepo.findByUsername(username);
		if (ogUser.isEmpty() || ogUser.get().getDeleted() == 1) {
			logger.warn("**DELETE USER** User not in database or deleted.");
			return new ResponseEntity<RESTError>(
					new RESTError(1030,
							"Username not found in database or user is deleted, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**DELETE USER** Attempt successful.");

		logger.info(
				"**DELETE USER** Attempt to find if there are active assignments linked to this teacher-subject combination.");

		logger.info(
				"**DELETE USER** Attempt to find is user is a student, if so delete link with student group and delete relationship with parent.");
		// see if user is a student and belongs to a student group, also delete from student parent
		if (ogUser.get() instanceof StudentEntity) {
			logger.info("**DELETE USER** User is a student.");
			StudentEntity ogStudent = (StudentEntity) ogUser.get();
			ogStudent.setBelongsToStudentGroup(null);
			List<StudentParentEntity> ogStudentParent = studentParentRepo.findByStudent(ogStudent);
			for (StudentParentEntity studentParentEntity : ogStudentParent) {
				studentParentEntity.setDeleted(1);
			}
			studentParentRepo.saveAll(ogStudentParent);
			logger.info("**DELETE USER** Attempt successful, unlinking complete.");
		}

		logger.info("**DELETE USER** Attempt to find is user is a parent, if so delete relationship with student.");
		// see if user is a parent, delete student parent
		if (ogUser.get() instanceof ParentEntity) {
			logger.info("**DELETE USER** User is a parent.");
			ParentEntity ogParent = (ParentEntity) ogUser.get();
			List<StudentParentEntity> ogStudentParent = studentParentRepo.findByParent(ogParent);
			for (StudentParentEntity studentParentEntity : ogStudentParent) {
				studentParentEntity.setDeleted(1);
			}
			studentParentRepo.saveAll(ogStudentParent);
			logger.info("**DELETE USER** Attempt successful, relationships deleted.");
		}

		logger.info(
				"**DELETE USER** Attempt to find is user is a teacher, if so delete relationship with teacher-subject combination.");
		// see if user is a teacher, delete teacher subject
		if (ogUser.get() instanceof TeacherEntity) {
			logger.info("**DELETE USER** User is a teacher.");
			TeacherEntity ogTeacher = (TeacherEntity) ogUser.get();
			List<TeacherSubjectEntity> ogTeacherSubject = teacherSubjectRepo.findAllByTeacher(ogTeacher);
			for (TeacherSubjectEntity teacherSubjectEntity : ogTeacherSubject) {
				teacherSubjectEntity.setDeleted(1);
			}
			teacherSubjectRepo.saveAll(ogTeacherSubject);
			logger.info("**DELETE USER** Attempt successful, relationships deleted.");
		}

		logger.info("**DELETE USER** Attempt on editing deleted field and saving to db.");
		// set to deleted and save
		ogUser.get().setDeleted(1);
		userRepo.save(ogUser.get());
		logger.info("**DELETE USER** Attempt successful, using service to get pretty output.");
		return userService.deletedUserDTOtranslation(ogUser.get());
	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to restore a deleted user.
	 * -- postman code adm031 --
	 * 
	 * @param username
	 * @return if ok set user to restored
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/restoreUser/{username}")
	public ResponseEntity<?> restoreUser(@PathVariable String username) {

		logger.info("**RESTORE USER** Access to the endpoint successful.");

		logger.info("**RESTORE USER** Attempt to find a deleted user in database.");
		// initial check for existance in db
		Optional<UserEntity> ogUser = userRepo.findByUsername(username);
		if (ogUser.isEmpty() || ogUser.get().getDeleted() == 0) {
			logger.warn("**RESTORE USER** User not in database or active.");
			return new ResponseEntity<RESTError>(
					new RESTError(1030,
							"Username not found in database or is active, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**RESTORE USER** Attempt successful.");

		// set to active and save
		logger.info("**RESTORE USER** Attempt on editing deleted field and saving to db.");
		ogUser.get().setDeleted(0);
		userRepo.save(ogUser.get());
		logger.info("**DELETE USER** Attempt successful, using service to get pretty output.");
		return userService.deletedUserDTOtranslation(ogUser.get());
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch all active users.
	 * postman code adm040
	 * 
	 * @return list of active users
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/activeUsers")
	public ResponseEntity<?> findAllActiveUsers() {

		// translate to DTO useng a service
		List<GetUserDTO> activeUsersDTO = new ArrayList<>();
		List<UserEntity> activeUsers = (List<UserEntity>) userRepo.findAllByDeleted(0);
		for (UserEntity userEntity : activeUsers) {
			activeUsersDTO.add(userService.foundUserDTOtranslation(userEntity));
		}

		// check if list is empty
		if (activeUsersDTO.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(1090, "No active users in database."),
					HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<List<GetUserDTO>>(activeUsersDTO, HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch all deleted users.
	 * postman code adm041
	 * 
	 * @return list of deleted users
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/deletedUsers")
	public ResponseEntity<?> findAllDeletedUsers() {

		// translate to DTO useng a service
		List<UserEntity> deletedUsers = (List<UserEntity>) userRepo.findAllByDeleted(1);
		List<GetUserDTO> deletedUsersDTO = new ArrayList<>();
		for (UserEntity userEntity : deletedUsers) {
			deletedUsersDTO.add(userService.foundUserDTOtranslation(userEntity));
		}

		// check if list is empty
		if (deletedUsersDTO.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(1090, "No deleted users in database."),
					HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<List<GetUserDTO>>(deletedUsersDTO, HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch all users with specific role.
	 * postman code adm042
	 * 
	 * @return list of active users with role
	 **************************************************************************************/
	@RequestMapping(method = RequestMethod.GET, path = "/admin/activeUsers/{role}")
	public ResponseEntity<?> findAllActiveUsersByRole(@PathVariable String role) {

		// role check
		if (!userService.isRoleInEnum(role)) {
			return new ResponseEntity<RESTError>(new RESTError(1000, "Role name not allowed, check ERole for details."),
					HttpStatus.BAD_REQUEST);
		}

		// translate to DTO useng a service
		List<GetUserDTO> activeUsersDTO = new ArrayList<>();
		List<UserEntity> activeUsersWithRole = (List<UserEntity>) userRepo.findAllByDeletedAndRole(0,
				roleRepo.findByName(ERole.valueOf(role)).get());
		for (UserEntity userEntity : activeUsersWithRole) {
			activeUsersDTO.add(userService.foundUserDTOtranslation(userEntity));
		}

		// check if list is empty
		if (activeUsersDTO.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(1090, "No active users in database."),
					HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<List<GetUserDTO>>(activeUsersDTO, HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch a specific active user.
	 * postman code adm043
	 * 
	 * @return specific user
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/user/{username}")
	public ResponseEntity<?> findActiveUser(@PathVariable String username) {

		Optional<UserEntity> activeUser = userRepo.findByDeletedAndUsername(0, username);
		if (activeUser.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(1090, "No active user in database."),
					HttpStatus.NOT_FOUND);
		}

		// translate to DTO useng a service
		GetUserDTO activeUserDTO = userService.foundUserDTOtranslation(activeUser.get());

		return new ResponseEntity<GetUserDTO>(activeUserDTO, HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch children of specific active parent.
	 * postman code adm044
	 * 
	 * @return children list
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/students/{usernameParent}")
	public ResponseEntity<?> findActiveStudentsFromParent(@PathVariable String usernameParent) {

		logger.info("**GET CHILDREN** Access to the endpoint successful.");

		logger.info("**GET CHILDREN** Attempt to find active parent in database.");
		// initial check for active parent existance in db
		Optional<UserEntity> ogParent = userRepo.findByDeletedAndRoleAndUsername(0,
				roleRepo.findByName(ERole.ROLE_PARENT).get(), usernameParent);
		if (ogParent.isEmpty()) {
			logger.warn("**GET CHILDREN** Not an active parent.");
			return new ResponseEntity<RESTError>(
					new RESTError(1100, "Parent not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET CHILDREN** Active parent found.");

		logger.info("**GET CHILDREN** Attempt to make a list of children belonging to a parent.");
		// prepare a list for output and get children list from parent, check if list is empty
		List<GetChildrenDTO> activeChildrenDTOs = new ArrayList<>();
		ParentEntity ogParentCast = (ParentEntity) ogParent.get();

		List<StudentParentEntity> children = studentParentRepo.findByParent(ogParentCast);

		if (children.isEmpty()) {
			logger.warn(
					"**GET CHILDREN** Parent without childre. This should not happen, schedule for user maintenance.");
			return new ResponseEntity<RESTError>(
					new RESTError(1110, "No students assigned to this user. Schedule for db maintenance."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET CHILDREN** List prepared, attempting to access service for translation to DTO.");

		// translate to DTO using a service
		for (StudentParentEntity studentEntity : children) {
			activeChildrenDTOs.add(userService.foundChildrenDTOtranslation(studentEntity));
		}
		logger.info("**GET CHILDREN** All done, outputing a list of children.");

		return new ResponseEntity<List<GetChildrenDTO>>(activeChildrenDTOs, HttpStatus.OK);

	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch parents of specific active student.
	 * postman code adm045
	 * 
	 * @return parents list
	 **************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HOMEROOM" })
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/parents/{usernameStudent}")
	public ResponseEntity<?> findActiveParentsFromStudent(@PathVariable String usernameStudent) {

		logger.info("**GET PARENTS** Access to the endpoint successful.");

		logger.info("**GET PARENTS** Attempt to find active parent in database.");
		// initial check for active student existance in db
		Optional<UserEntity> ogStudent = userRepo.findByDeletedAndRoleAndUsername(0,
				roleRepo.findByName(ERole.ROLE_STUDENT).get(), usernameStudent);
		if (ogStudent.isEmpty()) {
			logger.warn("**GET PARENTS** Not an active parent.");
			return new ResponseEntity<RESTError>(
					new RESTError(1120, "Student not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET PARENTS** Active parent found.");

		StudentEntity ogStudentCast = (StudentEntity) ogStudent.get();

		logger.info(
				"**GET PARENTS** Attempt to check roles and allow only homeroom teacher responsible for student or admin to access.");
		if (!ogStudentCast.getBelongsToStudentGroup().getHomeroomTeacher().getUsername().equals(userService.whoAmI())
				&& !userService.amIAdmin()) {
			logger.warn("**GET PARENTS** Role not adequate or homeroom teacher not assigned to student.");
		}

		logger.info("**GET PARENTS** Role adequate.");

		logger.info("**GET PARENTS** Attempt to make a list of children belonging to a parent.");
		// prepare a list for output and get parents list from children, check if list is empty
		List<GetParentsDTO> activeParentsDTOs = new ArrayList<>();
		List<StudentParentEntity> parents = studentParentRepo.findByStudent(ogStudentCast);

		if (parents.isEmpty()) {
			logger.warn(
					"**GET PARENTS** Parent without childre. This should not happen, schedule for user maintenance.");
			return new ResponseEntity<RESTError>(
					new RESTError(1130, "No parents assigned to this user. Schedule for db maintenance."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET PARENTS** List prepared, attempting to access service for translation to DTO.");

		// translate to DTO using a service
		for (StudentParentEntity parentEntity : parents) {
			activeParentsDTOs.add(userService.foundParentsDTOtranslation(parentEntity));
		}
		logger.info("**GET PARENTS** All done, outputing a list of children.");

		return new ResponseEntity<List<GetParentsDTO>>(activeParentsDTOs, HttpStatus.OK);

	}

	/***************************************************************************************
	 * GET to find logged user's username
	 * -- postman code whoAmI --
	 * 	
	 * @return username
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/whoAmI")
	public ResponseEntity<?> loggedUser() {

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username;

		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else {
			username = principal.toString();
		}

		return new ResponseEntity<String>("User " + username + " is logged in the system.", HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET to download logfile
	 * -- postman code getLogs --
	 * 	
	 * @return username
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/downloadLogs")
	public ResponseEntity<Resource> downloadLogs(@RequestParam String fileName, HttpServletRequest request) {

		// Load file as Resource
		Resource resource = downloadService.loadFileAsResource(fileName);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.info("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}
}

//TODO 

/**
 * 
 * ****ADMIN PANEL****
 * 
 * 1. add new student -- ok
 * 2. add new teacher/homeroom/admin/principal -- ok
 * 3. add new parent -- ok
 * 4. delete user -- ok
 * 5. change user role -- ok
 * 6. update user -- ok
 * 7. update teacher fetures -- ok
 * 8. update student features -- ok
 * 9. update parent fetures -- ok
 * 10. assign subjects to teachers
 * 11. get info for all entites
 *  # provide admin role to all endpoints throughout, asign specific roles if needed
 * 
 * 
 * ****PRINCIPAL PANEL****
 * 
 * 1. update teacher/homeroom/admin salary -- ok
 * 2. add new student group
 * 3. assign students and homeroom to student group
 * 4. remove students and homeroom from student group
 * 5. assign teachers to student groups
 * 6. remove teachers from student groups
 * 7. add overriden grades
 * 
 * 
 * ****HOMEROOM PANEL****
 *  
 * 1. get results for student group
 * 2. get parents info (for student group assigned)
 *  
 *  
 * ****TEACHER PANEL****
 * 
 * 1. add new assignment
 * 2. update assignments
 * 3. delete assignmets - not graded!
 * 4. assign grades to assignments
 * 5. get assignments for children taking the class
 * 6  get student grades for children taking the class
 * 
 * 
 * ****PARENT PANEL****
 * 
 * 1. get student grades - for related children
 * 2. get grades by subject - for related children
 * 3. get email updates when grade is assigned
 * 
 * 
 * ****STUDENT PANEL****
 * 
 * 1. get my grades
 * 2. get my grades by subject
 * 	 
 * */
