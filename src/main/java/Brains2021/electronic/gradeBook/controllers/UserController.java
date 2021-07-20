package Brains2021.electronic.gradeBook.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import Brains2021.electronic.gradeBook.entites.users.ParentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.RoleRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.security.Views;
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
		Optional<UserEntity> user = userRepo.findByUsername(username);
		if (user.isPresent() && Encryption.validatePassword(password, user.get().getPassword())) {
			// if found, try password
			String token = userService.createJWTToken(user.get());
			// if ok make token
			UserTokenDTO retVal = new UserTokenDTO(username, "Bearer " + token); //Bearer moze da se postavi ovde ili na frontendu
			return new ResponseEntity<UserTokenDTO>(retVal, HttpStatus.OK);
		}

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
					new RESTError(22, "Student with an existing unique ID number in database."),
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
		TeacherEntity newTeacher = userService.createTeacherDTOtranslation(teacher);

		// standard checks for new users, password match and username availability
		if (!newTeacher.getPassword().equals(newTeacher.getRepeatedPassword())) {
			return new ResponseEntity<RESTError>(
					new RESTError(1020, "Passwords not matching, please check your entry."), HttpStatus.BAD_REQUEST);
		}
		if (userRepo.findByUsername(newTeacher.getUsername()).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(1021, "Username already in database."),
					HttpStatus.BAD_REQUEST);
		}

		//allow for teacher being a parent in the database, no unique ID check

		// encript password and save user
		newTeacher.setPassword(Encryption.getPasswordEncoded(newTeacher.getPassword()));
		userRepo.save(newTeacher);

		// invoke service for Entity translation to DTO
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
		Optional<UserEntity> ogUser = userRepo
				.findByDeletedFalseAndRoleAndUsername(roleRepo.findByName(ERole.ROLE_PARENT).get(), username);
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
			@RequestParam String usernamChild) {

		// initial check for active parent existance in db
		Optional<UserEntity> ogParent = userRepo
				.findByDeletedFalseAndRoleAndUsername(roleRepo.findByName(ERole.ROLE_PARENT).get(), usernameParent);
		if (ogParent.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1090, "Parent not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// initial check for active student existance in db
		Optional<UserEntity> ogStudent = userRepo
				.findByDeletedFalseAndRoleAndUsername(roleRepo.findByName(ERole.ROLE_STUDENT).get(), usernamChild);
		if (ogStudent.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1100, "Student not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		StudentEntity updatedStudent = (StudentEntity) ogStudent.get();
		ParentEntity updatedParent = (ParentEntity) ogParent.get();

		// check for number of parents
		if (updatedStudent.getParents().size() >= 2) {
			return new ResponseEntity<RESTError>(new RESTError(1101, "No more than 2 parent allowed per student."),
					HttpStatus.BAD_REQUEST);
		}

		// check if child already assigned
		Set<StudentEntity> children = (Set<StudentEntity>) updatedParent.getChildren();
		if (children.contains(updatedStudent)) {
			return new ResponseEntity<RESTError>(new RESTError(1110, "Student already assigned to a parent."),
					HttpStatus.BAD_REQUEST);
		}

		// add Student to list of children, update parent and save to db
		children.add(updatedStudent);
		updatedParent.setChildren(children);
		userRepo.save(updatedParent);

		return new ResponseEntity<String>("Student " + usernamChild + " assigned to parent " + usernameParent + ".",
				HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to soft delete a user.
	 * on further updates think about chain deleting parents if all children get deleted
	 * -- postman code adm020 --
	 * 
	 * @param username
	 * @return if ok set user to deleted
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/deleteUser/{username}")
	public ResponseEntity<?> deleteUser(@PathVariable String username) {

		// initial check for existance in db
		Optional<UserEntity> ogUser = userRepo.findByUsername(username);
		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1030, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user existing, but already deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(
					new RESTError(1070,
							"User previously deleted and not accesible. Use different endpoint to restore the user."),
					HttpStatus.BAD_REQUEST);
		}
		// set to deleted and save
		ogUser.get().setDeleted(true);
		userRepo.save(ogUser.get());
		return userService.deletedUserDTOtranslation(ogUser.get());
	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to restore a deleted user.
	 * -- postman code adm021 --
	 * 
	 * @param username
	 * @return if ok set user to restored
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/restoreUser/{username}")
	public ResponseEntity<?> restoreUser(@PathVariable String username) {

		// initial check for existance in db
		Optional<UserEntity> ogUser = userRepo.findByUsername(username);
		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1030, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user existing, and set to deleted
		if (ogUser.get().getDeleted().equals(false)) {
			return new ResponseEntity<RESTError>(
					new RESTError(1080, "User is active. Use different endpoint to delete the user."),
					HttpStatus.BAD_REQUEST);
		}

		// restore user and save
		ogUser.get().setDeleted(false);
		userRepo.save(ogUser.get());
		return userService.deletedUserDTOtranslation(ogUser.get());
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch all active users.
	 * postman code adm030
	 * 
	 * @return list of active users
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/activeUsers")
	public ResponseEntity<?> findAllActiveUsers() {

		// translate to DTO useng a service
		List<GetUserDTO> activeUsersDTO = new ArrayList<>();
		List<UserEntity> activeUsers = (List<UserEntity>) userRepo.findAllByDeletedFalse();
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
	 * postman code adm031
	 * 
	 * @return list of deleted users
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/deletedUsers")
	public ResponseEntity<?> findAllDeletedUsers() {

		// translate to DTO useng a service
		List<UserEntity> deletedUsers = (List<UserEntity>) userRepo.findAllByDeletedTrue();
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
	 * postman code adm032
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
		List<UserEntity> activeUsersWithRole = (List<UserEntity>) userRepo
				.findAllByDeletedFalseAndRole(roleRepo.findByName(ERole.valueOf(role)).get());
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
	 * postman code adm033
	 * 
	 * @return specifoc user
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/user/{username}")
	public ResponseEntity<?> findActiveUser(@PathVariable String username) {

		Optional<UserEntity> activeUser = userRepo.findByDeletedFalseAndUsername(username);
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
	 * postman code adm034
	 * 
	 * @return children list
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/students/{usernameParent}")
	public ResponseEntity<?> findActiveStudentsFromParent(@PathVariable String usernameParent) {

		// initial check for active parent existance in db
		Optional<UserEntity> ogParent = userRepo
				.findByDeletedFalseAndRoleAndUsername(roleRepo.findByName(ERole.ROLE_PARENT).get(), usernameParent);
		if (ogParent.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1100, "Parent not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// prepare a list for output and get children list from parent, check if list is empty
		List<GetChildrenDTO> activeChildrenDTOs = new ArrayList<>();
		ParentEntity ogParentCast = (ParentEntity) ogParent.get();
		Set<StudentEntity> children = (Set<StudentEntity>) ogParentCast.getChildren();

		if (children.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1110, "No students assigned to this user. Schedule for db maintenance."),
					HttpStatus.NOT_FOUND);
		}

		// translate to DTO using a service
		for (StudentEntity studentEntity : children) {
			activeChildrenDTOs.add(userService.foundChildrenDTOtranslation(studentEntity));
		}

		return new ResponseEntity<List<GetChildrenDTO>>(activeChildrenDTOs, HttpStatus.OK);

	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch parents of specific active student.
	 * postman code adm035
	 * 
	 * @return parents list
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/admin/parents/{usernameStudent}")
	public ResponseEntity<?> findActiveParentsFromStudent(@PathVariable String usernameStudent) {

		// initial check for active student existance in db
		Optional<UserEntity> ogStudent = userRepo
				.findByDeletedFalseAndRoleAndUsername(roleRepo.findByName(ERole.ROLE_STUDENT).get(), usernameStudent);
		if (ogStudent.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1120, "Student not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// prepare a list for output and get parents list from children, check if list is empty
		List<GetParentsDTO> activeParentsDTOs = new ArrayList<>();
		StudentEntity ogStudentCast = (StudentEntity) ogStudent.get();
		Set<ParentEntity> parents = (Set<ParentEntity>) ogStudentCast.getParents();

		if (parents.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1130, "No parents assigned to this user. Schedule for db maintenance."),
					HttpStatus.NOT_FOUND);
		}

		// translate to DTO using a service
		for (ParentEntity parentEntity : parents) {
			activeParentsDTOs.add(userService.foundParentsDTOtranslation(parentEntity));
		}

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
