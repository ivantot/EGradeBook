package Brains2021.electronic.gradeBook.services.user;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.in.CreateParentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateStudentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherDTO;
import Brains2021.electronic.gradeBook.dtos.in.UpdateUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedParentDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedStudentDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedTeacherDTO;
import Brains2021.electronic.gradeBook.dtos.out.DeletedUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetChildrenDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetParentsDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.UpdatedUserDTO;
import Brains2021.electronic.gradeBook.entites.users.ParentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentParentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.RoleRepository;
import Brains2021.electronic.gradeBook.repositories.StudentParentRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.utils.Encryption;
import Brains2021.electronic.gradeBook.utils.enums.ERole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class UserServiceImp implements UserService {

	@Autowired
	UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private StudentParentRepository studentParentRepo;

	@Value("${spring.security.secret-key}")
	private String securityKey;

	@Value("${spring.securty.token-duration}")
	private Integer tokenDuration;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 * service for generating tokens, sets authorities and token duration and signs token using key from app.props
	 * 
	 */
	@Override
	public String createJWTToken(UserEntity user) {
		List<GrantedAuthority> grantedAuthority = AuthorityUtils
				.commaSeparatedStringToAuthorityList(user.getRole().getName().toString());
		String token = Jwts.builder().setId("softtekJWT").setSubject(user.getUsername())
				.claim("authorities",
						grantedAuthority.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + this.tokenDuration))
				.signWith(SignatureAlgorithm.HS512, this.securityKey).compact();
		return token;
	}

	/**
	 * 
	 * service for checkinf if user is with admin role
	 * 
	 */
	@Override
	public Boolean amIAdmin() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_ADMIN)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amITeacher() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_TEACHER)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amIStudent() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_STUDENT)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amIParent() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_PARENT)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amIHeadmaster() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_HEADMASTER)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amIHomeroom() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_HOMEROOM)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * check for relation between users 
	 * 
	 */
	@Override
	public Boolean areWeRelated(ParentEntity parent, StudentEntity student) {
		if (studentParentRepo.findByStudentAndParentAndDeleted(student, parent, 0).isPresent()) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * check if ERole enum contains the provided role 
	 * 
	 */
	@Override
	public Boolean isRoleInEnum(String role) {
		ERole[] allRoles = ERole.values();
		for (ERole eRole : allRoles) {
			//do comparison
			if (role.equals(eRole.toString())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String whoAmI() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username;

		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else {
			username = principal.toString();
		}
		return username;
	}

	/**
	 * 
	 * quickly encode a password
	 * 
	 */
	@Override
	public String encodePassword(String passwordToEncode) {
		return Encryption.getPasswordEncoded(passwordToEncode);
	}

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for creating Students
	 * 
	 */
	@Override
	public StudentEntity createStudentDTOtranslation(CreateStudentDTO student) {
		logger.info("##POST NEW STUDENT## Entered service for DTO translation to entity.");

		StudentEntity newStudent = new StudentEntity();
		logger.info("##POST NEW STUDENT## Translation started.");

		if (student.getName() != null) {
			newStudent.setName(student.getName());
		}
		if (student.getSurname() != null) {
			newStudent.setSurname(student.getSurname());
		}
		if (student.getEmail() != null) {
			newStudent.setEmail(student.getEmail());
		}
		if (student.getUsername() != null) {
			newStudent.setUsername(student.getUsername());
		}
		if (student.getPassword() != null) {
			newStudent.setPassword(student.getPassword());
		}
		if (student.getRepeatedPassword() != null) {
			newStudent.setRepeatedPassword(student.getRepeatedPassword());
		}
		if (student.getJmbg() != null) {
			newStudent.setJmbg(student.getJmbg());
		}
		if (student.getStudentUniqueNumber() != null) {
			newStudent.setStudentUniqueNumber(student.getStudentUniqueNumber());
		}
		if (student.getDateOfBirth() != null) {
			newStudent.setDateOfBirth(student.getDateOfBirth());
		}
		newStudent.setRole(roleRepo.findByName(ERole.ROLE_STUDENT).get());
		newStudent.setDeleted(0);

		logger.info("##POST NEW STUDENT## Translation complete, exiting service and returning to endpoint.");

		return newStudent;

	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for creating Students
	 * 
	 */
	@Override
	public ResponseEntity<?> createdStudentDTOtranslation(StudentEntity student) {

		logger.info("##POST NEW STUDENT## Entered service for Entity translation to DTO.");

		CreatedStudentDTO newStudentDTO = new CreatedStudentDTO();

		if (student.getName() != null) {
			newStudentDTO.setName(student.getName());
		}
		if (student.getSurname() != null) {
			newStudentDTO.setSurname(student.getSurname());
		}
		if (student.getDateOfBirth() != null) {
			newStudentDTO.setDateOfBirth(student.getDateOfBirth());
		}
		if (student.getEmail() != null) {
			newStudentDTO.setEmail(student.getEmail());
		}
		if (student.getJmbg() != null) {
			newStudentDTO.setJmbg(student.getJmbg());
		}
		if (student.getStudentUniqueNumber() != null) {
			newStudentDTO.setStudentUniqueNumber(student.getStudentUniqueNumber());
		}
		if (student.getUsername() != null) {
			newStudentDTO.setUsername(student.getUsername());
		}

		logger.info("##POST NEW STUDENT## Translation complete, exiting service and returning to endpoint.\n"
				+ newStudentDTO.toString());

		return new ResponseEntity<CreatedStudentDTO>(newStudentDTO, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for creating Parents
	 * 
	 */
	@Override
	public ParentEntity createParentDTOtranslation(CreateParentDTO parent) {
		logger.info("##POST NEW PARENT## Entered service for DTO translation to entity.");

		ParentEntity newParent = new ParentEntity();
		logger.info("##POST NEW PARENT## Translation started.");

		if (parent.getName() != null) {
			newParent.setName(parent.getName());
		}
		if (parent.getSurname() != null) {
			newParent.setSurname(parent.getSurname());
		}
		if (parent.getEmail() != null) {
			newParent.setEmail(parent.getEmail());
		}
		if (parent.getUsername() != null) {
			newParent.setUsername(parent.getUsername());
		}
		if (parent.getPassword() != null) {
			newParent.setPassword(parent.getPassword());
		}
		if (parent.getRepeatedPassword() != null) {
			newParent.setRepeatedPassword(parent.getRepeatedPassword());
		}
		if (parent.getJmbg() != null) {
			newParent.setJmbg(parent.getJmbg());
		}
		if (parent.getDateOfBirth() != null) {
			newParent.setDateOfBirth(parent.getDateOfBirth());
		}
		if (parent.getPhoneNumber() != null) {
			newParent.setPhoneNumber(parent.getPhoneNumber());
		}
		newParent.setRole(roleRepo.findByName(ERole.ROLE_PARENT).get());
		newParent.setDeleted(0);

		logger.info("##POST NEW STUDENT## Translation complete, exiting service and returning to endpoint.");

		return newParent;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for creating Parents
	 * 
	 */
	@Override
	public ResponseEntity<?> createdParentDTOtranslation(ParentEntity parent) {

		logger.info("##POST NEW PARENT## Entered service for Entity translation to DTO.");

		CreatedParentDTO newParentDTO = new CreatedParentDTO();

		if (parent.getName() != null) {
			newParentDTO.setName(parent.getName());
		}
		if (parent.getSurname() != null) {
			newParentDTO.setSurname(parent.getSurname());
		}
		if (parent.getDateOfBirth() != null) {
			newParentDTO.setDateOfBirth(parent.getDateOfBirth());
		}
		if (parent.getEmail() != null) {
			newParentDTO.setEmail(parent.getEmail());
		}
		if (parent.getJmbg() != null) {
			newParentDTO.setJmbg(parent.getJmbg());
		}
		if (parent.getUsername() != null) {
			newParentDTO.setUsername(parent.getUsername());
		}
		if (parent.getPhoneNumber() != null) {
			newParentDTO.setPhoneNumber(parent.getPhoneNumber());
		}

		logger.info("##POST NEW PARENT## Translation complete, exiting service and returning to endpoint.\n"
				+ newParentDTO.toString());

		return new ResponseEntity<CreatedParentDTO>(newParentDTO, HttpStatus.OK);

	}

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for creating Teachers
	 * 
	 */
	@Override
	public TeacherEntity createTeacherDTOtranslation(CreateTeacherDTO teacher) {
		logger.info("##POST NEW TEACHER## Entered service for DTO translation to entity.");

		TeacherEntity newTeacher = new TeacherEntity();

		if (teacher.getName() != null) {
			newTeacher.setName(teacher.getName());
		}
		if (teacher.getSurname() != null) {
			newTeacher.setSurname(teacher.getSurname());
		}
		if (teacher.getEmail() != null) {
			newTeacher.setEmail(teacher.getEmail());
		}
		if (teacher.getUsername() != null) {
			newTeacher.setUsername(teacher.getUsername());
		}
		if (teacher.getPassword() != null) {
			newTeacher.setPassword(teacher.getPassword());
		}
		if (teacher.getRepeatedPassword() != null) {
			newTeacher.setRepeatedPassword(teacher.getRepeatedPassword());
		}
		if (teacher.getJmbg() != null) {
			newTeacher.setJmbg(teacher.getJmbg());
		}
		if (teacher.getDateOfBirth() != null) {
			newTeacher.setDateOfBirth(teacher.getDateOfBirth());
		}
		if (teacher.getStartOfEmployment() != null) {
			newTeacher.setStartOfEmployment(teacher.getStartOfEmployment());
		}
		if (teacher.getSalary() != null) {
			newTeacher.setSalary(teacher.getSalary());
		} else {
			newTeacher.setSalary(60000.00);
		}
		if (teacher.getWeeklyHourCapacity() != null) {
			newTeacher.setWeeklyHourCapacity(teacher.getWeeklyHourCapacity());
		}
		newTeacher.setRole(roleRepo.findByName(ERole.ROLE_TEACHER).get());
		newTeacher.setIsAdministrator(0);
		newTeacher.setIsHomeroomTeacher(0);
		newTeacher.setIsHeadmaster(0);
		newTeacher.setDeleted(0);

		logger.info("##POST NEW TEACHER## Translation complete, exiting service and returning to endpoint.");
		return newTeacher;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for creating Teachers
	 * 
	 */
	@Override
	public ResponseEntity<?> createdTeacherDTOtranslation(TeacherEntity teacher) {
		logger.info("##POST NEW TEACHER## Entered service for Entity translation to DTO.");
		CreatedTeacherDTO newTeacherDTO = new CreatedTeacherDTO();

		if (teacher.getName() != null) {
			newTeacherDTO.setName(teacher.getName());
		}
		if (teacher.getSurname() != null) {
			newTeacherDTO.setSurname(teacher.getSurname());
		}
		if (teacher.getDateOfBirth() != null) {
			newTeacherDTO.setDateOfBirth(teacher.getDateOfBirth());
		}
		if (teacher.getEmail() != null) {
			newTeacherDTO.setEmail(teacher.getEmail());
		}
		if (teacher.getJmbg() != null) {
			newTeacherDTO.setJmbg(teacher.getJmbg());
		}
		if (teacher.getUsername() != null) {
			newTeacherDTO.setUsername(teacher.getUsername());
		}
		if (teacher.getSalary() != null) {
			newTeacherDTO.setSalary(teacher.getSalary());
		}
		if (teacher.getWeeklyHourCapacity() != null) {
			newTeacherDTO.setWeeklyHourCapacity(teacher.getWeeklyHourCapacity());
		}
		if (teacher.getStartOfEmployment() != null) {
			newTeacherDTO.setStartOfEmployment(teacher.getStartOfEmployment());
		}

		logger.info(
				"##POST NEW TEACHER## Translation complete, exiting service and returning to endpoint. All actions complete, teacher created.\n"
						+ newTeacherDTO.toString());
		return new ResponseEntity<CreatedTeacherDTO>(newTeacherDTO, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes an input DTO along with existing User and updates fields
	 * used for editing Users general info
	 * 
	 */
	@Override
	public UserEntity updateUserDTOtranslation(UpdateUserDTO updatedUser, UserEntity ogUser) {

		logger.info("##PUT USER## Entered service for DTO translation to entity.");

		if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
			ogUser.setName(updatedUser.getName());
		}
		if (updatedUser.getSurname() != null && !updatedUser.getSurname().isBlank()) {
			ogUser.setSurname(updatedUser.getSurname());
		}
		if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
			ogUser.setEmail(updatedUser.getEmail());
		}
		if (updatedUser.getUsername() != null && !updatedUser.getUsername().isBlank()) {
			ogUser.setUsername(updatedUser.getUsername());
		}
		if (updatedUser.getJmbg() != null && !updatedUser.getJmbg().isBlank()) {
			ogUser.setJmbg(updatedUser.getJmbg());
		}
		if (updatedUser.getDateOfBirth() != null) {
			ogUser.setDateOfBirth(updatedUser.getDateOfBirth());
		}

		logger.info("##PUT USER## Translation complete, exiting service and returning to endpoint.");

		return ogUser;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for editing Users general info
	 * 
	 */
	@Override
	public ResponseEntity<?> updatedUserDTOtranslation(UserEntity ogUser) {

		logger.info("##PUT USER## Entered service for Entity translation to DTO.");

		UpdatedUserDTO updatedUserDTO = new UpdatedUserDTO();

		if (ogUser.getName() != null && !ogUser.getName().isBlank()) {
			updatedUserDTO.setName(ogUser.getName());
		}
		if (ogUser.getSurname() != null && !ogUser.getSurname().isBlank()) {
			updatedUserDTO.setSurname(ogUser.getSurname());
		}
		if (ogUser.getDateOfBirth() != null) {
			updatedUserDTO.setDateOfBirth(ogUser.getDateOfBirth());
		}
		if (ogUser.getEmail() != null && !ogUser.getEmail().isBlank()) {
			updatedUserDTO.setEmail(ogUser.getEmail());
		}
		if (ogUser.getJmbg() != null && !ogUser.getJmbg().isBlank()) {
			updatedUserDTO.setJmbg(ogUser.getJmbg());
		}
		if (ogUser.getUsername() != null && !ogUser.getUsername().isBlank()) {
			updatedUserDTO.setUsername(ogUser.getUsername());
		}

		logger.info(
				"##PUT USER## Translation complete, exiting service and returning to endpoint. All actions complete, user updated.\n"
						+ updatedUserDTO.toString());

		return new ResponseEntity<UpdatedUserDTO>(updatedUserDTO, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for deleting Users
	 * 
	 */
	@Override
	public ResponseEntity<?> deletedUserDTOtranslation(UserEntity user) {

		logger.info("##DELETE USER## Entered service for Entity translation to DTO.");

		DeletedUserDTO deletedUser = new DeletedUserDTO();
		deletedUser.setName(user.getName());
		deletedUser.setSurname(user.getSurname());
		deletedUser.setRole(user.getRole().getName().toString());
		deletedUser.setUsername(user.getUsername());

		logger.info(
				"##DELETE USER## Translation complete, exiting service and returning to endpoint. All actions complete, user deleted.\n"
						+ deletedUser.toString());
		return new ResponseEntity<DeletedUserDTO>(deletedUser, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes care of asigning roles to teachers along with salary bonuses
	 * used for changing teacher's role
	 * 
	 */
	@Override
	public TeacherEntity updateTeacherRole(TeacherEntity teacher, String role, Double bonus) {

		logger.info("##CHANGE TEACHER ROLE## Entered service for handlig salary logic.");

		teacher.setRole(roleRepo.findByName(ERole.valueOf(role)).get());

		// logic for assigning role-specific salary bonuses
		if (role.equals(ERole.ROLE_ADMIN.toString())) {
			teacher.setIsAdministrator(1);
			teacher.setIsHomeroomTeacher(0);
			teacher.setIsHeadmaster(0);
			teacher.setSalaryHeadmasterBonus(0.00);
			teacher.setSalaryHomeroomBonus(0.00);
			if (bonus != null) {
				teacher.setSalaryAdminBonus(bonus);
			} else {
				teacher.setSalaryAdminBonus(15000.00);
			}
		}
		if (role.equals(ERole.ROLE_HOMEROOM.toString())) {
			teacher.setIsAdministrator(0);
			teacher.setIsHomeroomTeacher(1);
			teacher.setIsHeadmaster(0);
			teacher.setSalaryHeadmasterBonus(0.00);
			teacher.setSalaryAdminBonus(0.00);
			if (bonus != null) {
				teacher.setSalaryHomeroomBonus(bonus);
			} else {
				teacher.setSalaryHomeroomBonus(15000.00);
			}
		}
		if (role.equals(ERole.ROLE_HEADMASTER.toString())) {
			teacher.setIsAdministrator(0);
			teacher.setIsHomeroomTeacher(0);
			teacher.setIsHeadmaster(1);
			teacher.setSalaryAdminBonus(0.00);
			teacher.setSalaryHomeroomBonus(0.00);
			if (bonus != null) {
				teacher.setSalaryHeadmasterBonus(bonus);
			} else {
				teacher.setSalaryHeadmasterBonus(15000.00);
			}
		}
		if (role.equals(ERole.ROLE_TEACHER.toString())) {
			teacher.setIsAdministrator(0);
			teacher.setIsHomeroomTeacher(0);
			teacher.setIsHeadmaster(0);
			teacher.setSalaryHeadmasterBonus(0.00);
			teacher.setSalaryAdminBonus(0.00);
			teacher.setSalaryHomeroomBonus(0.00);
		}

		logger.info("##CHANGE TEACHER ROLE## Returning to controller.");

		return userRepo.save(teacher);
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for fetching Users
	 * 
	 */
	@Override
	public GetUserDTO foundUserDTOtranslation(UserEntity user) {

		logger.info("##GET ALL USERS## Entered service for Entity translation to DTO.");

		GetUserDTO getUser = new GetUserDTO();
		getUser.setName(user.getName());
		getUser.setSurname(user.getSurname());
		getUser.setRole(user.getRole().getName().toString());
		getUser.setUsername(user.getUsername());
		getUser.setEmail(user.getEmail());

		logger.info("##GET ALL USERS## Translation complete, exiting service and returning to endpoint.");
		return getUser;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for fetching Students via Parent
	 * 
	 */
	@Override
	public GetChildrenDTO foundChildrenDTOtranslation(StudentParentEntity student) {

		logger.info("##GET CHILDREN## Entered service for DTO translation to entity.");
		List<StudentParentEntity> ogParents = studentParentRepo.findByStudent(student.getStudent());

		logger.info("##GET CHILDREN## Setting all fields in Entity.");
		GetChildrenDTO getStudent = new GetChildrenDTO();
		getStudent.setName(student.getStudent().getName());
		getStudent.setSurname(student.getStudent().getSurname());
		getStudent.setRole(student.getStudent().getRole().getName().toString());
		getStudent.setUsername(student.getStudent().getUsername());
		if (student.getStudent().getBelongsToStudentGroup() != null) {
			getStudent.setStudentGroup(student.getStudent().getBelongsToStudentGroup().getYear() + "-"
					+ student.getStudent().getBelongsToStudentGroup().getYearIndex());
		}
		Map<String, String> parents = new HashMap<String, String>();
		for (StudentParentEntity parent : ogParents) {
			parents.put(parent.getParent().getName() + " " + parent.getParent().getSurname(),
					parent.getParent().getPhoneNumber());
		}
		getStudent.setParents(parents);
		logger.info("##GET CHILDREN## Exiting service for DTO translation to entity.");
		return getStudent;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for fetching Parents via Student
	 * 
	 */
	@Override
	public GetParentsDTO foundParentsDTOtranslation(StudentParentEntity parent) {

		logger.info("##GET PARENTS## Entered service for DTO translation to entity.");
		List<StudentParentEntity> ogChildren = studentParentRepo.findByParent(parent.getParent());

		logger.info("##GET PARENTS## Setting all fields in Entity.");
		GetParentsDTO getParent = new GetParentsDTO();
		getParent.setName(parent.getParent().getName());
		getParent.setSurname(parent.getParent().getSurname());
		getParent.setRole(parent.getParent().getRole().getName().toString());
		getParent.setUsername(parent.getParent().getUsername());
		getParent.setPhoneNumber(parent.getParent().getPhoneNumber());
		Set<String> children = new HashSet<String>();
		for (StudentParentEntity child : ogChildren) {
			children.add(child.getStudent().getName() + " " + child.getStudent().getSurname());
		}
		getParent.setChildren(children);
		logger.info("##GET PARENTS## Exiting service for DTO translation to entity.");
		return getParent;
	}

}
