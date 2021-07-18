package Brains2021.electronic.gradeBook.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

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

import Brains2021.electronic.gradeBook.dtos.in.CreateParentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateStudentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherDTO;
import Brains2021.electronic.gradeBook.dtos.in.UpdatePasswordDTO;
import Brains2021.electronic.gradeBook.dtos.in.UpdateUserDTO;
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
			return new ResponseEntity<RESTError>(new RESTError(0, "Role name not allowed, check ERole for details."),
					HttpStatus.BAD_REQUEST);
		}

		if (roleRepo.findByName(ERole.valueOf(role)).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(1, "Role already in the database."),
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
			return new ResponseEntity<RESTError>(new RESTError(20, "Passwords not matching, please check your entry."),
					HttpStatus.BAD_REQUEST);
		}
		if (userRepo.findByUsername(newStudent.getUsername()).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(21, "Username already in database."),
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
			return new ResponseEntity<RESTError>(new RESTError(20, "Passwords not matching, please check your entry."),
					HttpStatus.BAD_REQUEST);
		}
		if (userRepo.findByUsername(newParent.getUsername()).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(21, "Username already in database."),
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
			return new ResponseEntity<RESTError>(new RESTError(20, "Passwords not matching, please check your entry."),
					HttpStatus.BAD_REQUEST);
		}
		if (userRepo.findByUsername(newTeacher.getUsername()).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(21, "Username already in database."),
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
					new RESTError(30, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(new RESTError(31, "User previously deleted and not accesible."),
					HttpStatus.BAD_REQUEST);
		}

		// chaeck if username taken, see if same as old to avoid RESTError for taken username
		if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(ogUser.get().getUsername())
				&& userRepo.findByUsername(updatedUser.getUsername()).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(32, "Username taken, please choose another."),
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
					new RESTError(30, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(new RESTError(31, "User previously deleted and not accesible."),
					HttpStatus.BAD_REQUEST);
		}

		// check for role in db
		if (!userService.isRoleInEnum(role)) {
			return new ResponseEntity<RESTError>(
					new RESTError(40, "Role not in the system, contact the Ministry of Education."),
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
					new RESTError(30, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(new RESTError(31, "User previously deleted and not accesible."),
					HttpStatus.BAD_REQUEST);
		}

		// check if old password matches the passwrod stored in db
		if (!Encryption.validatePassword(passwordsUpdate.getOldPassword(), ogUser.get().getPassword())) {
			return new ResponseEntity<RESTError>(new RESTError(50, "Old password not correct, please try again."),
					HttpStatus.BAD_REQUEST);
		}

		// check if new and repeated passwords match
		if (!passwordsUpdate.getNewPassword().equals(passwordsUpdate.getRepeatedPassword())) {
			return new ResponseEntity<RESTError>(
					new RESTError(51, "New password and repeated password don't match. Check your inputs."),
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
					new RESTError(30, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is a teacher
		if (!ogUser.get().getRole().getName().equals(ERole.ROLE_ADMIN)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HEADMASTER)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HOMEROOM)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_TEACHER)) {
			return new ResponseEntity<RESTError>(new RESTError(60, "User is not a teacher."), HttpStatus.BAD_REQUEST);
		}

		// check if new role is valid
		if (!role.equals(ERole.ROLE_ADMIN.toString()) && !role.equals(ERole.ROLE_HEADMASTER.toString())
				&& !role.equals(ERole.ROLE_HOMEROOM.toString()) && !role.equals(ERole.ROLE_TEACHER.toString())) {
			return new ResponseEntity<RESTError>(
					new RESTError(61, "You must choose one of teacher roles available in ERole."),
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
					new RESTError(30, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(new RESTError(31, "User previously deleted and not accesible."),
					HttpStatus.BAD_REQUEST);
		}

		// check if user is a teacher
		if (!ogUser.get().getRole().getName().equals(ERole.ROLE_ADMIN)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HEADMASTER)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HOMEROOM)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_TEACHER)) {
			return new ResponseEntity<RESTError>(new RESTError(60, "User is not a teacher."), HttpStatus.BAD_REQUEST);
		}

		TeacherEntity ogTeacher = (TeacherEntity) ogUser.get();
		Double oldSalary = ogTeacher.getSalary();
		ogTeacher.setSalary(salary);
		userRepo.save(ogTeacher);

		return new ResponseEntity<String>("Teacher " + username + " has undergone a salary change, used to be "
				+ oldSalary + ", and now is " + ogTeacher.getSalary() + ".", HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to soft delete a user.
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
					new RESTError(30, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user existing, but already deleted
		if (ogUser.get().getDeleted().equals(true)) {
			return new ResponseEntity<RESTError>(
					new RESTError(70,
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
					new RESTError(30, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user existing, and set to deleted
		if (ogUser.get().getDeleted().equals(false)) {
			return new ResponseEntity<RESTError>(
					new RESTError(80, "User is active. Use different endpoint to delete the user."),
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

		List<GetUserDTO> activeUsersDTO = new ArrayList<>();
		List<UserEntity> activeUSers = (List<UserEntity>) userRepo.findAllByDeletedFalse();
		for (UserEntity userEntity : activeUSers) {
			activeUsersDTO.add(userService.foundUserDTOtranslation(userEntity));
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

		List<UserEntity> deletedUsers = (List<UserEntity>) userRepo.findAllByDeletedTrue();
		List<GetUserDTO> deletedUsersDTO = new ArrayList<>();
		for (UserEntity userEntity : deletedUsers) {
			deletedUsersDTO.add(userService.foundUserDTOtranslation(userEntity));
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
			return new ResponseEntity<RESTError>(new RESTError(0, "Role name not allowed, check ERole for details."),
					HttpStatus.BAD_REQUEST);
		}

		List<GetUserDTO> activeUsersDTO = new ArrayList<>();
		List<UserEntity> activeUsersWithRole = (List<UserEntity>) userRepo
				.findAllByDeletedFalseAndRole(roleRepo.findByName(ERole.valueOf(role)).get());
		for (UserEntity userEntity : activeUsersWithRole) {
			activeUsersDTO.add(userService.foundUserDTOtranslation(userEntity));
		}

		return new ResponseEntity<List<GetUserDTO>>(activeUsersDTO, HttpStatus.OK);
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
	 * 9. update parent fetures TODO
	 * 10. assign subjects to teachers
	 * 11. get info for all entites
	 *  # provide admin role to all endpoints throughout
	 * 
	 * 
	 * ****PRINCIPAL PANEL****
	 * 
	 * 1. update teacher/homeroom/admin salary
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
	 * 
	 * 
	 * ****STUDENT PANEL****
	 * 
	 * 1. get my grades
	 * 2. get my grades by subject
	 * 	 
	 * */

}
