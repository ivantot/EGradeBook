package Brains2021.electronic.gradeBook.controllers;

import java.util.List;
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

import Brains2021.electronic.gradeBook.dtos.in.CreateUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.UserTokenDTO;
import Brains2021.electronic.gradeBook.entites.RoleEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.RoleRepository;
import Brains2021.electronic.gradeBook.repositories.StudentRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.user.UserService;
import Brains2021.electronic.gradeBook.utils.Encryption;
import Brains2021.electronic.gradeBook.utils.RESTError;

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

	/**
	 * POST login endpoint accessible to all
	 * postman code
	 * 	
	 * @param username
	 * @param password
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/login") // putanja kao u configu
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

	/**
	 * utility POST endpoint for posting user roles to database, meant to be accessed by IT specialist
	 * postman code
	 * 
	 * @param roleName
	 * @return
	 */

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newRole")
	public ResponseEntity<?> postNewRole(@Valid @RequestBody RoleEntity role) {

		if (roleRepo.findByName(role.getName()).isEmpty()) {
			new ResponseEntity<RESTError>(
					new RESTError(0, "Not an acceptable name for a ROLE, use roles provided in ERole enummeration."),
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<RoleEntity>(roleRepo.save(role), HttpStatus.OK);
	}

	/**
	 * GET endpoint for fetching all users in the system
	 * postman code
	 * 
	 * @return
	 */

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "")
	public ResponseEntity<?> findAllUsers() {
		return new ResponseEntity<List<UserEntity>>((List<UserEntity>) userRepo.findAll(), HttpStatus.OK);
	}

	/**
	 * POST endpoint for creating all new users
	 * postman code
	 * 
	 * @param newUser
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newUser")
	public ResponseEntity<?> postNewUser(@Valid @RequestBody CreateUserDTO newUser) {

		return userService.translateFromDTO(newUser);
	}

	/**
	 * 
	 * 
	 * 
	 */
	@Secured({ "ROLE_ADMIN", "ROLE_STUDENT" })
	@RequestMapping(method = RequestMethod.POST, path = "/newStudent")
	public ResponseEntity<?> postNewStudent(@Valid @RequestBody CreateUserDTO newUser) {

		if (!newUser.getPassword().equals(newUser.getRepeatedPassword())) {
			new ResponseEntity<RESTError>(new RESTError(21, "Passwords not matching, please check your entry."), HttpStatus.BAD_REQUEST);
		}
		if (userRepo.findByUsername(newUser.getUsername()).isPresent()) {
			new ResponseEntity<RESTError>(new RESTError(22, "Username already in database."), HttpStatus.BAD_REQUEST);
		}
		if (userRepo.findByJmbg(newUser.getJmbg()).isPresent()) {
			new ResponseEntity<RESTError>(new RESTError(23, "Student with an existing JMBG in database."), HttpStatus.BAD_REQUEST);
		}

		StudentEntity newStudent = new StudentEntity();

		newStudent.setName(newUser.getName());
		newStudent.setSurname(newUser.getSurname());
		newStudent.setEmail(newUser.getEmail());
		newStudent.setUsername(newUser.getUsername());
		newStudent.setPassword(Encryption.getPasswordEncoded(newUser.getPassword()));
		newStudent.setJmbg(newUser.getJmbg());
		newStudent.setDateOfBirth(newUser.getDateOfBirth());
		newStudent.setRole(roleRepo.findByName(newUser.getRole()).get());
		newStudent.setDeleted(false);

		return new ResponseEntity<StudentEntity>(studentRepo.save(newStudent), HttpStatus.OK);
	}
}
